package me.mapacheee.hitendcrystal.service;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.hitendcrystal.HitEndCrystalPlugin;
import me.mapacheee.hitendcrystal.config.Config;
import me.mapacheee.hitendcrystal.config.ConfigService;
import org.bukkit.*;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;

@Service
public class CrystalService {

    private final ConfigService configService;
    private Location crystalLocation;
    private EnderCrystal crystal;
    private boolean initialized = false;

    @Inject
    public CrystalService(ConfigService configService) {
        this.configService = configService;
    }

    private synchronized void ensureInitialized() {
        if (!initialized) {
            loadCrystalLocation();
            initialized = true;
        }
    }

    private void loadCrystalLocation() {
        Config config = configService.getConfig();
        if (config == null) {
            HitEndCrystalPlugin.getInstance().getLogger().warning("Config not loaded yet, crystal will be spawned later");
            return;
        }

        Config.CrystalLocation loc = config.crystalLocation();
        World world = Bukkit.getWorld(loc.world());

        if (world != null) {
            this.crystalLocation = new Location(world, loc.x(), loc.y(), loc.z());
        } else {
            HitEndCrystalPlugin.getInstance().getLogger().warning(
                "World '" + loc.world() + "' not found! Crystal not spawned."
            );
        }
    }

    public void spawnCrystal() {
        if (!initialized) {
            ensureInitialized();
        }

        if (crystalLocation == null) return;

        if (crystal != null && !crystal.isDead()) {
            crystal.remove();
        }

        crystalLocation.getChunk().load();
        crystal = (EnderCrystal) crystalLocation.getWorld().spawnEntity(crystalLocation, EntityType.END_CRYSTAL);
        crystal.setShowingBottom(false);
        crystal.setInvulnerable(true); // Make it invulnerable to prevent breaking
    }

    public void playCrystalAnimation(Location location) {
        location.getWorld().spawnParticle(
            Particle.EXPLOSION,
            location,
            1,
            0, 0, 0,
            0
        );

        location.getWorld().playSound(
            location,
            Sound.ENTITY_GENERIC_EXPLODE,
            1.0f,
            1.0f
        );

    }

    public boolean isCrystal(EnderCrystal entity) {
        if (!initialized) {
            ensureInitialized();
        }
        if (crystal == null) return false;
        return entity.getUniqueId().equals(crystal.getUniqueId());
    }

    public void setCrystalLocation(Location location) {
        this.crystalLocation = location;
        this.initialized = true; // Mark as initialized manually
        spawnCrystal();
    }

    public Location getCrystalLocation() {
        if (!initialized) {
            ensureInitialized();
        }
        return crystalLocation;
    }

    public EnderCrystal getCrystal() {
        if (!initialized) {
            ensureInitialized();
        }
        return crystal;
    }
}
