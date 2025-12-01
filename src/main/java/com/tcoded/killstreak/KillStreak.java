package com.tcoded.killstreak;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.killstreak.command.KillStreakCommand;
import com.tcoded.killstreak.data.PlayerDataManager;
import com.tcoded.killstreak.listener.KillListener;
import com.tcoded.killstreak.listener.PlayerConnectionListener;
import com.tcoded.killstreak.placeholder.KillStreakExpansion;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class.
 */
public class KillStreak extends JavaPlugin {

    private FoliaLib foliaLib;
    private PlayerDataManager dataManager;
    private KillStreakExpansion expansion;

    /**
     * @return FoliaLib instance for scheduling tasks
     */
    public FoliaLib getFoliaLib() {
        return foliaLib;
    }

    /**
     * @return data manager
     */
    public PlayerDataManager getDataManager() {
        return dataManager;
    }

    @Override
    public void onEnable() {
        this.foliaLib = new FoliaLib(this);
        this.dataManager = new PlayerDataManager(getDataFolder());
        // Save default config
        saveDefaultConfig();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new KillListener(dataManager), this);
        pm.registerEvents(new PlayerConnectionListener(this, dataManager), this);
        // Register /ks command
        this.getCommand("ks").setExecutor(new KillStreakCommand(this));

        if (pm.isPluginEnabled("PlaceholderAPI")) {
            this.expansion = new KillStreakExpansion(this, dataManager);
            this.expansion.register();
        }
    }

    @Override
    public void onDisable() {
        if (expansion != null) {
            expansion.unregister();
        }
        HandlerList.unregisterAll(this);
        dataManager.saveAll();
    }
}
