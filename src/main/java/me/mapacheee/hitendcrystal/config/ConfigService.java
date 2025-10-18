package me.mapacheee.hitendcrystal.config;

import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnReload;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

@Service
public class ConfigService {

    private final Container<Config> configContainer;
    private final Container<Messages> messagesContainer;
    private final Container<CrystalLocationConfig> crystalLocationContainer;

    @Inject
    public ConfigService(Container<Config> configContainer, Container<Messages> messagesContainer, Container<CrystalLocationConfig> crystalLocationContainer) {
        this.configContainer = configContainer;
        this.messagesContainer = messagesContainer;
        this.crystalLocationContainer = crystalLocationContainer;
    }

    public Config getConfig() {
        return configContainer.get();
    }

    public Messages getMessages() {
        return messagesContainer.get();
    }

    public CrystalLocationConfig getCrystalLocationConfig() {
        return crystalLocationContainer.get();
    }

    @OnReload
    public void reload() {
        configContainer.reload();
        messagesContainer.reload();
        crystalLocationContainer.reload();
    }

    public Location getCrystalLocation() {
        CrystalLocationConfig config = getCrystalLocationConfig();
        World world = Bukkit.getWorld(config.world);
        if (world != null) {
            return new Location(world, config.x, config.y, config.z);
        }
        return null;
    }

    public boolean setCrystalLocation(Location location) {
        return crystalLocationContainer.update(config -> new CrystalLocationConfig(
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ()
        ));
    }
}
