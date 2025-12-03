package com.tcoded.killstreak.util;

import com.tcoded.killstreak.test.TestLogger;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

/**
 * Utility for parsing PlaceholderAPI placeholders.
 */
public final class PlaceholderUtil {

    private PlaceholderUtil() {
    }

    /**
     * Parses PlaceholderAPI tokens for the given player.
     *
     * @param player player to provide context
     * @param text   template text
     * @return parsed text
     */
    public static String parsePlaceholders(Player player, String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        if (!pluginManager.isPluginEnabled("PlaceholderAPI")) {
            return text;
        }

        String result = PlaceholderAPI.setPlaceholders(player, text);
        TestLogger.logTestEnv("[PlaceholderUtil] Parsed: '" + text + "' -> '" + result + "' (player: " + player.getName() + ")");
        return result;
    }
}
