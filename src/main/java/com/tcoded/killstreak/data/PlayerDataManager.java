package com.tcoded.killstreak.data;

import com.tcoded.killstreak.util.FileUtil;
import com.tcoded.killstreak.util.PlayerDataSerializer;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages cached {@link PlayerData} instances and persistence.
 */
public class PlayerDataManager {

    private final ConcurrentMap<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final File dataFolder;

    /**
     * @param pluginFolder base plugin data folder
     */
    public PlayerDataManager(File pluginFolder) {
        this.dataFolder = new File(pluginFolder, "playerdata");
        this.dataFolder.mkdirs();
    }

    /**
     * Retrieves cached data for a player.
     *
     * @param uuid player UUID
     * @return player data or a new instance if none cached
     */
    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, u -> new PlayerData());
    }

    /**
     * Loads player data from disk into the cache.
     *
     * @param uuid player UUID
     */
    public void load(UUID uuid) {
        File file = getFile(uuid);
        try {
            String json = FileUtil.readFile(file);
            if (!json.isEmpty()) {
                cache.put(uuid, PlayerDataSerializer.deserialize(json));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves cached data to disk.
     *
     * @param uuid player UUID
     */
    public void save(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) {
            return;
        }
        File file = getFile(uuid);
        try {
            FileUtil.writeFile(file, PlayerDataSerializer.serialize(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a player's data from the cache.
     *
     * @param uuid player UUID
     */
    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    /**
     * Saves all cached player data.
     */
    public void saveAll() {
        cache.keySet().forEach(this::save);
    }

    private File getFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".json");
    }
}
