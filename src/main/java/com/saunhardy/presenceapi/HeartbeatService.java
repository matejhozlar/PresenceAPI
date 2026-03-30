package com.saunhardy.presenceapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically sends the full online player list to the backend
 * so it can reconcile stale sessions from missed leave events.
 */
public class HeartbeatService {
    private final ScheduledExecutorService scheduler;
    private final ApiClient apiClient;
    private volatile MinecraftServer server;

    public HeartbeatService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PresenceAPI-Heartbeat");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Starts the heartbeat loop. Called once the server is available.
     */
    public void start(MinecraftServer server) {
        this.server = server;

        int intervalMinutes = Config.HEARTBEAT_INTERVAL_MINUTES.get();
        if (intervalMinutes <= 0) {
            presenceAPI.LOGGER.info("Heartbeat disabled (interval = 0)");
            return;
        }

        scheduler.scheduleAtFixedRate(
                this::sendHeartbeat,
                intervalMinutes,
                intervalMinutes,
                TimeUnit.MINUTES
        );

        presenceAPI.LOGGER.info("Heartbeat scheduled every {} minute(s)", intervalMinutes);
    }

    private void sendHeartbeat() {
        if (!Config.ENABLED.get() || server == null) {
            return;
        }

        try {
            List<ServerPlayer> players = server.getPlayerList().getPlayers();

            JsonArray playersArray = new JsonArray();
            for (ServerPlayer player : players) {
                JsonObject obj = new JsonObject();
                obj.addProperty("uuid", player.getStringUUID());
                obj.addProperty("username", player.getGameProfile().getName());
                playersArray.add(obj);
            }

            JsonObject payload = new JsonObject();
            payload.add("players", playersArray);
            payload.addProperty("timestamp", System.currentTimeMillis());

            String configServerId = Config.SERVER_ID.get();
            if (!configServerId.isEmpty()) {
                payload.addProperty("serverId", configServerId);
            }

            apiClient.sendHeartbeat(payload);
        } catch (Exception e) {
            if (Config.LOG_ERRORS.get()) {
                presenceAPI.LOGGER.error("Failed to build heartbeat payload", e);
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
