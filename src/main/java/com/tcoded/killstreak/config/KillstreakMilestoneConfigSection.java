package com.tcoded.killstreak.config;

import com.tcoded.killstreak.milestone.KillstreakMilestone;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<Map<?, ?>> rawEntries = config.getMapList(PATH);
        List<KillstreakMilestone> milestones = new ArrayList<>();
        for (Map<?, ?> entry : rawEntries) {
            KillstreakMilestone milestone = deserialize(entry);
            if (milestone == null) {
                continue;
            }
            milestones.add(milestone);
        }
        return milestones;
    }

    private static KillstreakMilestone deserialize(Map<?, ?> entry) {
        if (entry == null || entry.isEmpty()) {
            return null;
        }

        Object killsObject = entry.get("kills");
        Object messageObject = entry.get("message");
        if (!(killsObject instanceof Number) || !(messageObject instanceof String)) {
            return null;
        }

        int kills = ((Number) killsObject).intValue();
        if (kills <= 0) {
            return null;
        }

        String message = (String) messageObject;
        if (message.isEmpty()) {
            return null;
        }

        return new KillstreakMilestone(kills, message);
    }
}
