package me.mapacheee.hitendcrystal.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.mapacheee.hitendcrystal.HitEndCrystalPlugin;
import me.mapacheee.hitendcrystal.data.ClickStorage;
import me.mapacheee.hitendcrystal.data.PlayerClickData;
import me.mapacheee.hitendcrystal.service.ClickCounterService;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HitEndCrystalPlaceholder extends PlaceholderExpansion {

    private final HitEndCrystalPlugin plugin;
    private final ClickCounterService clickCounterService;
    private final ClickStorage clickStorage;

    public HitEndCrystalPlaceholder(HitEndCrystalPlugin plugin, ClickCounterService clickCounterService, ClickStorage clickStorage) {
        this.plugin = plugin;
        this.clickCounterService = clickCounterService;
        this.clickStorage = clickStorage;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "hitendcrystal";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Mapacheee";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("player_clicks")) {
            return String.valueOf(clickCounterService.getClicks(player));
        }

        // %hitendcrystal_player_position%
        if (params.equalsIgnoreCase("player_position")) {
            try {
                return String.valueOf(clickStorage.getPlayerPosition(player.getUniqueId()).get());
            } catch (Exception e) {
                return "N/A";
            }
        }

        // Top placeholders
        if (params.startsWith("top")) {
            String[] parts = params.split("_");
            if (parts.length != 2) return "";

            try {
                int position = Integer.parseInt(parts[0].replace("top", ""));
                String type = parts[1]; // "name" or "clicks"

                List<PlayerClickData> topPlayers = clickStorage.getTopPlayers(position).join();

                if (topPlayers.size() >= position) {
                    PlayerClickData data = topPlayers.get(position - 1);

                    if (type.equalsIgnoreCase("name")) {
                        return data.playerName();
                    } else if (type.equalsIgnoreCase("clicks")) {
                        return String.valueOf(data.clicks());
                    }
                }
            } catch (Exception e) {
                return "";
            }
        }

        return null;
    }
}
