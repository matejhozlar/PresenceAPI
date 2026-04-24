package com.saunhardy.presenceapi;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // API Configuration
    public static final ModConfigSpec.ConfigValue<String> API_URL;
    public static final ModConfigSpec.ConfigValue<String> JWT_SECRET;
    public static final ModConfigSpec.ConfigValue<String> SERVER_ID;
    public static final ModConfigSpec.BooleanValue ENABLED;

    // Data Configuration
    public static final ModConfigSpec.BooleanValue SEND_DIMENSION;
    public static final ModConfigSpec.BooleanValue SEND_POSITION;

    // Heartbeat Configuration
    public static final ModConfigSpec.IntValue HEARTBEAT_INTERVAL_MINUTES;

    // Logging Configuration
    public static final ModConfigSpec.BooleanValue LOG_REQUESTS;

    static {
        BUILDER.comment("PresenceAPI Configuration").push("api");

        API_URL = BUILDER
                .comment("The base URL of the backend API (e.g. http://127.0.0.1:5000). Requires server restart to take effect")
                .define("apiUrl", "http://127.0.0.1:5000");

        JWT_SECRET = BUILDER
                .comment("Secret key used to sign JWT tokens for API authentication. Requires server restart to take effect")
                .define("jwtSecret", "CHANGE-ME-must-be-at-least-32-chars");

        SERVER_ID = BUILDER
                .comment("Optional server identifier to include in all requests (useful for multi-server setups)")
                .define("serverId", "");

        ENABLED = BUILDER
                .comment("Enable or disable presence tracking system. Requires server restart to take effect")
                .define("enabled", true);

        HEARTBEAT_INTERVAL_MINUTES = BUILDER
                .comment("Interval in minutes between heartbeat syncs (sends full player list to backend). Set to 0 to disable. Requires server restart to take effect")
                .defineInRange("heartbeatIntervalMinutes", 5, 0, 60);

        BUILDER.pop();

        BUILDER.comment("Player Data Configuration").push("data");

        SEND_DIMENSION = BUILDER
                .comment("Include the dimension the player is in")
                .define("dimension", true);

        SEND_POSITION = BUILDER
                .comment("Include the player's coordinates")
                .define("position", true);

        BUILDER.pop();

        BUILDER.comment("Logging Configuration").push("logging");

        LOG_REQUESTS = BUILDER
                .comment("Log all outgoing API requests")
                .define("logRequests", true);

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
