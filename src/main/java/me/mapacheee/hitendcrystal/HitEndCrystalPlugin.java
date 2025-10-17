package me.mapacheee.hitendcrystal;

import com.thewinterframework.paper.PaperWinterPlugin;
import com.thewinterframework.plugin.WinterBootPlugin;

@WinterBootPlugin
public class HitEndCrystalPlugin extends PaperWinterPlugin {

    private static HitEndCrystalPlugin instance;

    @Override
    public void onPluginEnable() {
        instance = this;
        getLogger().info("HitEndCrystal enabled successfully!");
    }

    @Override
    public void onPluginDisable() {
        getLogger().info("HitEndCrystal disabled!");
    }

    public static HitEndCrystalPlugin getInstance() {
        return instance;
    }
}
