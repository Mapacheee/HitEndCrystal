package me.mapacheee.hitendcrystal.config;

import com.thewinterframework.configurate.config.Configurate;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
@Configurate("config")
public record Config(
    String regionName,
    int cooldownSeconds,
    boolean feedbackMessage,
    CrystalLocation crystalLocation,
    SwordItem swordItem,
    ClickGoal clickGoal,
    Database database
) {

    @ConfigSerializable
    public record CrystalLocation(
        String world,
        double x,
        double y,
        double z
    ) {}

    @ConfigSerializable
    public record SwordItem(
        Material material,
        String name,
        List<String> lore,
        int slot,
        boolean unbreakable,
        List<String> enchantments
    ) {}

    @ConfigSerializable
    public record ClickGoal(
        boolean enabled,
        int target,
        boolean resetAfterGoal,
        List<String> commands
    ) {}

    @ConfigSerializable
    public record Database(
        String type,
        MysqlConfig mysql
    ) {
        @ConfigSerializable
        public record MysqlConfig(
            String host,
            int port,
            String database,
            String username,
            String password,
            int poolSize
        ) {}
    }
}
