package com.saunhardy.presenceapi;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;

import java.util.List;

/**
 * JSON request DTOs for the backend, plus helpers that build the per-player
 * telemetry fields while honouring the {@code [data]} config toggles.
 * <p>
 * Defined locally (rather than reusing {@code createrington-api}) so fields can
 * be added freely. The presence event and the heartbeat roster share the same
 * per-player builders, so a player is described identically in both.
 * <p>
 * Nullable fields are omitted from the JSON when their toggle is off, because
 * the configured Gson skips nulls. These are read on the server thread (presence
 * events fire there; the heartbeat uses {@code payloadOn(server, ...)}).
 */
final class Payloads {

    private Payloads() {}

    // ── shared value objects ────────────────────────────────────────────
    record Position(double x, double y, double z) {}

    record Rotation(float yaw, float pitch) {}

    // ── presence event (join/leave): a single player ────────────────────
    record PresenceRequest(
            String minecraftUsername,
            String uuid,
            String state,
            Long timestamp,
            Integer serverId,
            Position position,
            String dimension,
            Rotation rotation,
            Integer experienceLevel,
            Float health,
            Integer ping,
            Integer playTimeTicks
    ) {}

    // ── heartbeat: the full online roster ───────────────────────────────
    record HeartbeatRequest(
            List<HeartbeatEntry> players,
            Integer serverId,
            Long timestamp
    ) {}

    // One player within a heartbeat — same per-player telemetry as a presence
    // event (minus {@code state}, which is implicitly "online" for everyone here).
    record HeartbeatEntry(
            String uuid,
            String minecraftUsername,
            Position position,
            String dimension,
            Rotation rotation,
            Integer experienceLevel,
            Float health,
            Integer ping,
            Integer playTimeTicks
    ) {}

    // ── per-player field builders (respect the [data] toggles) ──────────
    static Position position(ServerPlayer p) {
        return Config.SEND_POSITION.get() ? new Position(p.getX(), p.getY(), p.getZ()) : null;
    }

    static String dimension(ServerPlayer p) {
        return Config.SEND_DIMENSION.get() ? p.level().dimension().location().toString() : null;
    }

    static Rotation rotation(ServerPlayer p) {
        return Config.SEND_ROTATION.get() ? new Rotation(p.getYRot(), p.getXRot()) : null;
    }

    static Integer experienceLevel(ServerPlayer p) {
        return Config.SEND_EXPERIENCE.get() ? p.experienceLevel : null;
    }

    static Float health(ServerPlayer p) {
        return Config.SEND_HEALTH.get() ? p.getHealth() : null;
    }

    static Integer ping(ServerPlayer p) {
        return Config.SEND_PING.get() ? p.connection.latency() : null;
    }

    // The vanilla play_time stat only advances while the server awards it, so
    // a mod that pauses it (e.g. while the player is AFK) is reflected here.
    static Integer playTimeTicks(ServerPlayer p) {
        return Config.SEND_PLAY_TIME.get()
                ? p.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME))
                : null;
    }

    /** The configured server identifier, or {@code null} when unset. */
    static Integer serverId() {
        String serverId = Config.SERVER_ID.get();
        return serverId.isEmpty() ? null : Integer.parseInt(serverId);
    }

    /** Builds a heartbeat roster entry for a player using the shared field builders. */
    static HeartbeatEntry heartbeatEntry(ServerPlayer p) {
        return new HeartbeatEntry(
                p.getStringUUID(),
                p.getGameProfile().getName(),
                position(p),
                dimension(p),
                rotation(p),
                experienceLevel(p),
                health(p),
                ping(p),
                playTimeTicks(p)
        );
    }
}
