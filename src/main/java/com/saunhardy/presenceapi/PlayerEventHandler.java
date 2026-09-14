package com.saunhardy.presenceapi;

import com.saunhardy.createrington.api.presence.PresenceRequest;
import com.saunhardy.crnet.CRNetClient;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = presenceAPI.MODID)
public class PlayerEventHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            sendPresenceData(player, "joined");
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            sendPresenceData(player, "left");
        }
    }

    private static void sendPresenceData(ServerPlayer player, String state) {
        CRNetClient client = presenceAPI.getClient();
        if (client == null || !Config.ENABLED.get()) {
            return;
        }

        PresenceRequest request = new PresenceRequest(
                player.getGameProfile().getName(),
                player.getStringUUID(),
                state,
                System.currentTimeMillis(),
                Payloads.serverId(),
                Payloads.position(player),
                Payloads.dimension(player),
                Payloads.playTimeTicks(player)
        );

        String json = presenceAPI.getGson().toJson(request);

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
}
