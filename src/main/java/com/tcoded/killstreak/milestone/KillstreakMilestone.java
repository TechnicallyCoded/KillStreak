package com.tcoded.killstreak.milestone;

import java.util.List;

/**
 * Represents a configurable killstreak milestone.
 */
public record KillstreakMilestone(int kills, String message, List<String> rewards) {
}
