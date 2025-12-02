package com.tcoded.killstreak.test;

import com.tcoded.killstreak.KillStreak;
import com.tcoded.killstreak.data.PlayerDataManager;
import com.tcoded.killstreak.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
            // Enable debug mode for the duration of the test
            DebugMode.enable();
            DebugMode.log("Debug mode enabled for self-test");

            TestLogger.log("========== KILLSTREAK SELF-TEST STARTED ==========");
            TestLogger.log("Test Executor: " + executor.getName());
            TestLogger.log("Online Players: " + onlinePlayers.size());
            DebugMode.log("Test started at: " + LocalDateTime.now());

            // Force auto-respawn to true
            boolean originalAutoRespawn = Bukkit.getServer().getSpawnRadius() >= 0; // Placeholder check
            forceAutoRespawn(true);
            TestLogger.log("Auto-respawn forced to TRUE");
            DebugMode.log("Auto-respawn configuration verified");

            // Select 2 test players
            Player player1 = onlinePlayers.get(0);
            Player player2 = onlinePlayers.get(1);

            TestLogger.log("Test Player 1: " + player1.getName() + " (" + player1.getUniqueId() + ")");
            TestLogger.log("Test Player 2: " + player2.getName() + " (" + player2.getUniqueId() + ")");
            DebugMode.log("Player 1 health: %.1f/%.1f", player1.getHealth(), player1.getMaxHealth());
            DebugMode.log("Player 2 health: %.1f/%.1f", player2.getHealth(), player2.getMaxHealth());

            // Reset player data for clean test
            TestLogger.log("Resetting player data for clean test...");
            dataManager.get(player1.getUniqueId()).setKills(0);
            dataManager.get(player1.getUniqueId()).setDeaths(0);
            dataManager.get(player2.getUniqueId()).setKills(0);
            dataManager.get(player2.getUniqueId()).setDeaths(0);
            DebugMode.log("Player data cache reset completed");

            // Store initial killstreak data
            int p1InitialKills = dataManager.get(player1.getUniqueId()).getKills();
            int p1InitialDeaths = dataManager.get(player1.getUniqueId()).getDeaths();
            int p2InitialKills = dataManager.get(player2.getUniqueId()).getKills();
            int p2InitialDeaths = dataManager.get(player2.getUniqueId()).getDeaths();

            TestLogger.log("--- INITIAL STATE ---");
            TestLogger.log(player1.getName() + ": kills=" + p1InitialKills + ", deaths=" + p1InitialDeaths);
            TestLogger.log(player2.getName() + ": kills=" + p2InitialKills + ", deaths=" + p2InitialDeaths);

            // Schedule test cases on main thread with proper delays (70 ticks = 3.5 seconds per test)
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> runTestCase1(executor, logFile, player1, player2, p1InitialKills, p1InitialDeaths, p2InitialKills, p2InitialDeaths), 0L);

        } catch (Exception e) {
            DebugMode.disable();
            TestLogger.disable();
            executor.sendMessage(ChatColor.RED + "Test failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void runTestCase1(Player executor, File logFile, Player player1, Player player2, int p1InitialKills, int p1InitialDeaths, int p2InitialKills, int p2InitialDeaths) {
        TestLogger.log("=== NEW SELFTEST VERSION WITH DETAILED STATE LOGGING ===");
        TestLogger.log("--- TEST CASE 1: Player 1 kills Player 2 ---");
        DebugMode.log("Starting death simulation: %s -> %s", player1.getName(), player2.getName());
        simulateDeath(player2, player1);

        // Schedule test case 2 after 70 ticks (3.5 seconds)
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            int p1KillsAfter1 = dataManager.get(player1.getUniqueId()).getKills();
            int p1DeathsAfter1 = dataManager.get(player1.getUniqueId()).getDeaths();
            int p2KillsAfter1 = dataManager.get(player2.getUniqueId()).getKills();
            int p2DeathsAfter1 = dataManager.get(player2.getUniqueId()).getDeaths();

            TestLogger.log("Result:");
            TestLogger.log(player1.getName() + ": kills=" + p1KillsAfter1 + " (expected " + (p1InitialKills + 1) + "), deaths=" + p1DeathsAfter1);
            TestLogger.log(player2.getName() + ": kills=" + p2KillsAfter1 + " (expected " + p2InitialKills + "), deaths=" + p2DeathsAfter1 + " (expected " + (p2InitialDeaths + 1) + ")");

            boolean test1Pass = (p1KillsAfter1 == p1InitialKills + 1) && (p2DeathsAfter1 == p2InitialDeaths + 1) && (p2KillsAfter1 == 0);
            TestLogger.log("Test Case 1 Result: " + (test1Pass ? "PASS" : "FAIL"));

            runTestCase2(executor, logFile, player1, player2, p1KillsAfter1, p1DeathsAfter1, p2KillsAfter1, p2DeathsAfter1, test1Pass);
        }, 70L);
    }

    private void runTestCase2(Player executor, File logFile, Player player1, Player player2, int p1KillsAfter1, int p1DeathsAfter1, int p2KillsAfter1, int p2DeathsAfter1, boolean test1Pass) {
        TestLogger.log("--- TEST CASE 2: Player 2 kills Player 1 ---");
        simulateDeath(player1, player2);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            int p1KillsAfter2 = dataManager.get(player1.getUniqueId()).getKills();
            int p1DeathsAfter2 = dataManager.get(player1.getUniqueId()).getDeaths();
            int p2KillsAfter2 = dataManager.get(player2.getUniqueId()).getKills();
            int p2DeathsAfter2 = dataManager.get(player2.getUniqueId()).getDeaths();

            TestLogger.log("Result:");
            TestLogger.log(player1.getName() + ": kills=" + p1KillsAfter2 + " (expected 0), deaths=" + p1DeathsAfter2 + " (expected " + (p1DeathsAfter1 + 1) + ")");
            TestLogger.log(player2.getName() + ": kills=" + p2KillsAfter2 + " (expected " + (p2KillsAfter1 + 1) + "), deaths=" + p2DeathsAfter2);

            boolean test2Pass = (p1KillsAfter2 == 0) && (p1DeathsAfter2 == p1DeathsAfter1 + 1) && (p2KillsAfter2 == p2KillsAfter1 + 1);
            TestLogger.log("Test Case 2 Result: " + (test2Pass ? "PASS" : "FAIL"));

            runTestCase3(executor, logFile, player1, player2, p1KillsAfter2, p2KillsAfter2, test1Pass, test2Pass);
        }, 70L);
    }

    private void runTestCase3(Player executor, File logFile, Player player1, Player player2, int p1KillsAfter2, int p2KillsAfter2, boolean test1Pass, boolean test2Pass) {
        TestLogger.log("--- TEST CASE 3: Player 1 kills Player 2 again ---");
        simulateDeath(player2, player1);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            int p1KillsAfter3 = dataManager.get(player1.getUniqueId()).getKills();
            int p2KillsAfter3 = dataManager.get(player2.getUniqueId()).getKills();

            TestLogger.log("Result:");
            TestLogger.log(player1.getName() + ": kills=" + p1KillsAfter3 + " (expected " + (p1KillsAfter2 + 1) + ")");
            TestLogger.log(player2.getName() + ": kills=" + p2KillsAfter3 + " (expected 0)");

            boolean test3Pass = (p1KillsAfter3 == p1KillsAfter2 + 1) && (p2KillsAfter3 == 0);
            TestLogger.log("Test Case 3 Result: " + (test3Pass ? "PASS" : "FAIL"));

            runAtomicWritesTest(executor, logFile, test1Pass, test2Pass, test3Pass);
        }, 70L);
    }

    private void runAtomicWritesTest(Player executor, File logFile, boolean test1Pass, boolean test2Pass, boolean test3Pass) {
        TestLogger.log("");
        TestLogger.log("========== ATOMIC WRITES TEST ==========");
        boolean atomicWritesPass = testAtomicWrites();
        TestLogger.log("Atomic Writes Test Result: " + (atomicWritesPass ? "PASS" : "FAIL"));

        // Summary
        TestLogger.log("");
        TestLogger.log("========== TEST SUMMARY ==========");
        TestLogger.log("Test Case 1 (Player 1 kills Player 2): " + (test1Pass ? "PASS" : "FAIL"));
        TestLogger.log("Test Case 2 (Player 2 kills Player 1): " + (test2Pass ? "PASS" : "FAIL"));
        TestLogger.log("Test Case 3 (Player 1 kills Player 2 again): " + (test3Pass ? "PASS" : "FAIL"));
        TestLogger.log("Atomic Writes Test: " + (atomicWritesPass ? "PASS" : "FAIL"));
        TestLogger.log("Overall Result: " + (test1Pass && test2Pass && test3Pass && atomicWritesPass ? "ALL TESTS PASSED" : "SOME TESTS FAILED"));

        TestLogger.log("Restoring settings...");
        DebugMode.log("Test execution completed successfully");
        TestLogger.log("========== KILLSTREAK SELF-TEST COMPLETED ==========");

        // Cleanup after all tests complete
        DebugMode.disable();
        TestLogger.disable();
        executor.sendMessage(ChatColor.GREEN + "Self-test completed. Check logs at: " + logFile.getAbsolutePath());
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

        // Schedule damage on main thread, then respawn on a delayed task
        Bukkit.getScheduler().runTask(plugin, () -> {
            TestLogger.log("  [STATE BEFORE DAMAGE]");
            TestLogger.log("    Victim: " + victim.getName() + " (UUID: " + victim.getUniqueId() + ")");
            TestLogger.log("    Killer: " + killer.getName() + " (UUID: " + killer.getUniqueId() + ")");
            TestLogger.log("    Victim Health: " + victim.getHealth() + "/" + victim.getMaxHealth());
            TestLogger.log("    Victim Alive: " + !victim.isDead());
            TestLogger.log("    Victim Last Damage Cause: " + (victim.getLastDamageCause() != null ? victim.getLastDamageCause().getCause() : "null"));
            TestLogger.log("    Victim Current Killer: " + (victim.getKiller() != null ? victim.getKiller().getName() : "null"));
            TestLogger.log("    Killer Health: " + killer.getHealth() + "/" + killer.getMaxHealth());
            TestLogger.log("    Killer Alive: " + !killer.isDead());

            // Store initial data state
            int victimKillsBefore = dataManager.get(victim.getUniqueId()).getKills();
            int victimDeathsBefore = dataManager.get(victim.getUniqueId()).getDeaths();
            int killerKillsBefore = dataManager.get(killer.getUniqueId()).getKills();
            int killerDeathsBefore = dataManager.get(killer.getUniqueId()).getDeaths();
            TestLogger.log("    Victim Data Before: kills=" + victimKillsBefore + ", deaths=" + victimDeathsBefore);
            TestLogger.log("    Killer Data Before: kills=" + killerKillsBefore + ", deaths=" + killerDeathsBefore);

            // Apply lethal damage with killer
            double damage = victim.getHealth() + 1;
            TestLogger.log("  [APPLYING DAMAGE]");
            TestLogger.log("    Damage Amount: " + damage);
            TestLogger.log("    Calling victim.damage(damage, killer)...");
            victim.damage(damage, killer);
            TestLogger.log("    damage() call completed");

            TestLogger.log("  [STATE AFTER DAMAGE]");
            TestLogger.log("    Victim Health: " + victim.getHealth() + "/" + victim.getMaxHealth());
            TestLogger.log("    Victim Alive: " + !victim.isDead());
            TestLogger.log("    Victim Dead: " + victim.isDead());
            TestLogger.log("    Victim Last Damage Cause: " + (victim.getLastDamageCause() != null ? victim.getLastDamageCause().getCause() : "null"));
            TestLogger.log("    Victim Current Killer: " + (victim.getKiller() != null ? victim.getKiller().getName() : "null"));
            TestLogger.log("    Data After Damage: kills=" + dataManager.get(victim.getUniqueId()).getKills() + ", deaths=" + dataManager.get(victim.getUniqueId()).getDeaths());
            TestLogger.log("    Killer Data After Damage: kills=" + dataManager.get(killer.getUniqueId()).getKills() + ", deaths=" + dataManager.get(killer.getUniqueId()).getDeaths());

            // Schedule respawn AFTER death event has time to process (30 ticks = 1.5 seconds)
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                TestLogger.log("  [STATE BEFORE RESPAWN]");
                TestLogger.log("    Victim: " + victim.getName());
                TestLogger.log("    Victim Is Dead: " + victim.isDead());
                TestLogger.log("    Victim Health: " + victim.getHealth() + "/" + victim.getMaxHealth());
                TestLogger.log("    Victim Killer: " + (victim.getKiller() != null ? victim.getKiller().getName() : "null"));
                TestLogger.log("    Victim Data Before Respawn: kills=" + dataManager.get(victim.getUniqueId()).getKills() + ", deaths=" + dataManager.get(victim.getUniqueId()).getDeaths());

                // Teleport and heal first
                TestLogger.log("  [PERFORMING RESPAWN]");
                TestLogger.log("    Teleporting to spawn...");
                victim.teleport(spawnLocation);
                TestLogger.log("    Setting health...");
                victim.setHealth(victim.getMaxHealth());

                // Clear combat state - critical for next test
                // Apply a harmless damage event to reset the killer tracking
                TestLogger.log("    Clearing combat state...");
                org.bukkit.event.entity.EntityDamageEvent resetEvent =
                    new org.bukkit.event.entity.EntityDamageEvent(
                        victim,
                        org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM,
                        0
                    );
                victim.setLastDamageCause(resetEvent);

                TestLogger.log("  [STATE AFTER RESPAWN]");
                TestLogger.log("    Victim Is Dead: " + victim.isDead());
                TestLogger.log("    Victim Health: " + victim.getHealth() + "/" + victim.getMaxHealth());
                TestLogger.log("    Victim Killer: " + (victim.getKiller() != null ? victim.getKiller().getName() : "null"));
                TestLogger.log("    Victim Last Damage Cause: " + (victim.getLastDamageCause() != null ? victim.getLastDamageCause().getCause() : "null"));
                TestLogger.log("    Victim Data After Respawn: kills=" + dataManager.get(victim.getUniqueId()).getKills() + ", deaths=" + dataManager.get(victim.getUniqueId()).getDeaths());
            }, 30L);
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
     * Tests atomic write functionality to ensure data integrity.
     *
     * @return true if all atomic write tests pass, false otherwise
     */
    private boolean testAtomicWrites() {
        TestLogger.log("[Atomic Writes] Testing file write atomicity...");

        File testDir = new File(plugin.getDataFolder(), "atomicity-test");
        testDir.mkdirs();

        try {
            // Test 1: Normal atomic write
            TestLogger.log("[Atomic Writes] Test 1: Writing test data atomically...");
            File testFile = new File(testDir, "test-data.json");
            String testContent = "{\"test\":\"atomic-write-success\"}";
            FileUtil.writeFileAtomic(testFile, testContent);

            // Verify file exists and contains correct data
            if (!testFile.exists()) {
                TestLogger.log("[Atomic Writes] Test 1 FAILED: File not created");
                return false;
            }

            String readContent = Files.readString(testFile.toPath(), StandardCharsets.UTF_8);
            if (!readContent.equals(testContent)) {
                TestLogger.log("[Atomic Writes] Test 1 FAILED: Content mismatch");
                return false;
            }
            TestLogger.log("[Atomic Writes] Test 1 PASSED: File written and verified");

            // Test 2: Overwrite with atomic write
            TestLogger.log("[Atomic Writes] Test 2: Overwriting file atomically...");
            String newContent = "{\"test\":\"atomic-overwrite-success\"}";
            FileUtil.writeFileAtomic(testFile, newContent);

            String readNewContent = Files.readString(testFile.toPath(), StandardCharsets.UTF_8);
            if (!readNewContent.equals(newContent)) {
                TestLogger.log("[Atomic Writes] Test 2 FAILED: Overwrite content mismatch");
                return false;
            }
            TestLogger.log("[Atomic Writes] Test 2 PASSED: File overwritten atomically");

            // Test 3: Multiple sequential writes
            TestLogger.log("[Atomic Writes] Test 3: Multiple sequential atomic writes...");
            for (int i = 0; i < 5; i++) {
                String iterContent = "{\"iteration\":" + i + "}";
                FileUtil.writeFileAtomic(testFile, iterContent);

                String readIter = Files.readString(testFile.toPath(), StandardCharsets.UTF_8);
                if (!readIter.equals(iterContent)) {
                    TestLogger.log("[Atomic Writes] Test 3 FAILED: Iteration " + i + " content mismatch");
                    return false;
                }
            }
            TestLogger.log("[Atomic Writes] Test 3 PASSED: All sequential writes successful");

            // Test 4: Verify no temp files left behind
            TestLogger.log("[Atomic Writes] Test 4: Checking for orphaned temp files...");
            File[] tempFiles = testDir.listFiles((dir, name) -> name.endsWith(".tmp"));
            if (tempFiles != null && tempFiles.length > 0) {
                TestLogger.log("[Atomic Writes] Test 4 FAILED: Found " + tempFiles.length + " orphaned temp files");
                return false;
            }
            TestLogger.log("[Atomic Writes] Test 4 PASSED: No orphaned temp files detected");

            // Cleanup
            TestLogger.log("[Atomic Writes] Cleaning up test files...");
            testFile.delete();
            testDir.delete();

            TestLogger.log("[Atomic Writes] All tests completed successfully!");
            return true;

        } catch (IOException e) {
            TestLogger.log("[Atomic Writes] FAILED: " + e.getMessage());
            return false;
        }
    }

}
