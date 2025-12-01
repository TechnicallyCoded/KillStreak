package com.tcoded.killstreak.command;

import com.tcoded.killstreak.KillStreak;
import com.tcoded.killstreak.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to show a player's current killstreak.
 */
public class KillStreakCommand implements CommandExecutor {

    private final KillStreak plugin;

    public KillStreakCommand(KillStreak plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        if (args.length != 0) {
            return false;
        }
        Player player = (Player) sender;
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        int ks = data.getKills();
        String template = plugin.getConfig().getString("messages.killstreak", "&aYour current killstreak is {killstreak}!");
        String message = ChatColor.translateAlternateColorCodes('&', template.replace("{killstreak}", String.valueOf(ks)));
        player.sendMessage(message);
        return true;
    }
}

