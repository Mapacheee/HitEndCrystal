package me.mapacheee.hitendcrystal.data;

import java.util.UUID;

public record PlayerClickData(
    UUID uuid,
    String playerName,
    int clicks
) {

    public PlayerClickData withClicks(int newClicks) {
        return new PlayerClickData(uuid, playerName, newClicks);
    }

    public PlayerClickData incrementClicks() {
        return new PlayerClickData(uuid, playerName, clicks + 1);
    }
}

