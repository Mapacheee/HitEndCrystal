package me.mapacheee.hitendcrystal.config;

import com.thewinterframework.configurate.config.Configurate;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
@Configurate("messages")
public record Messages(
    String clickSuccess,
    String clickCooldown,
    String notInRegion,
    String goalReached,
    List<String> statsMessage,
    String topHeader,
    String topEntry,
    String topFooter,
    String reloadSuccess,
    String crystalSet,
    String counterReset,
    String noPermission,
    String playerNotFound,
    String invalidWorld,
    String databaseError
) {}
