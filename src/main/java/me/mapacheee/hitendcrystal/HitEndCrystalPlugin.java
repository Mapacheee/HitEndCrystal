package me.mapacheee.hitendcrystal;

import com.thewinterframework.paper.PaperWinterPlugin;
import com.thewinterframework.plugin.WinterBootPlugin;

@WinterBootPlugin
public class HitEndCrystalPlugin extends PaperWinterPlugin {

    private static HitEndCrystalPlugin instance;

    @Override
    public void onPluginEnable() {
        getLogger().info("HitEndCrystal enabled");
    }

    @Override
    public void onPluginDisable() {
        getLogger().info("HitEndCrystal disabled");
        instance = null;
    }

    public static HitEndCrystalPlugin getInstance() {
        return instance;
    }
}