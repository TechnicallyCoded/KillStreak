package com.tcoded.killstreak.placeholder;

import com.tcoded.killstreak.KillStreak;
import com.tcoded.killstreak.data.PlayerData;
import com.tcoded.killstreak.data.PlayerDataManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion providing kill streak placeholders.
 */
public class KillStreakExpansion extends PlaceholderExpansion {

    private final KillStreak plugin;
    private final PlayerDataManager manager;

    /**
     * @param plugin  main plugin instance
     * @param manager data manager
     */
    public KillStreakExpansion(KillStreak plugin, PlayerDataManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "killstreak";
    }

    @Override
    public @NotNull String getAuthor() {
        return "tcoded";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        if ("killstreak".equalsIgnoreCase(params)) {
            PlayerData data = manager.get(player.getUniqueId());
            if (data == null) {
                return null;
            }

            return String.valueOf(data.getKills());
        }

        return null;
    }
}
