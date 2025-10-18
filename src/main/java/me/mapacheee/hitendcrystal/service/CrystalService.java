package me.mapacheee.hitendcrystal.service;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import me.mapacheee.hitendcrystal.HitEndCrystalPlugin;
import me.mapacheee.hitendcrystal.config.ConfigService;
import org.bukkit.*;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.scheduler.BukkitRunnable;

@Service
public class CrystalService {

    private final ConfigService configService;
    private Location crystalLocation;
    private EnderCrystal crystal;
    private boolean initialized = false;
    private NamespacedKey crystalKey;

    @Inject
    public CrystalService(ConfigService configService) {
        this.configService = configService;
    }

    private NamespacedKey getCrystalKey() {
        if (crystalKey == null) {
            crystalKey = new NamespacedKey(HitEndCrystalPlugin.getInstance(), "hitendcrystal_entity");
        }
        return crystalKey;
    }

    private synchronized void ensureInitialized() {
        if (!initialized) {
            loadCrystalLocation();
            initialized = true;
        }
    }

    private void loadCrystalLocation() {
        this.crystalLocation = configService.getCrystalLocation();
    }

    @OnEnable
    public void onEnable() {

        if (configService.getConfig() == null) {
            scheduleInitRetry();
            return;
        }

        loadCrystalLocation();
        if (crystalLocation == null || crystalLocation.getWorld() == null) {
            scheduleInitRetry();
            return;
        }

        initOrLinkCrystal();
    }

    private void scheduleInitRetry() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (configService.getConfig() == null) return;
                    loadCrystalLocation();
                    if (crystalLocation == null || crystalLocation.getWorld() == null) return;
                    initOrLinkCrystal();
                    cancel();
                } catch (Exception e) {
                }
            }
        }.runTaskTimer(HitEndCrystalPlugin.getInstance(), 20L, 20L);
    }

    private void initOrLinkCrystal() {
        ensureInitialized();
        if (crystalLocation == null) return;

        try {
            for (Entity e : crystalLocation.getWorld().getNearbyEntities(crystalLocation, 2, 2, 2)) {
                if (e instanceof EnderCrystal found) {
                    try {
                        if (found.getPersistentDataContainer().has(getCrystalKey(), PersistentDataType.BYTE) || found.getScoreboardTags().contains("hitendcrystal_tag")) {
                            this.crystal = found;
                            this.crystal.setShowingBottom(false);
                            this.crystal.getPersistentDataContainer().set(getCrystalKey(), PersistentDataType.BYTE, (byte)1);
                            this.crystal.addScoreboardTag("hitendcrystal_tag");
                            HitEndCrystalPlugin.getInstance().getLogger().info("Found existing marked EnderCrystal and linked it.");
                            return;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            for (Entity e : crystalLocation.getWorld().getNearbyEntities(crystalLocation, 2, 2, 2)) {
                if (e instanceof EnderCrystal found) {
                    try {
                        found.remove();
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }

        spawnCrystal();
        HitEndCrystalPlugin.getInstance().getLogger().info("Spawned EnderCrystal at configured location.");
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
        try {
            crystal.getPersistentDataContainer().set(getCrystalKey(), PersistentDataType.BYTE, (byte)1);
        } catch (Exception ignored) {
        }
        try {
            crystal.addScoreboardTag("hitendcrystal_tag");
        } catch (Exception ignored) {
        }
    }

    public void playCrystalAnimation(Location location) {
        if (location == null || location.getWorld() == null) return;
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
        if (entity == null) return false;

        try {
            if (entity.getPersistentDataContainer().has(getCrystalKey(), PersistentDataType.BYTE)) {
                return true;
            }
        } catch (Exception ignored) {
        }

        if (crystal != null && !crystal.isDead()) {
            if (entity.getUniqueId().equals(crystal.getUniqueId())) return true;
        }

        if (crystalLocation != null) {
            try {
                Location entLoc = entity.getLocation();
                if (entLoc.getWorld() != null && entLoc.getWorld().equals(crystalLocation.getWorld())) {
                    double distSq = entLoc.distanceSquared(crystalLocation);
                    if (distSq <= 4.0) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    public EnderCrystal getCrystal() {
        if (!initialized) {
            ensureInitialized();
        }
        return crystal;
    }

    public void ensureLoaded() {
        try {
            if (configService.getConfig() == null) {
                return;
            }
            loadCrystalLocation();
            if (crystalLocation == null || crystalLocation.getWorld() == null) return;
            initOrLinkCrystal();
        } catch (Exception ignored) {
        }
    }

    public void setCrystalLocationAndRespawn(org.bukkit.Location location) {
        configService.setCrystalLocation(location);
        loadCrystalLocation();
        spawnCrystal();
    }
}
