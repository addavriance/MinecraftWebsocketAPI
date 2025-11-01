package me.adda.mcwebapi.api.modules;

import me.adda.mcwebapi.api.BaseApiModule;
import me.adda.mcwebapi.api.annotations.ApiMethod;
import me.adda.mcwebapi.api.annotations.ApiModule;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.network.chat.Component;


import java.util.*;
import java.util.stream.Collectors;

@ApiModule("player")
public class PlayerApiModule extends BaseApiModule {

    @ApiMethod("sendMessage")
    public boolean sendMessage(String playerName, String message) {
        System.out.println(playerName);
        ServerPlayer player = findPlayer(playerName);
        if (player != null) {
            player.sendSystemMessage(Component.literal(message));
            return true;
        }
        return false;
    }

    @ApiMethod("getHealth")
    public float getHealth(String playerName) {
        ServerPlayer player = findPlayer(playerName);
        return player != null ? player.getHealth() : -1.0f;
    }

    @ApiMethod("getPosition")
    public Map<String, Double> getPosition(String playerName) {
        ServerPlayer player = findPlayer(playerName);
        if (player != null) {
            Map<String, Double> pos = new HashMap<>();
            pos.put("x", player.getX());
            pos.put("y", player.getY());
            pos.put("z", player.getZ());
            return pos;
        }
        return null;
    }

    @ApiMethod("teleport")
    public boolean teleport(String playerName, double x, double y, double z) {
        ServerPlayer player = findPlayer(playerName);
        if (player != null) {
            player.teleportTo(x, y, z);
            return true;
        }
        return false;
    }

    @ApiMethod("list")
    public List<String> listPlayers() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.getPlayerList().getPlayers()
                    .stream()
                    .map(player -> player.getGameProfile().getName())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @ApiMethod("getFood")
    public int getFoodLevel(String playerName) {
        ServerPlayer player = findPlayer(playerName);
        return player != null ? player.getFoodData().getFoodLevel() : -1;
    }

    private ServerPlayer findPlayer(String identifier) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.getPlayerList().getPlayerByName(identifier);
        }
        return null;
    }
}
