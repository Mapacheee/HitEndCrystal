package me.mapacheee.hitendcrystal.service;

import com.google.inject.Inject;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import com.thewinterframework.service.annotation.Service;
import me.mapacheee.hitendcrystal.config.ConfigService;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Service
public class WorldGuardService {

    private final ConfigService configService;

    @Inject
    public WorldGuardService(ConfigService configService) {
        this.configService = configService;
    }

    public boolean isInRegion(Player player) {
        if (configService == null || configService.getConfig() == null) return false;

        String regionName = configService.getConfig().regionName();
        if (regionName == null || regionName.isBlank()) return false;

        Location location = player.getLocation();

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));

            if (regions == null) return false;

            ProtectedRegion region = regions.getRegion(regionName);
            if (region == null) return false;

            return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean regionExists(String regionName, org.bukkit.World world) {
        if (regionName == null || world == null) return false;
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));

            if (regions == null) return false;

            return regions.hasRegion(regionName);
        } catch (Exception e) {
            return false;
        }
    }
}
