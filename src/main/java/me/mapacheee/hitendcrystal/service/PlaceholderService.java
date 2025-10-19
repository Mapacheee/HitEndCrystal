package me.mapacheee.hitendcrystal.service;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnDisable;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import me.mapacheee.hitendcrystal.HitEndCrystalPlugin;
import me.mapacheee.hitendcrystal.data.ClickStorage;
import me.mapacheee.hitendcrystal.placeholder.HitEndCrystalPlaceholder;
import org.bukkit.Bukkit;

import java.util.logging.Level;

@Service
public class PlaceholderService {

    private final ClickCounterService clickCounterService;
    private final ClickStorage clickStorage;
    private final HitEndCrystalPlugin plugin;
    private HitEndCrystalPlaceholder placeholder;

    @Inject
    public PlaceholderService(ClickCounterService clickCounterService, ClickStorage clickStorage, HitEndCrystalPlugin plugin) {
        this.clickCounterService = clickCounterService;
        this.clickStorage = clickStorage;
        this.plugin = plugin;
    }

    @OnEnable
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                registerPlaceholders();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register placeholders", e);
            }
        } else {
            plugin.getLogger().warning("PlaceholderAPI not found! Placeholders will not work.");
        }
    }

    private void registerPlaceholders() {
        placeholder = new HitEndCrystalPlaceholder(plugin, clickCounterService, clickStorage);
        placeholder.register();
    }

    @OnDisable
    public void onDisable() {
        if (placeholder != null) {
            placeholder.unregister();
        }
    }
}
