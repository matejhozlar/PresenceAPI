package com.saunhardy.presenceapi;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // API Configuration
    public static final ModConfigSpec.ConfigValue<String> API_URL;
    public static final ModConfigSpec.ConfigValue<String> JWT_SECRET;
    public static final ModConfigSpec.ConfigValue<String> SERVER_ID;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.ConfigValue<String> PRESENCE_ENDPOINT;
    public static final ModConfigSpec.ConfigValue<String> HEARTBEAT_ENDPOINT;

    // Data Configuration
    public static final ModConfigSpec.BooleanValue SEND_DISPLAY_NAME;
    public static final ModConfigSpec.BooleanValue SEND_PLAYER_IP;
    public static final ModConfigSpec.BooleanValue SEND_GAMEMODE;
    public static final ModConfigSpec.BooleanValue SEND_DIMENSION;
    public static final ModConfigSpec.BooleanValue SEND_POSITION;
    public static final ModConfigSpec.BooleanValue SEND_HEALTH;
    public static final ModConfigSpec.BooleanValue SEND_EXPERIENCE_LEVEL;

    // Heartbeat Configuration
    public static final ModConfigSpec.IntValue HEARTBEAT_INTERVAL_MINUTES;

    // Logging Configuration
    public static final ModConfigSpec.BooleanValue LOG_REQUESTS;
    public static final ModConfigSpec.BooleanValue LOG_RESPONSES;
    public static final ModConfigSpec.BooleanValue LOG_ERRORS;

    static {
        BUILDER.comment("PresenceAPI Configuration").push("api");

        API_URL = BUILDER
                .comment("The base URL of the backend API (e.g. http://127.0.0.1:5000)")
                .define("apiUrl", "http://127.0.0.1:5000");

        PRESENCE_ENDPOINT = BUILDER
                .comment("The endpoint path for presence events (relative to the base URL)")
                .define("presenceEndpoint", "/api/presence");

        HEARTBEAT_ENDPOINT = BUILDER
                .comment("The endpoint path for heartbeat syncs (relative to the base URL)")
                .define("heartbeatEndpoint", "/api/presence/heartbeat");

        JWT_SECRET = BUILDER
                .comment("Secret key used to sign JWT tokens for API authentication")
                .define("jwtSecret", "your-secret-key-change-this");

        SERVER_ID = BUILDER
                .comment("Optional server identifier to include in all requests (useful for multi-server setups)")
                .define("serverId", "");

        ENABLED = BUILDER
                .comment("Enable or disable presence tracking system")
                .define("enabled", true);

        HEARTBEAT_INTERVAL_MINUTES = BUILDER
                .comment("Interval in minutes between heartbeat syncs (sends full player list to backend). Set to 0 to disable")
                .defineInRange("heartbeatIntervalMinutes", 5, 0, 60);

        BUILDER.pop();

        BUILDER.comment("Player Data Configuration").push("data");

        SEND_DISPLAY_NAME = BUILDER
                .comment("Include the player's display name (may differ from username)")
                .define("displayName", true);

        SEND_PLAYER_IP = BUILDER
                .comment("Include the player's IP address")
                .define("playerIp", false);

        SEND_GAMEMODE = BUILDER
                .comment("Include the player's current gamemode")
                .define("gamemode", true);

        SEND_DIMENSION = BUILDER
                .comment("Include the dimension the player is in")
                .define("dimension", true);

        SEND_POSITION = BUILDER
                .comment("Include the player's coordinates")
                .define("position", true);

        SEND_HEALTH = BUILDER
                .comment("Include the player's health")
                .define("health", false);

        SEND_EXPERIENCE_LEVEL = BUILDER
                .comment("Include the player's experience level")
                .define("experienceLevel", true);

        BUILDER.pop();

        BUILDER.comment("Logging Configuration").push("logging");

        LOG_REQUESTS = BUILDER
                .comment("Log all outgoing API requests")
                .define("logRequests", true);

        LOG_RESPONSES = BUILDER
                .comment("Log all API responses")
                .define("logResponses", false);

        LOG_ERRORS = BUILDER
                .comment("Log all errors")
                .define("logErrors", true);
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
