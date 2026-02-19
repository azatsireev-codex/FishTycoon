package com.fishtycoon.model;

import java.util.UUID;

public record LeaderboardEntry(UUID uuid, String name, int value) {
}
