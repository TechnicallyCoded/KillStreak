package com.tcoded.killstreak.listener;

import com.tcoded.killstreak.KillStreak;
import com.tcoded.killstreak.data.PlayerDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Handles player join and leave events for data management.
 */
public class PlayerConnectionListener implements Listener {

    private final PlayerDataManager manager;
    private final KillStreak plugin;

    /**
     * @param plugin  main plugin instance
     * @param manager data manager
     */
    public PlayerConnectionListener(KillStreak plugin, PlayerDataManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    /**
     * Loads player data asynchronously during pre login.
     *
     * @param event pre login event
     */
    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        manager.load(uuid);
    }

    /**
     * Saves and removes player data asynchronously when they leave.
     *
     * @param event quit event
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.getFoliaLib().getScheduler().runAsync(task -> manager.save(uuid));
        manager.remove(uuid);
    }
}
