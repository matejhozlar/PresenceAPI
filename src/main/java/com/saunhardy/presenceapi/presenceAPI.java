package com.saunhardy.presenceapi;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.saunhardy.crnet.CRNetClient;
import com.saunhardy.crnet.HeartbeatHandle;
import com.saunhardy.crnet.auth.AuthStrategy;
import net.minecraft.server.MinecraftServer;
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

    // Built at server start from the configured naming convention. Used by both
    // the heartbeat payload (here) and the presence events (PlayerEventHandler).
    private static Gson gson;

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

        gson = buildGson(Config.JSON_FIELD_NAMING.get());

        AuthStrategy authStrategy;
        if ("none".equals(Config.AUTH_MODE.get())) {
            authStrategy = AuthStrategy.none();
            LOGGER.info("PresenceAPI auth mode: none (requests are sent without an Authorization header)");
        } else {
            String jwtSecret = Config.JWT_SECRET.get();
            int secretBytes = jwtSecret == null ? 0 : jwtSecret.getBytes(StandardCharsets.UTF_8).length;
            if (secretBytes < MIN_JWT_SECRET_BYTES) {
                LOGGER.error(
                        "PresenceAPI disabled: jwtSecret is {} bytes ({} bits) but HS256 requires at least {} bytes (256 bits). "
                                + "Edit config/presenceapi-common.toml and set 'jwtSecret' to a value of at least {} characters "
                                + "(or set 'authMode' to \"none\" if your backend requires no auth), then restart the server.",
                        secretBytes, secretBytes * 8, MIN_JWT_SECRET_BYTES, MIN_JWT_SECRET_BYTES);
                return;
            }
            authStrategy = AuthStrategy.selfSignedJwt(jwtSecret, 60, "createrington.mod");
        }

        client = new CRNetClient.Builder()
                .baseUrl(Config.API_URL.get())
                .auth(authStrategy)
                .build();

        int heartbeatInterval = Config.HEARTBEAT_INTERVAL_MINUTES.get();
        if (heartbeatInterval > 0) {
            MinecraftServer server = event.getServer();
            heartbeatHandle = client.heartbeat()
                    .endpoint(Config.HEARTBEAT_ENDPOINT.get())
                    .interval(heartbeatInterval, TimeUnit.MINUTES)
                    // Build on the server thread: the per-player telemetry
                    // (position, health, ping, ...) is only safe to read there.
                    .payloadOn(server, () -> buildHeartbeatPayload(server))
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

    static Gson getGson() {
        return gson;
    }

    /**
     * Builds the Gson used to serialise request bodies, honouring the configured
     * field naming convention. {@code snake_case} maps to Gson's
     * {@code LOWER_CASE_WITH_UNDERSCORES} policy; anything else (i.e.
     * {@code camelCase}) keeps the record component names as-is.
     */
    private static Gson buildGson(String namingConvention) {
        GsonBuilder builder = new GsonBuilder();
        if ("snake_case".equals(namingConvention)) {
            builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        }
        return builder.create();
    }

    public static String buildHeartbeatPayload(MinecraftServer server) {
        List<Payloads.HeartbeatEntry> entries = server.getPlayerList().getPlayers().stream()
                .filter(p -> !(p instanceof FakePlayer))
                .map(Payloads::heartbeatEntry)
                .toList();

        Payloads.HeartbeatRequest request = new Payloads.HeartbeatRequest(
                entries,
                Payloads.serverId(),
                System.currentTimeMillis()
        );

        return gson.toJson(request);
    }
}
