package me.mapacheee.hitendcrystal.listener;

import com.google.inject.Inject;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.thewinterframework.paper.listener.ListenerComponent;
import me.mapacheee.hitendcrystal.HitEndCrystalPlugin;
import me.mapacheee.hitendcrystal.config.Config;
import me.mapacheee.hitendcrystal.config.ConfigService;
import me.mapacheee.hitendcrystal.service.ClickCounterService;
import me.mapacheee.hitendcrystal.service.SwordService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ListenerComponent
public class PlayerListener implements Listener {

    private final ConfigService configService;
    private final ClickCounterService clickCounterService;
    private final SwordService swordService;
    private final Set<UUID> playersInRegion = new HashSet<>();

    @Inject
    public PlayerListener(
        ConfigService configService,
        ClickCounterService clickCounterService,
        SwordService swordService
    ) {
        this.configService = configService;
        this.clickCounterService = clickCounterService;
        this.swordService = swordService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        clickCounterService.loadPlayer(player);

        Bukkit.getScheduler().runTaskLater(HitEndCrystalPlugin.getInstance(), () -> {
            if (isInRegion(player)) {
                playersInRegion.add(player.getUniqueId());
                swordService.giveSword(player);
            }
        }, 10L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        clickCounterService.unloadPlayer(player);
        playersInRegion.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        boolean wasInRegion = playersInRegion.contains(player.getUniqueId());
        boolean isNowInRegion = isInRegion(player);

        if (!wasInRegion && isNowInRegion) {
            playersInRegion.add(player.getUniqueId());
            swordService.giveSword(player);
        } else if (wasInRegion && !isNowInRegion) {
            playersInRegion.remove(player.getUniqueId());
            swordService.removeSword(player);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (swordService.isClickSword(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    private boolean isInRegion(Player player) {
        Config config = configService.getConfig();
        if (config == null) {
            return false;
        }

        String regionName = config.regionName();

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));

        for (ProtectedRegion region : regions) {
            if (region.getId().equalsIgnoreCase(regionName)) {
                return true;
            }
        }

        return false;
    }
}
