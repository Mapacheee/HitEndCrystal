package me.mapacheee.hitendcrystal.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class CrystalLocationConfig {
    public String world = "world";
    public double x = 0;
    public double y = 0;
    public double z = 0;

    public CrystalLocationConfig() {}

    public CrystalLocationConfig(String world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

