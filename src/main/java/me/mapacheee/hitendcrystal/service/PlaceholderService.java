package me.mapacheee.hitendcrystal.service;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
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

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            registerPlaceholders();
        }
    }

    private void registerPlaceholders() {
        placeholder = new HitEndCrystalPlaceholder(
            HitEndCrystalPlugin.getInstance(),
            clickCounterService,
            clickStorage
        );
        placeholder.register();

        HitEndCrystalPlugin.getInstance().getLogger().info("PlaceholderAPI hooks registered!");
    }

    public void unregister() {
        if (placeholder != null) {
            placeholder.unregister();
        }
    }
}
