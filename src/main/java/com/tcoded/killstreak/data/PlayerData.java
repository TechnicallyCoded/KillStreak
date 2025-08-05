package com.tcoded.killstreak.data;

/**
 * Simple data holder for a player's kill statistics.
 */
public class PlayerData {

    private int kills;
    private int deaths;

    /**
     * Creates a new PlayerData object with zeroed stats.
     */
    public PlayerData() {
    }

    /**
     * @return current kill streak value
     */
    public int getKills() {
        return kills;
    }

    /**
     * @param kills new kill streak value
     */
    public void setKills(int kills) {
        this.kills = kills;
    }

    /**
     * @return total deaths
     */
    public int getDeaths() {
        return deaths;
    }

    /**
     * @param deaths total deaths
     */
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
}
