package com.saunhardy.presenceapi;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // API Configuration
    public static final ModConfigSpec.ConfigValue<String> API_ENDPOINT;
    public static final ModConfigSpec.ConfigValue<String> JWT_SECRET;
    public static final ModConfigSpec.BooleanValue ENABLED;

    // Data Configuration
    public static final ModConfigSpec.BooleanValue SEND_DISPLAY_NAME;
    public static final ModConfigSpec.BooleanValue SEND_UUID;
    public static final ModConfigSpec.BooleanValue SEND_PLAYER_IP;
    public static final ModConfigSpec.BooleanValue SEND_GAMEMODE;
    public static final ModConfigSpec.BooleanValue SEND_DIMENSION;
    public static final ModConfigSpec.BooleanValue SEND_POSITION;
    public static final ModConfigSpec.BooleanValue SEND_HEALTH;
    public static final ModConfigSpec.BooleanValue SEND_EXPERIENCE_LEVEL;

    // Network Configuration
    public static final ModConfigSpec.IntValue TIMEOUT_SECONDS;
    public static final ModConfigSpec.BooleanValue RETRY_ON_FAILURE;
    public static final ModConfigSpec.IntValue MAX_RETRIES;

    // Logging Configuration
    public static final ModConfigSpec.BooleanValue LOG_REQUESTS;
    public static final ModConfigSpec.BooleanValue LOG_RESPONSES;
    public static final ModConfigSpec.BooleanValue LOG_ERRORS;

    static {
        BUILDER.comment("PresenceAPI Configuration").push("api");

        API_ENDPOINT = BUILDER
                .comment("The HTTP(S) endpoint to send player presence data to")
                .define("endpoint", "http://localhost:5000/api/presence");

        JWT_SECRET = BUILDER
                .comment("Secret key used to sign JWT tokens for API authentication")
                .define("jwtSecret", "your-secret-key-change-this");

        ENABLED = BUILDER
                .comment("Enable or disable presence tracking system")
                .define("enabled", true);

        BUILDER.pop();

        BUILDER.comment("Player Data Configuration").push("data");

        SEND_DISPLAY_NAME = BUILDER
                .comment("Include the player's display name (may differ from username)")
                .define("displayName", true);

        SEND_UUID = BUILDER
                .comment("Include the player's UUID")
                .define("uuid", true);

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

        BUILDER.comment("Network Configuration").push("network");

        TIMEOUT_SECONDS = BUILDER
                .comment("HTTP request timeout in seconds")
                .defineInRange("timeoutSeconds", 5, 1, 60);

        RETRY_ON_FAILURE = BUILDER
                .comment("Retry failed requests")
                .define("retry", true);

        MAX_RETRIES = BUILDER
                .comment("Maximum number of retry attempts")
                .defineInRange("maxRetries", 3, 0 ,10);

        BUILDER.pop();

        BUILDER.comment("Loggin Configuration").push("logging");

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