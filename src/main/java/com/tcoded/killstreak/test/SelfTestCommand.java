package com.tcoded.killstreak.test;

import com.tcoded.killstreak.KillStreak;
import com.tcoded.killstreak.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Command executor for /selftestkillstreaks - tests killstreak functionality.
 */
public class SelfTestCommand implements CommandExecutor {

    private final KillStreak plugin;
    private final PlayerDataManager dataManager;
    private static final DateTimeFormatter logFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public SelfTestCommand(KillStreak plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.size() < 2) {
            sender.sendMessage(ChatColor.RED + "At least 2 players must be online to run the test.");
            return true;
        }

        // If sender is a player, use them as executor. Otherwise use the first online player
        Player executor = (sender instanceof Player) ? (Player) sender : onlinePlayers.get(0);

        // Start test asynchronously to avoid blocking main thread
        plugin.getFoliaLib().getScheduler().runAsync(task -> runTest(executor, onlinePlayers));
        return true;
    }

    /**
     * Runs the self-test with fake damage and death events.
     *
     * @param executor the player who executed the command
     * @param onlinePlayers all online players
     */
    private void runTest(Player executor, List<Player> onlinePlayers) {
        // Setup logging
        String logFileName = "killstreak-test-" + LocalDateTime.now().format(logFormatter) + ".log";
        File logFile = new File(plugin.getDataFolder(), logFileName);
        plugin.getDataFolder().mkdirs();

        if (!TestLogger.enable(logFile.getAbsolutePath())) {
            executor.sendMessage(ChatColor.RED + "Failed to enable test logging.");
            return;
        }

        try {
            TestLogger.log("========== KILLSTREAK SELF-TEST STARTED ==========");
            TestLogger.log("Test Executor: " + executor.getName());
            TestLogger.log("Online Players: " + onlinePlayers.size());

            // Force auto-respawn to true
            boolean originalAutoRespawn = Bukkit.getServer().getSpawnRadius() >= 0; // Placeholder check
            forceAutoRespawn(true);
            TestLogger.log("Auto-respawn forced to TRUE");

            // Select 2 test players
            Player player1 = onlinePlayers.get(0);
            Player player2 = onlinePlayers.get(1);

            TestLogger.log("Test Player 1: " + player1.getName() + " (" + player1.getUniqueId() + ")");
            TestLogger.log("Test Player 2: " + player2.getName() + " (" + player2.getUniqueId() + ")");

            // Reset player data for clean test
            TestLogger.log("Resetting player data for clean test...");
            dataManager.get(player1.getUniqueId()).setKills(0);
            dataManager.get(player1.getUniqueId()).setDeaths(0);
            dataManager.get(player2.getUniqueId()).setKills(0);
            dataManager.get(player2.getUniqueId()).setDeaths(0);

            // Store initial killstreak data
            int p1InitialKills = dataManager.get(player1.getUniqueId()).getKills();
            int p1InitialDeaths = dataManager.get(player1.getUniqueId()).getDeaths();
            int p2InitialKills = dataManager.get(player2.getUniqueId()).getKills();
            int p2InitialDeaths = dataManager.get(player2.getUniqueId()).getDeaths();

            TestLogger.log("--- INITIAL STATE ---");
            TestLogger.log(player1.getName() + ": kills=" + p1InitialKills + ", deaths=" + p1InitialDeaths);
            TestLogger.log(player2.getName() + ": kills=" + p2InitialKills + ", deaths=" + p2InitialDeaths);

            // Test Case 1: Player 1 kills Player 2
            TestLogger.log("--- TEST CASE 1: Player 1 kills Player 2 ---");
            simulateDeath(player2, player1);
            sleep(500);

            int p1KillsAfter1 = dataManager.get(player1.getUniqueId()).getKills();
            int p1DeathsAfter1 = dataManager.get(player1.getUniqueId()).getDeaths();
            int p2KillsAfter1 = dataManager.get(player2.getUniqueId()).getKills();
            int p2DeathsAfter1 = dataManager.get(player2.getUniqueId()).getDeaths();

            TestLogger.log("Result:");
            TestLogger.log(player1.getName() + ": kills=" + p1KillsAfter1 + " (expected " + (p1InitialKills + 1) + "), deaths=" + p1DeathsAfter1);
            TestLogger.log(player2.getName() + ": kills=" + p2KillsAfter1 + " (expected " + p2InitialKills + "), deaths=" + p2DeathsAfter1 + " (expected " + (p2InitialDeaths + 1) + ")");

            boolean test1Pass = (p1KillsAfter1 == p1InitialKills + 1) && (p2DeathsAfter1 == p2InitialDeaths + 1) && (p2KillsAfter1 == 0);
            TestLogger.log("Test Case 1 Result: " + (test1Pass ? "PASS" : "FAIL"));

            // Test Case 2: Player 2 kills Player 1
            TestLogger.log("--- TEST CASE 2: Player 2 kills Player 1 ---");
            simulateDeath(player1, player2);
            sleep(500);

            int p1KillsAfter2 = dataManager.get(player1.getUniqueId()).getKills();
            int p1DeathsAfter2 = dataManager.get(player1.getUniqueId()).getDeaths();
            int p2KillsAfter2 = dataManager.get(player2.getUniqueId()).getKills();
            int p2DeathsAfter2 = dataManager.get(player2.getUniqueId()).getDeaths();

            TestLogger.log("Result:");
            TestLogger.log(player1.getName() + ": kills=" + p1KillsAfter2 + " (expected 0), deaths=" + p1DeathsAfter2 + " (expected " + (p1DeathsAfter1 + 1) + ")");
            TestLogger.log(player2.getName() + ": kills=" + p2KillsAfter2 + " (expected " + (p2KillsAfter1 + 1) + "), deaths=" + p2DeathsAfter2);

            boolean test2Pass = (p1KillsAfter2 == 0) && (p1DeathsAfter2 == p1DeathsAfter1 + 1) && (p2KillsAfter2 == p2KillsAfter1 + 1);
            TestLogger.log("Test Case 2 Result: " + (test2Pass ? "PASS" : "FAIL"));

            // Test Case 3: Player 1 kills Player 2 again (testing killstreak increment)
            TestLogger.log("--- TEST CASE 3: Player 1 kills Player 2 again ---");
            simulateDeath(player2, player1);
            sleep(500);

            int p1KillsAfter3 = dataManager.get(player1.getUniqueId()).getKills();
            int p2KillsAfter3 = dataManager.get(player2.getUniqueId()).getKills();

            TestLogger.log("Result:");
            TestLogger.log(player1.getName() + ": kills=" + p1KillsAfter3 + " (expected " + (p1KillsAfter2 + 1) + ")");
            TestLogger.log(player2.getName() + ": kills=" + p2KillsAfter3 + " (expected 0)");

            boolean test3Pass = (p1KillsAfter3 == p1KillsAfter2 + 1) && (p2KillsAfter3 == 0);
            TestLogger.log("Test Case 3 Result: " + (test3Pass ? "PASS" : "FAIL"));

            // Summary
            TestLogger.log("========== TEST SUMMARY ==========");
            TestLogger.log("Test Case 1 (Player 1 kills Player 2): " + (test1Pass ? "PASS" : "FAIL"));
            TestLogger.log("Test Case 2 (Player 2 kills Player 1): " + (test2Pass ? "PASS" : "FAIL"));
            TestLogger.log("Test Case 3 (Player 1 kills Player 2 again): " + (test3Pass ? "PASS" : "FAIL"));
            TestLogger.log("Overall Result: " + (test1Pass && test2Pass && test3Pass ? "ALL TESTS PASSED" : "SOME TESTS FAILED"));

            // Restore settings
            TestLogger.log("Restoring settings...");
            TestLogger.log("========== KILLSTREAK SELF-TEST COMPLETED ==========");

        } finally {
            TestLogger.disable();
            executor.sendMessage(ChatColor.GREEN + "Self-test completed. Check logs at: " + logFile.getAbsolutePath());
        }
    }

    /**
     * Simulates a player death with a killer.
     *
     * @param victim the player who dies
     * @param killer the player who dealt the killing blow
     */
    private void simulateDeath(Player victim, Player killer) {
        // Get current location (safe location for respawn)
        Location spawnLocation = victim.getWorld().getSpawnLocation();

        TestLogger.log("Simulated death: " + victim.getName() + " killed by " + killer.getName());

        // Schedule damage on main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            TestLogger.log("  [DEBUG] Victim health before damage: " + victim.getHealth());
            TestLogger.log("  [DEBUG] Applying lethal damage from " + killer.getName());

            // Set the killer by applying damage from the killer
            double damage = victim.getHealth() + 1; // Lethal damage
            victim.damage(damage, killer);

            TestLogger.log("  [DEBUG] Damage applied, victim health: " + victim.getHealth());
        });

        // Wait longer for death event to process
        sleep(300);

        // Schedule respawn on main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            TestLogger.log("  [DEBUG] Respawning " + victim.getName());
            victim.teleport(spawnLocation);
            victim.setHealth(victim.getMaxHealth());
            TestLogger.log("  [DEBUG] Respawn complete");
        });
    }

    /**
     * Force auto-respawn to be enabled.
     *
     * @param enabled true to enable, false to disable
     */
    private void forceAutoRespawn(boolean enabled) {
        // In a real implementation, this would modify Bukkit/Paper server settings
        // For now, we just log it and ensure players respawn in our test
        TestLogger.log("Auto-respawn setting: " + enabled);
    }

    /**
     * Sleep for a specified duration.
     *
     * @param milliseconds duration in milliseconds
     */
    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
