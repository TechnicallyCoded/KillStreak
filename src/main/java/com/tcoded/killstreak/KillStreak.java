package com.tcoded.killstreak;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.killstreak.command.KillStreakCommand;
import com.tcoded.killstreak.config.KillstreakMilestoneConfigSection;
import com.tcoded.killstreak.data.PlayerDataManager;
import com.tcoded.killstreak.listener.KillListener;
import com.tcoded.killstreak.listener.PlayerConnectionListener;
import com.tcoded.killstreak.milestone.KillstreakMilestoneAnnouncer;
import com.tcoded.killstreak.placeholder.KillStreakExpansion;
import com.tcoded.killstreak.test.SelfTestCommand;
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
    private KillstreakMilestoneAnnouncer milestoneAnnouncer;

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

    /**
     * @return milestone announcer
     */
    public KillstreakMilestoneAnnouncer getMilestoneAnnouncer() {
        return milestoneAnnouncer;
    }

    @Override
    public void onEnable() {
        this.foliaLib = new FoliaLib(this);
        this.dataManager = new PlayerDataManager(getDataFolder());
        // Save default config
        saveDefaultConfig();
        this.milestoneAnnouncer = new KillstreakMilestoneAnnouncer(KillstreakMilestoneConfigSection.load(this));

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new KillListener(dataManager, milestoneAnnouncer), this);
        pm.registerEvents(new PlayerConnectionListener(this, dataManager), this);
        // Register /ks command
        this.getCommand("ks").setExecutor(new KillStreakCommand(this));
        // Register /selftestkillstreaks command
        this.getCommand("selftestkillstreaks").setExecutor(new SelfTestCommand(this));

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
