package com.tcoded.killstreak.milestone;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Announces milestone messages when a player reaches configured streaks.
 */
public class KillstreakMilestoneAnnouncer {

    private final List<KillstreakMilestone> milestones;
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;

    /**
     * @param milestones configured milestones
     */
    public KillstreakMilestoneAnnouncer(List<KillstreakMilestone> milestones) {
        this.milestones = milestones;
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.legacySection();
    }

    /**
     * Announces a message if the given kill count matches a configured milestone.
     *
     * @param player     killer
     * @param killCount  current kill count
     */
    public void announceIfMilestone(Player player, int killCount) {
        if (milestones.isEmpty()) {
            return;
        }

        for (KillstreakMilestone milestone : milestones) {
            if (milestone.kills() != killCount) {
                continue;
            }
            broadcast(player, killCount, milestone.message());
            return;
        }
    }

    private void broadcast(Player player, int killCount, String template) {
        Component message = miniMessage.deserialize(
                template,
                Placeholder.parsed("player", player.getName()),
                Placeholder.parsed("killstreak", String.valueOf(killCount))
        );
        broadcastToPlayers(message);
    }

    private void broadcastToPlayers(Component message) {
        String legacyMessage = legacySerializer.serialize(message);
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(legacyMessage);
        }
    }
}
