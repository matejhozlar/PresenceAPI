package com.saunhardy.presenceapi;

import com.mojang.brigadier.CommandDispatcher;
import com.saunhardy.crnet.CRNetClient;
import com.saunhardy.createrington.api.Endpoints;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = presenceAPI.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("presenceapi")
            // .requires(source -> source.hasPermission(2)) restricts the command to Server OPs and the Server Console
            .requires(source -> source.hasPermission(2)) 
            .then(Commands.literal("sync")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    CRNetClient client = presenceAPI.getClient();

                    // Check if the mod is actually enabled in the config
                    if (client == null || !Config.ENABLED.get()) {
                        source.sendFailure(Component.literal("PresenceAPI is disabled or the client is not initialized."));
                        return 0;
                    }

                    source.sendSuccess(() -> Component.literal("Triggering manual PresenceAPI heartbeat sync..."), true);

                    // Generate the payload using the method we just made public
                    String payload = presenceAPI.buildHeartbeatPayload(source.getServer());

                    // Send the async POST request to the heartbeat endpoint
                    client.postAsync(Endpoints.PRESENCE_HEARTBEAT, payload)
                        .whenComplete((response, ex) -> {
                            if (ex != null) {
                                presenceAPI.LOGGER.error("Failed to execute manual sync: {}", ex.getMessage());
                                source.sendFailure(Component.literal("Failed to send sync request. Check server console for errors."));
                            } else if (!response.isSuccess()) {
                                presenceAPI.LOGGER.error("Manual sync returned HTTP {}: {}", 
                                        response.getStatusCode(), 
                                        response.getMessage() != null ? response.getMessage() : response.getError());
                                source.sendFailure(Component.literal("Sync returned an error from the API. Check server logs."));
                            } else {
                                source.sendSuccess(() -> Component.literal("Successfully synced presence data with the backend!"), true);
                            }
                        });

                    return 1;
                })
            )
        );
    }
}