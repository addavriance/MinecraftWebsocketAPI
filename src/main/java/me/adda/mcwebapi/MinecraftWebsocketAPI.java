package me.adda.mcwebapi;

import me.adda.mcwebapi.api.ReflectiveApiDispatcher;
import me.adda.mcwebapi.api.modules.*;
import me.adda.mcwebapi.config.Config;
import me.adda.mcwebapi.websocket.WebSocketServer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MinecraftWebsocketAPI.MODID)
public class MinecraftWebsocketAPI {
    public static final String MODID = "mcwebapi";
    private static final Logger LOGGER = LogManager.getLogger();

    private WebSocketServer webSocketServer;
    private ReflectiveApiDispatcher apiDispatcher;

    public MinecraftWebsocketAPI(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing WebSocket API Mod");

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);

        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void onServerStarting(net.neoforged.neoforge.event.server.ServerStartingEvent event) {
        LOGGER.info("Server starting, initializing WebSocket API");

        try {

            apiDispatcher = new ReflectiveApiDispatcher();

            apiDispatcher.registerModule(new PlayerApiModule());
            apiDispatcher.registerModule(new WorldApiModule());

            webSocketServer = new WebSocketServer(apiDispatcher);
            webSocketServer.start();

            LOGGER.info("WebSocket API Mod successfully started on port {}", Config.SERVER.port.get());

        } catch (Exception e) {
            LOGGER.error("Failed to start WebSocket API Mod", e);
        }
    }

    private void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        LOGGER.info("Server stopping, shutting down WebSocket API");

        if (webSocketServer != null) {
            webSocketServer.stop();
        }
    }
}