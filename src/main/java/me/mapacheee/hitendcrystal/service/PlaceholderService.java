package me.mapacheee.hitendcrystal.service;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnDisable;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import me.mapacheee.hitendcrystal.HitEndCrystalPlugin;
import me.mapacheee.hitendcrystal.data.ClickStorage;
import me.mapacheee.hitendcrystal.placeholder.HitEndCrystalPlaceholder;
import org.bukkit.Bukkit;

@Service
public class PlaceholderService {

    private final ClickCounterService clickCounterService;
    private final ClickStorage clickStorage;
    private HitEndCrystalPlaceholder placeholder;

    @Inject
    public PlaceholderService(ClickCounterService clickCounterService, ClickStorage clickStorage) {
        this.clickCounterService = clickCounterService;
        this.clickStorage = clickStorage;
    }

    @OnEnable
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                registerPlaceholders();
            } catch (Exception e) {
                if (HitEndCrystalPlugin.getInstance() != null) {
                    HitEndCrystalPlugin.getInstance().getLogger().warning("Failed to register PlaceholderAPI hooks: " + e.getMessage());
                }
            }
        } else {
            if (HitEndCrystalPlugin.getInstance() != null) {
                HitEndCrystalPlugin.getInstance().getLogger().warning("PlaceholderAPI not found! Placeholders will not work.");
            }
        }
    }

    @OnDisable
    public void onDisable() {
        try {
            unregister();
        } catch (Exception e) {
        }
    }

    private void registerPlaceholders() {
        placeholder = new HitEndCrystalPlaceholder(
            HitEndCrystalPlugin.getInstance(),
            clickCounterService,
            clickStorage
        );

        if (placeholder.register()) {
            HitEndCrystalPlugin.getInstance().getLogger().info("PlaceholderAPI hooks registered successfully!");
        } else {
            HitEndCrystalPlugin.getInstance().getLogger().warning("Failed to register PlaceholderAPI hooks!");
        }
    }

    private void unregister() {
        if (placeholder != null) {
            placeholder.unregister();
            if (HitEndCrystalPlugin.getInstance() != null) {
                HitEndCrystalPlugin.getInstance().getLogger().info("PlaceholderAPI hooks unregistered.");
            }
        }
    }
}
