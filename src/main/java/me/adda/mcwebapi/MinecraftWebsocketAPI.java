package me.adda.mcwebapi;

import me.adda.mcwebapi.api.BaseApiModule;
import me.adda.mcwebapi.api.ReflectiveApiDispatcher;
import me.adda.mcwebapi.api.modules.*;
import me.adda.mcwebapi.config.Config;
import me.adda.mcwebapi.websocket.WebSocketServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
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

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server starting, initializing WebSocket API");

        try {
            BaseApiModule.clearAllPlayerCaches();
            BaseApiModule.clearAllLevelCaches();

            apiDispatcher = new ReflectiveApiDispatcher();

            apiDispatcher.registerModule(new PlayerApiModule());
            apiDispatcher.registerModule(new WorldApiModule());
            apiDispatcher.registerModule(new BlockApiModule());

            webSocketServer = new WebSocketServer(apiDispatcher);
            webSocketServer.start();

            LOGGER.info("WebSocket API Mod successfully started on port {}", Config.SERVER.port.get());

        } catch (Exception e) {
            LOGGER.error("Failed to start WebSocket API Mod", e);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Server stopping, shutting down WebSocket API");

        if (webSocketServer != null) {
            webSocketServer.stop();
        }

        BaseApiModule.clearAllLevelCaches();
        BaseApiModule.clearAllPlayerCaches();
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BaseApiModule.removePlayerFromAllCaches(player.getScoreboardName());
            BaseApiModule.removePlayerFromAllCaches(player.getUUID());
            LOGGER.debug("Removed player {} from all API caches", player.getScoreboardName());
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BaseApiModule.refreshPlayerInAllCaches(player);
            LOGGER.debug("Refreshed player {} in API caches after dimension change",
                    player.getScoreboardName());
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BaseApiModule.refreshPlayerInAllCaches(player);
            LOGGER.debug("Refreshed player {} in API caches after respawn",
                    player.getScoreboardName());
        }
    }
}