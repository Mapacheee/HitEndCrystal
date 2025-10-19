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

    @Inject
    public ConfigService(Container<Config> configContainer, Container<Messages> messagesContainer) {
        this.configContainer = configContainer;
        this.messagesContainer = messagesContainer;
    }

    public Config getConfig() {
        return configContainer.get();
    }

    public Messages getMessages() {
        return messagesContainer.get();
    }

    @OnReload
    public void reload() {
        configContainer.reload();
        messagesContainer.reload();
    }

    public Location getCrystalLocation() {
        Config.CrystalLocation cl = configContainer.get().crystalLocation();
        World world = Bukkit.getWorld(cl.world());
        if (world != null) {
            return new Location(world, cl.x(), cl.y(), cl.z());
        }
        return null;
    }

    public boolean setCrystalLocation(Location location) {
        return configContainer.update(config -> new Config(
            config.regionName(),
            config.cooldownSeconds(),
            config.feedbackMessage(),
            new Config.CrystalLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ()),
            config.swordItem(),
            config.clickGoal(),
            config.database()
        ));
    }
}
