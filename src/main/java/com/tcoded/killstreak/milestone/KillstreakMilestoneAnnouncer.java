package com.tcoded.killstreak.milestone;

import com.tcoded.killstreak.test.TestLogger;
import com.tcoded.killstreak.util.PlaceholderUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Announces milestone messages when a player reaches configured streaks.
 */
public class KillstreakMilestoneAnnouncer {

    private final List<KillstreakMilestone> milestones;
    private final MiniMessage miniMessage;

    /**
     * @param milestones configured milestones
     */
    public KillstreakMilestoneAnnouncer(List<KillstreakMilestone> milestones) {
        this.milestones = milestones;
        this.miniMessage = MiniMessage.miniMessage();
    }

    /**
     * Announces a message if the given kill count matches a configured milestone.
     *
     * @param player     killer
     * @param killCount  current kill count
     */
    public void announceIfMilestone(Player player, int killCount) {
        if (milestones.isEmpty()) {
            TestLogger.logTestEnv("[KillStreak-Milestone] No milestones configured");
            return;
        }

        TestLogger.logTestEnv("[KillStreak-Milestone] Checking milestone for " + player.getName() + " with " + killCount + " kills");
        for (KillstreakMilestone milestone : milestones) {
            TestLogger.logTestEnv("[KillStreak-Milestone] Comparing: kills=" + milestone.kills() + " vs current=" + killCount);
            if (milestone.kills() != killCount) {
                continue;
            }
            TestLogger.logTestEnv("[KillStreak-Milestone] MILESTONE TRIGGERED! Announcing and rewarding...");
            broadcast(player, killCount, milestone.message());
            reward(player, killCount, milestone.rewards());
            return;
        }
    }

    private void broadcast(Player player, int killCount, String template) {
        String parsedTemplate = PlaceholderUtil.parsePlaceholders(player, template);
        String filledTemplate = parsedTemplate.replace("{killstreak}", String.valueOf(killCount));
        Component message = miniMessage.deserialize(
                filledTemplate,
                Placeholder.parsed("player", player.getName())
        );
        broadcastToPlayers(message);
    }

    private void broadcastToPlayers(Component message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(message);
        }
    }

    private void reward(Player player, int killCount, List<String> rewards) {
        if (rewards == null || rewards.isEmpty()) {
            return;
        }

        CommandSender console = Bukkit.getConsoleSender();
        for (String reward : rewards) {
            if (reward == null || reward.isEmpty()) {
                continue;
            }
            String parsedReward = PlaceholderUtil.parsePlaceholders(player, reward);
            String filledReward = parsedReward.replace("{killstreak}", String.valueOf(killCount));
            TestLogger.logTestEnv("[KillStreak-Milestone] Executing reward command: " + filledReward);
            Bukkit.dispatchCommand(console, filledReward);
        }
    }
}
