package com.saunhardy.presenceapi;

import com.saunhardy.crnet.CRNetClient;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = presenceAPI.MODID)
public class PlayerEventHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendPresenceData(player, "joined");
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendPresenceData(player, "left");
        }
    }

    private static void sendPresenceData(ServerPlayer player, String state) {
        CRNetClient client = presenceAPI.getClient();
        if (client == null || !Config.ENABLED.get()) {
            return;
        }

        PlayerPresenceData data = buildPlayerData(player, state);
        String json = data.toJson().toString();

        if (Config.LOG_REQUESTS.get()) {
            presenceAPI.LOGGER.info("Sending presence data: {}", json);
        }

        client.postAsync(Config.PRESENCE_ENDPOINT.get(), json)
                .whenComplete((response, ex) -> {
                    if (ex != null) {
                        presenceAPI.LOGGER.error("Failed to send presence data: {}", ex.getMessage());
                    } else if (!response.isSuccess()) {
                        presenceAPI.LOGGER.error("Presence POST returned HTTP {}: {}",
                                response.getStatusCode(),
                                response.getMessage() != null ? response.getMessage() : response.getError());
                    } else if (Config.LOG_REQUESTS.get()) {
                        presenceAPI.LOGGER.info("Presence data sent: {}",
                                response.getMessage() != null ? response.getMessage() : "success");
                    }
                });
    }

    private static PlayerPresenceData buildPlayerData(ServerPlayer player, String state) {
        PlayerPresenceData.Builder builder = PlayerPresenceData.fromPlayer(player, state);

        String serverId = Config.SERVER_ID.get();
        if (!serverId.isEmpty()) {
            builder.serverId(serverId);
        }

        if (Config.SEND_DISPLAY_NAME.get()) {
            builder.displayName(player.getDisplayName().getString());
        }
        if (Config.SEND_GAMEMODE.get()) {
            builder.gamemode(player.gameMode.getGameModeForPlayer());
        }
        if (Config.SEND_DIMENSION.get()) {
            builder.dimension(player.level().dimension().location().toString());
        }
        if (Config.SEND_POSITION.get()) {
            builder.position(player.getX(), player.getY(), player.getZ());
        }
        if (Config.SEND_HEALTH.get()) {
            builder.health(player.getHealth());
        }
        if (Config.SEND_EXPERIENCE_LEVEL.get()) {
            builder.experienceLevel(player.experienceLevel);
        }
        if (Config.SEND_PLAYER_IP.get() && player.connection != null) {
            builder.ipAddress(player.connection.getRemoteAddress().toString());
        }

        return builder.build();
    }
}
