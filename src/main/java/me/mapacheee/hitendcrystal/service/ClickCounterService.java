package me.mapacheee.hitendcrystal.service;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.hitendcrystal.config.Config;
import me.mapacheee.hitendcrystal.config.ConfigService;
import me.mapacheee.hitendcrystal.data.ClickStorage;
import me.mapacheee.hitendcrystal.data.PlayerClickData;
import me.mapacheee.hitendcrystal.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClickCounterService {

    private final ConfigService configService;
    private final ClickStorage clickStorage;
    private final MessageUtil messageUtil;

    private final Map<UUID, PlayerClickData> playerDataCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    @Inject
    public ClickCounterService(ConfigService configService, ClickStorage clickStorage, MessageUtil messageUtil) {
        this.configService = configService;
        this.clickStorage = clickStorage;
        this.messageUtil = messageUtil;
    }

    public void loadPlayer(Player player) {
        clickStorage.loadPlayer(player.getUniqueId(), player.getName())
            .thenAccept(data -> playerDataCache.put(player.getUniqueId(), data));
    }

    public void unloadPlayer(Player player) {
        PlayerClickData data = playerDataCache.remove(player.getUniqueId());
        if (data != null) {
            clickStorage.savePlayer(data);
        }
        cooldowns.remove(player.getUniqueId());
    }

    public boolean isOnCooldown(Player player) {
        Long lastHit = cooldowns.get(player.getUniqueId());
        if (lastHit == null) return false;

        long cooldownMs = configService.getConfig().cooldownSeconds() * 1000L;
        return System.currentTimeMillis() - lastHit < cooldownMs;
    }

    public long getRemainingCooldown(Player player) {
        Long lastHit = cooldowns.get(player.getUniqueId());
        if (lastHit == null) return 0;

        long cooldownMs = configService.getConfig().cooldownSeconds() * 1000L;
        long elapsed = System.currentTimeMillis() - lastHit;
        return Math.max(0, (cooldownMs - elapsed) / 1000);
    }

    public void registerClick(Player player) {
        PlayerClickData data = playerDataCache.get(player.getUniqueId());
        if (data == null) {
            data = new PlayerClickData(player.getUniqueId(), player.getName(), 0);
        }

        PlayerClickData newData = data.incrementClicks();
        playerDataCache.put(player.getUniqueId(), newData);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        clickStorage.savePlayer(newData);

        if (configService.getConfig().feedbackMessage()) {
            String message = messageUtil.format(
                configService.getMessages().clickSuccess()
                    .replace("{clicks}", String.valueOf(newData.clicks()))
            );
            player.sendMessage(message);
        }

        Config.ClickGoal goal = configService.getConfig().clickGoal();
        if (goal.enabled() && newData.clicks() >= goal.target()) {
            if (newData.clicks() % goal.target() == 0) { // Multiple of target
                onGoalReached(player, newData);
            }
        }
    }

    private void onGoalReached(Player player, PlayerClickData data) {
        Config.ClickGoal goal = configService.getConfig().clickGoal();

        String message = messageUtil.format(
            configService.getMessages().goalReached()
                .replace("{goal}", String.valueOf(goal.target()))
        );
        player.sendMessage(message);

        for (String command : goal.commands()) {
            String cmd = command.replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        if (goal.resetAfterGoal()) {
            PlayerClickData resetData = data.withClicks(0);
            playerDataCache.put(player.getUniqueId(), resetData);
            clickStorage.savePlayer(resetData);
        }
    }

    public int getClicks(Player player) {
        PlayerClickData data = playerDataCache.get(player.getUniqueId());
        return data != null ? data.clicks() : 0;
    }

    public void resetClicks(UUID uuid) {
        PlayerClickData data = playerDataCache.get(uuid);
        if (data != null) {
            PlayerClickData resetData = data.withClicks(0);
            playerDataCache.put(uuid, resetData);
        }
        clickStorage.resetPlayer(uuid);
    }

    public PlayerClickData getPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }
}

