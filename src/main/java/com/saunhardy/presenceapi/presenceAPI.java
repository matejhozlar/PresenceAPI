package com.saunhardy.presenceapi;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(presenceAPI.MODID)
public class presenceAPI {
    public static final String MODID = "presenceapi";
    public static final Logger LOGGER = LogUtils.getLogger();

    public presenceAPI(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Register for server stopping event
        NeoForge.EVENT_BUS.register(this);

        LOGGER.info("Presence API initialized");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Server stopping - shutting down PresenceAPI");
        PlayerEventHandler.shutdown();
    }
}