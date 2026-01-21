package com.saunhardy.presenceapi;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;


@EventBusSubscriber(modid = presenceAPI.MODID)
public class PlayerEventHandler {
    private static ApiClient apiClient;

    private static ApiClient getApiClient() {
        if (apiClient == null) {
            apiClient = new ApiClient();
        }
        return apiClient;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerPresenceData data = buildPlayerData(player, "joined");
            getApiClient().sendPresenceData(data);
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerPresenceData data = buildPlayerData(player, "left");
            getApiClient().sendPresenceData(data);
        }
    }

    private static PlayerPresenceData buildPlayerData(ServerPlayer player, String state) {
        PlayerPresenceData.Builder builder = PlayerPresenceData.fromPlayer(player, state)
                .displayName(player.getDisplayName().getString())
                .gamemode(player.gameMode.getGameModeForPlayer())
                .dimension(player.level().dimension().location().toString())
                .position(player.getX(), player.getY(), player.getZ())
                .health(player.getHealth())
                .experienceLevel(player.experienceLevel);

        if (player.getServer() != null && player.connection != null) {
            String ip = player.connection.getRemoteAddress().toString();
            builder.ipAddress(ip);
        }

        return builder.build();
    }

    public static void shutdown() {
        if (apiClient != null) {
            apiClient.shutdown();
        }
    }
}