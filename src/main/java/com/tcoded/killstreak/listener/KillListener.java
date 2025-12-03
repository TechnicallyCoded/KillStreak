package com.tcoded.killstreak.listener;

import com.tcoded.killstreak.data.PlayerData;
import com.tcoded.killstreak.data.PlayerDataManager;
import com.tcoded.killstreak.milestone.KillstreakMilestoneAnnouncer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

/**
 * Handles kill and death tracking.
 */
public class KillListener implements Listener {

    private final PlayerDataManager manager;
    private final KillstreakMilestoneAnnouncer milestoneAnnouncer;

    /**
     * @param manager data manager instance
     */
    public KillListener(PlayerDataManager manager, KillstreakMilestoneAnnouncer milestoneAnnouncer) {
        this.manager = manager;
        this.milestoneAnnouncer = milestoneAnnouncer;
    }

    /**
     * Tracks kills and deaths when players die.
     *
     * @param event death event
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        UUID vId = victim.getUniqueId();
        PlayerData victimData = manager.get(vId);
        victimData.setDeaths(victimData.getDeaths() + 1);
        victimData.setKills(0);

        Player killer = victim.getKiller();
        if (killer != null) {
            UUID kId = killer.getUniqueId();
            PlayerData killerData = manager.get(kId);
            int newKills = killerData.getKills() + 1;
            killerData.setKills(newKills);
            milestoneAnnouncer.announceIfMilestone(killer, newKills);
        }
    }
}
