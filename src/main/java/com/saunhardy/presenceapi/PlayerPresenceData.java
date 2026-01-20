package com.saunhardy.presenceapi;


import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class PlayerPresenceData {
    private final String minecraftUsername;
    private final String uuid;
    private final String state;
    private final String displayName;
    private final String gamemode;
    private final String dimension;
    private final Double x;
    private final Double y;
    private final Double z;
    private final Float health;
    private final Integer experienceLevel;
    private final String ipAddress;
    private final long timestamp;

    private PlayerPresenceData(Builder builder) {
        this.minecraftUsername = builder.minecraftUsername;
        this.uuid = builder.uuid;
        this.state = builder.state;
        this.displayName = builder.displayName;
        this.gamemode = builder.gamemode;
        this.dimension = builder.dimension;
        this.x = builder.x;
        this.y = builder.y;
        this.z = builder.z;
        this.health = builder.health;
        this.experienceLevel = builder.experienceLevel;
        this.ipAddress = builder.ipAddress;
        this.timestamp = System.currentTimeMillis();
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        json.addProperty("minecraftUsername", minecraftUsername);
        json.addProperty("uuid", uuid);
        json.addProperty("state", state);
        json.addProperty("timestamp", timestamp);

        if (displayName != null) json.addProperty("displayName", displayName);
        if (gamemode != null) json.addProperty("gamemode", gamemode);
        if (dimension != null) json.addProperty("dimension", dimension);
        if (ipAddress != null) json.addProperty("ipAddress", ipAddress);
        if (experienceLevel != null) json.addProperty("experienceLevel", experienceLevel);
        if (health != null) json.addProperty("health", health);

        if (x != null && y != null && z != null) {
            JsonObject position = new JsonObject();
            position.addProperty("x", x);
            position.addProperty("y", y);
            position.addProperty("z", z);
            json.add("position", position);
        }

        return json;
    }

    public static Builder fromPlayer(ServerPlayer player, String state) {
        return new Builder()
                .minecraftUsername(player.getGameProfile().getName())
                .uuid(player.getStringUUID())
                .state(state);
    }

    public static class Builder {
        private String minecraftUsername;
        private String uuid;
        private String state;
        private String displayName;
        private String gamemode;
        private String dimension;
        private Double x;
        private Double y;
        private Double z;
        private Float health;
        private Integer experienceLevel;
        private String ipAddress;

        public Builder minecraftUsername(String minecraftUsername) {
            this.minecraftUsername = minecraftUsername;
            return this;
        }

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder displayName(String displayName) {
            if (Config.SEND_DISPLAY_NAME.get()) {
                this.displayName = displayName;
            }
            return this;
        }

        public Builder gamemode(GameType gamemode) {
            if (Config.SEND_GAMEMODE.get()) {
                this.gamemode = gamemode.getName();
            }
            return this;
        }

        public Builder dimension(String dimension) {
            if (Config.SEND_DIMENSION.get()) {
                this.dimension = dimension;
            }
            return this;
        }

        public Builder position(double x, double y, double z) {
            if (Config.SEND_POSITION.get()) {
                this.x = x;
                this.y = y;
                this.z = z;
            }
            return this;
        }

        public Builder health(float health) {
            if (Config.SEND_HEALTH.get()) {
                this.health = health;
            }
            return this;
        }

        public Builder experienceLevel(int level) {
            if (Config.SEND_EXPERIENCE_LEVEL.get()) {
                this.experienceLevel = level;
            }
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            if (Config.SEND_PLAYER_IP.get()) {
                this.ipAddress = ipAddress;
            }
            return this;
        }

        public PlayerPresenceData build() {
            return new PlayerPresenceData(this);
        }
    }
}
