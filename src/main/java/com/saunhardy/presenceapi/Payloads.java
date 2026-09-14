package com.saunhardy.presenceapi;

import com.saunhardy.createrington.api.presence.HeartbeatPlayer;
import com.saunhardy.createrington.api.presence.Position;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;

// Builders for the per-player fields of the createrington-api request records,
// honouring the [data] toggles. A field whose toggle is off is null and
// therefore omitted from the JSON, since the configured Gson skips nulls.
final class Payloads {

    private Payloads() {}

    static Position position(ServerPlayer p) {
        return Config.SEND_POSITION.get() ? new Position(p.getX(), p.getY(), p.getZ()) : null;
    }

    static String dimension(ServerPlayer p) {
        return Config.SEND_DIMENSION.get() ? p.level().dimension().location().toString() : null;
    }

    // The vanilla play_time stat only advances while the server awards it, so
    // a mod that pauses it (e.g. while the player is AFK) is reflected here.
    static Integer playTimeTicks(ServerPlayer p) {
        return Config.SEND_PLAY_TIME.get()
                ? p.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME))
                : null;
    }

    static Integer serverId() {
        String serverId = Config.SERVER_ID.get();
        return serverId.isEmpty() ? null : Integer.parseInt(serverId);
    }

    static HeartbeatPlayer heartbeatPlayer(ServerPlayer p) {
        return new HeartbeatPlayer(
                p.getStringUUID(),
                p.getGameProfile().getName(),
                playTimeTicks(p)
        );
    }
}
