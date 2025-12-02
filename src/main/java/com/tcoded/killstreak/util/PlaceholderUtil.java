package com.tcoded.killstreak.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return text;
        }

        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
