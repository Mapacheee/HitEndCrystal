package me.mapacheee.hitendcrystal;

import com.thewinterframework.paper.PaperWinterPlugin;
import com.thewinterframework.plugin.WinterBootPlugin;
/*import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.RuntimeLibPluginConfiguration;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;

import java.io.File;
import java.net.URLClassLoader;*/

@WinterBootPlugin
public class HitEndCrystalPlugin extends PaperWinterPlugin {

    private static HitEndCrystalPlugin instance;

    public HitEndCrystalPlugin() {
    }

    /*@Override
    public void onPluginLoad() {
        super.onPluginLoad();
        instance = this;

        RuntimeLibPluginConfiguration config = RuntimeLibPluginConfiguration.parse();
        File libraries = new File(getDataFolder(), config.getLibsFolder());
        if (!libraries.exists()) {
            getLogger().info("[" + getName() + "] It appears you're running " + getName() + " for the first time.");
            getLogger().info("[" + getName() + "] Please give me a few seconds to install dependencies. This is a one-time process.");
        }
        DependencyManager dependencyManager = new DependencyManager(
                libraries,
                URLClassLoaderWrapper.wrap((URLClassLoader) getClass().getClassLoader())
        );
        config.getDependencies().forEach(dependencyManager::dependency);
        config.getRepositories().forEach(dependencyManager::repository);
        config.getRelocations().forEach(dependencyManager::relocate);
        dependencyManager.load();
    }*/

    @Override
    public void onPluginEnable() {
        getLogger().info("HitEndCrystal enabled successfully!");
    }

    @Override
    public void onPluginDisable() {
        getLogger().info("HitEndCrystal disabled!");
        instance = null;
    }

    public static HitEndCrystalPlugin getInstance() {
        return instance;
    }
}
