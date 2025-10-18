package me.mapacheee.hitendcrystal.command;

import com.google.inject.Inject;
import com.thewinterframework.command.CommandComponent;
import com.thewinterframework.service.ReloadServiceManager;
import me.mapacheee.hitendcrystal.config.ConfigService;
import me.mapacheee.hitendcrystal.data.ClickStorage;
import me.mapacheee.hitendcrystal.data.PlayerClickData;
import me.mapacheee.hitendcrystal.service.ClickCounterService;
import me.mapacheee.hitendcrystal.service.CrystalService;
import me.mapacheee.hitendcrystal.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.UUID;

@CommandComponent
@Command("enderhits|eh|hits")
public class EnderHitsCommand {

    private final ConfigService configService;
    private final ClickCounterService clickCounterService;
    private final ClickStorage clickStorage;
    private final CrystalService crystalService;
    private final MessageUtil messageUtil;
    private final ReloadServiceManager reloadServiceManager;

    @Inject
    public EnderHitsCommand(
        ConfigService configService,
        ClickCounterService clickCounterService,
        ClickStorage clickStorage,
        CrystalService crystalService,
        MessageUtil messageUtil,
        ReloadServiceManager reloadServiceManager
    ) {
        this.configService = configService;
        this.clickCounterService = clickCounterService;
        this.clickStorage = clickStorage;
        this.crystalService = crystalService;
        this.messageUtil = messageUtil;
        this.reloadServiceManager = reloadServiceManager;
    }

    @Command("")
    public void defaultCommand(final Player player) {
        int clicks = clickCounterService.getClicks(player);

        if (configService.getConfig() == null || configService.getMessages() == null) {
            player.sendMessage("§cConfig not loaded yet. Please try again.");
            return;
        }

        int goal = configService.getConfig().clickGoal().target();

        clickStorage.getPlayerPosition(player.getUniqueId()).thenAccept(position -> {
            for (String line : configService.getMessages().statsMessage()) {
                String formatted = messageUtil.format(
                    line.replace("{clicks}", String.valueOf(clicks))
                        .replace("{position}", String.valueOf(position))
                        .replace("{goal}", String.valueOf(goal))
                );
                player.sendMessage(formatted);
            }
        });
    }

    @Command("top")
    public void topCommand(final Source sender) {
        if (configService.getMessages() == null) {
            sender.source().sendMessage("§cPlugin configuration not loaded yet. Please try again.");
            return;
        }

        clickStorage.getTopPlayers(10).thenAccept(topPlayers -> {
            sender.source().sendMessage(messageUtil.format(configService.getMessages().topHeader()));

            int position = 1;
            for (PlayerClickData data : topPlayers) {
                String line = messageUtil.format(
                    configService.getMessages().topEntry()
                        .replace("{position}", String.valueOf(position))
                        .replace("{player}", data.playerName())
                        .replace("{clicks}", String.valueOf(data.clicks()))
                );
                sender.source().sendMessage(line);
                position++;
            }

            sender.source().sendMessage(messageUtil.format(configService.getMessages().topFooter()));
        });
    }

    @Command("reload")
    @Permission("hitendcrystal.admin")
    public void reloadCommand(final Source sender) {
        reloadServiceManager.reload();

        if (configService.getMessages() != null) {
            sender.source().sendMessage(messageUtil.format(configService.getMessages().reloadSuccess()));
        } else {
            sender.source().sendMessage("§aPlugin reloaded!");
        }
    }

    @Command("setcrystal")
    @Permission("hitendcrystal.admin")
    public void setCrystalCommand(final Source sender) {
        if (!(sender.source() instanceof Player player)) {
            sender.source().sendMessage(messageUtil.format("&#FF0000Este comando solo puede ser ejecutado por jugadores!"));
            return;
        }
        Location locationToSet = player.getLocation();
        crystalService.setCrystalLocationAndRespawn(locationToSet);
        if (configService.getMessages() != null) {
            sender.source().sendMessage(messageUtil.format(configService.getMessages().crystalSet()));
        } else {
            sender.source().sendMessage("§aEnder Crystal location updated!");
        }
    }

    @Command("reset <player>")
    @Permission("hitendcrystal.admin")
    public void resetCommand(final Source sender, final @Argument("player") String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        UUID targetUuid;

        if (target != null) {
            targetUuid = target.getUniqueId();
        } else {
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if (!offlinePlayer.hasPlayedBefore()) {
                if (configService.getMessages() != null) {
                    sender.source().sendMessage(messageUtil.format(configService.getMessages().playerNotFound()));
                } else {
                    sender.source().sendMessage("§cPlayer not found!");
                }
                return;
            }
            targetUuid = offlinePlayer.getUniqueId();
        }

        clickCounterService.resetClicks(targetUuid);

        if (configService.getMessages() != null) {
            String message = messageUtil.format(
                configService.getMessages().counterReset()
                    .replace("{player}", playerName)
            );
            sender.source().sendMessage(message);
        } else {
            sender.source().sendMessage("§aPlayer counter reset!");
        }
    }
}
