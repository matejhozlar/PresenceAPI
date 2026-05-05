package com.saunhardy.presenceapi;

import com.google.gson.Gson;
import com.saunhardy.crnet.CRNetClient;
import com.saunhardy.crnet.HeartbeatHandle;
import com.saunhardy.crnet.auth.AuthStrategy;
import com.saunhardy.createrington.api.Endpoints;
import com.saunhardy.createrington.api.presence.HeartbeatPlayer;
import com.saunhardy.createrington.api.presence.HeartbeatRequest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Mod(presenceAPI.MODID)
public class presenceAPI {
    public static final String MODID = "presenceapi";
    public static final Logger LOGGER = LogUtils.getLogger();

    // HS256 (used by CRNet's SelfSignedJwtStrategy) requires a key of at least
    // 256 bits, so the configured secret must be at least 32 UTF-8 bytes.
    private static final int MIN_JWT_SECRET_BYTES = 32;

    private static final Gson GSON = new Gson();

    private static CRNetClient client;
    private static HeartbeatHandle heartbeatHandle;

    public presenceAPI(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        NeoForge.EVENT_BUS.register(this);

        LOGGER.info("Presence API initialized");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!Config.ENABLED.get()) {
            LOGGER.info("PresenceAPI is disabled via config");
            return;
        }

        String jwtSecret = Config.JWT_SECRET.get();
        int secretBytes = jwtSecret == null ? 0 : jwtSecret.getBytes(StandardCharsets.UTF_8).length;
        if (secretBytes < MIN_JWT_SECRET_BYTES) {
            LOGGER.error(
                    "PresenceAPI disabled: jwtSecret is {} bytes ({} bits) but HS256 requires at least {} bytes (256 bits). "
                            + "Edit config/presenceapi-common.toml and set 'jwtSecret' to a value of at least {} characters, then restart the server.",
                    secretBytes, secretBytes * 8, MIN_JWT_SECRET_BYTES, MIN_JWT_SECRET_BYTES);
            return;
        }

        client = new CRNetClient.Builder()
                .baseUrl(Config.API_URL.get())
                .auth(AuthStrategy.selfSignedJwt(jwtSecret, 60, "createrington.mod"))
                .build();

        int heartbeatInterval = Config.HEARTBEAT_INTERVAL_MINUTES.get();
        if (heartbeatInterval > 0) {
            MinecraftServer server = event.getServer();
            heartbeatHandle = client.heartbeat()
                    .endpoint(Endpoints.PRESENCE_HEARTBEAT)
                    .interval(heartbeatInterval, TimeUnit.MINUTES)
                    .payload(() -> buildHeartbeatPayload(server))
                    .start();
        }

        LOGGER.info("PresenceAPI client started");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Server stopping - shutting down PresenceAPI");
        if (heartbeatHandle != null) {
            heartbeatHandle.stop();
            heartbeatHandle = null;
        }
        if (client != null) {
            client.close();
            client = null;
        }
    }

    static CRNetClient getClient() {
        return client;
    }

    private String buildHeartbeatPayload(MinecraftServer server) {
        List<ServerPlayer> players = List.copyOf(server.getPlayerList().getPlayers());

        String serverId = Config.SERVER_ID.get();
        Integer serverIdInt = serverId.isEmpty() ? null : Integer.parseInt(serverId);

        HeartbeatRequest request = new HeartbeatRequest(
                players.stream()
                        .filter(p -> !(p instanceof FakePlayer))
                        .map(p -> new HeartbeatPlayer(p.getStringUUID(), p.getGameProfile().getName()))
                        .toList(),
                serverIdInt,
                System.currentTimeMillis()
        );

        return GSON.toJson(request);
    }
}
