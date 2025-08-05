package com.tcoded.killstreak.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tcoded.killstreak.data.PlayerData;

/**
 * Utility for serializing and deserializing {@link PlayerData} objects.
 */
public final class PlayerDataSerializer {

    private PlayerDataSerializer() {
    }

    /**
     * Serializes the given data to JSON.
     *
     * @param data player data
     * @return JSON representation
     */
    public static String serialize(PlayerData data) {
        JsonObject json = new JsonObject();
        json.addProperty("kills", data.getKills());
        json.addProperty("deaths", data.getDeaths());
        return json.toString();
    }

    /**
     * Deserializes JSON into a {@link PlayerData} instance.
     *
     * @param json JSON data
     * @return player data instance
     */
    public static PlayerData deserialize(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        int kills = obj.get("kills").getAsInt();
        int deaths = obj.get("deaths").getAsInt();
        PlayerData data = new PlayerData();
        data.setKills(kills);
        data.setDeaths(deaths);
        return data;
    }
}
