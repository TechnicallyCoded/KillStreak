package com.tcoded.killstreak.config;

import com.tcoded.killstreak.milestone.KillstreakMilestone;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Reads the killstreak milestone section from the plugin configuration.
 */
public final class KillstreakMilestoneConfigSection {

    private static final String PATH = "killstreak-milestones";

    private KillstreakMilestoneConfigSection() {
    }

    /**
     * Loads milestones from the configuration.
     *
     * @param plugin plugin instance
     * @return configured milestones
     */
    public static List<KillstreakMilestone> load(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        plugin.getLogger().info("[KillStreak-Config] Loading milestones from config. Config keys: " + config.getKeys(false));
        ConfigurationSection section = config.getConfigurationSection(PATH);
        if (section == null) {
            plugin.getLogger().info("[KillStreak-Config] Section '" + PATH + "' not found in config!");
            return Collections.emptyList();
        }
        plugin.getLogger().info("[KillStreak-Config] Found milestone section with keys: " + section.getKeys(false));

        List<KillstreakMilestone> milestones = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection entrySection = section.getConfigurationSection(key);
            KillstreakMilestoneConfigEntry entry = deserialize(key, entrySection);
            if (entry == null) {
                continue;
            }
            milestones.add(toMilestone(entry));
        }
        return milestones;
    }

    private static KillstreakMilestoneConfigEntry deserialize(String key, ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        int streak = section.getInt("streak");
        if (streak <= 0) {
            return null;
        }

        String message = section.getString("message");
        if (message == null || message.isEmpty()) {
            return null;
        }

        List<String> rewards = section.getStringList("rewards");
        List<String> cleanedRewards = new ArrayList<>();
        for (String reward : rewards) {
            if (reward == null || reward.isEmpty()) {
                continue;
            }
            cleanedRewards.add(reward);
        }

        return new KillstreakMilestoneConfigEntry(key, streak, message, cleanedRewards);
    }

    private static KillstreakMilestone toMilestone(KillstreakMilestoneConfigEntry entry) {
        List<String> rewards = new ArrayList<>();
        for (String reward : entry.rewards()) {
            rewards.add(Objects.requireNonNull(reward));
        }
        return new KillstreakMilestone(entry.streak(), entry.message(), Collections.unmodifiableList(rewards));
    }

    private record KillstreakMilestoneConfigEntry(String key, int streak, String message, List<String> rewards) {
    }
}
