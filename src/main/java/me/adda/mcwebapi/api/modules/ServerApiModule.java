package me.adda.mcwebapi.api.modules;

import me.adda.mcwebapi.api.BaseApiModule;
import me.adda.mcwebapi.api.annotations.ApiMethod;
import me.adda.mcwebapi.api.annotations.ApiModule;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.stream.Collectors;

@ApiModule("server")
public class ServerApiModule extends BaseApiModule {

    @ApiMethod("getInfo")
    public Map<String, Object> getInfo() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;

        Map<String, Object> info = new HashMap<>();
        info.put("version", server.getServerVersion());
        info.put("brand", server.getServerModName());
        info.put("motd", server.getMotd());
        info.put("maxPlayers", server.getMaxPlayers());
        info.put("onlinePlayerCount", server.getPlayerCount());
        info.put("difficulty", server.getWorldData().getDifficulty().toString());
        info.put("isHardcore", server.getWorldData().isHardcore());
        info.put("defaultGameMode", server.getWorldData().getGameType().getName());
        info.put("ticksRunning", server.getTickCount());
        info.put("averageTPS", getAverageTPS());

        return info;
    }

    @ApiMethod("getVersion")
    public String getVersion() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getServerVersion() : null;
    }

    @ApiMethod("getBrand")
    public String getBrand() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getServerModName() : null;
    }

    @ApiMethod("getMotd")
    public String getMotd() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getMotd() : null;
    }

    @ApiMethod("getMaxPlayers")
    public int getMaxPlayers() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getMaxPlayers() : -1;
    }

    @ApiMethod("getOnlinePlayerCount")
    public int getOnlinePlayerCount() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getPlayerCount() : -1;
    }

    @ApiMethod("getOnlinePlayers")
    public List<String> getOnlinePlayers() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Collections.emptyList();

        return server.getPlayerList().getPlayers().stream()
                .map(ServerPlayer::getScoreboardName)
                .collect(Collectors.toList());
    }

    @ApiMethod("getOnlinePlayerUUIDs")
    public List<String> getOnlinePlayerUUIDs() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Collections.emptyList();

        return server.getPlayerList().getPlayers().stream()
                .map(player -> player.getUUID().toString())
                .collect(Collectors.toList());
    }

    @ApiMethod("getTPS")
    public double getTPS() {
        return getAverageTPS();
    }

    @ApiMethod("getAverageTPS")
    public double getAverageTPS() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return -1.0;

        // Calculate average TPS based on tick times
        double meanTickTime = mean(server.tickTimes) * 1.0E-6D;
        double tps = Math.min(1000.0 / meanTickTime, 20.0);
        return Math.round(tps * 100.0) / 100.0;
    }

    private double mean(long[] values) {
        long sum = 0L;
        for (long value : values) {
            sum += value;
        }
        return (double) sum / values.length;
    }

    @ApiMethod("getTickCount")
    public long getTickCount() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getTickCount() : -1;
    }

    @ApiMethod("getUptime")
    public long getUptime() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return -1;
        return server.getTickCount() * 50; // Convert ticks to milliseconds
    }

    @ApiMethod("getMemoryUsage")
    public Map<String, Long> getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Long> memory = new HashMap<>();
        memory.put("max", runtime.maxMemory());
        memory.put("total", runtime.totalMemory());
        memory.put("free", runtime.freeMemory());
        memory.put("used", runtime.totalMemory() - runtime.freeMemory());
        return memory;
    }

    @ApiMethod("getDifficulty")
    public String getDifficulty() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        return server.getWorldData().getDifficulty().toString();
    }

    @ApiMethod("setDifficulty")
    public boolean setDifficulty(String difficulty) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        try {
            Difficulty diff = Difficulty.valueOf(difficulty.toUpperCase());
            return executeOnServerThread(() -> {
                server.setDifficulty(diff, true);
                return true;
            });
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @ApiMethod("isHardcore")
    public boolean isHardcore() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null && server.getWorldData().isHardcore();
    }

    @ApiMethod("getDefaultGameMode")
    public String getDefaultGameMode() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        return server.getWorldData().getGameType().getName();
    }

    @ApiMethod("setDefaultGameMode")
    public boolean setDefaultGameMode(String gameMode) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        try {
            GameType type = GameType.byName(gameMode.toLowerCase());
            if (type == null) return false;

            return executeOnServerThread(() -> {
                server.getWorldData().setGameType(type);
                return true;
            });
        } catch (Exception e) {
            return false;
        }
    }

    @ApiMethod("executeCommand")
    public Map<String, Object> executeCommand(String command) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Server not available");
            return result;
        }

        return executeOnServerThread(() -> {
            Map<String, Object> result = new HashMap<>();
            try {
                CommandSourceStack source = server.createCommandSourceStack()
                        .withPermission(4)
                        .withSuppressedOutput();

                int exitCode = server.getCommands().performPrefixedCommand(source, command);
                result.put("success", exitCode > 0);
                result.put("exitCode", exitCode);
            } catch (Exception e) {
                result.put("success", false);
                result.put("error", e.getMessage());
            }
            return result;
        });
    }

    @ApiMethod("broadcast")
    public boolean broadcast(String message) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
            return true;
        });
    }

    @ApiMethod("save")
    public boolean save() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            try {
                server.saveAllChunks(false, true, true);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    @ApiMethod("stop")
    public boolean stop() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        executeOnServerThread(() -> {
            server.halt(false);
            return null;
        });
        return true;
    }

    @ApiMethod("getWhitelist")
    public List<String> getWhitelist() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Collections.emptyList();

        return Arrays.stream(server.getPlayerList().getWhiteListNames())
                .collect(Collectors.toList());
    }

    @ApiMethod("isWhitelistEnabled")
    public boolean isWhitelistEnabled() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null && server.getPlayerList().isUsingWhitelist();
    }

    @ApiMethod("setWhitelistEnabled")
    public boolean setWhitelistEnabled(boolean enabled) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            server.getPlayerList().setUsingWhiteList(enabled);
            return true;
        });
    }

    @ApiMethod("getOperators")
    public List<String> getOperators() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Collections.emptyList();

        return Arrays.stream(server.getPlayerList().getOpNames())
                .collect(Collectors.toList());
    }

    @ApiMethod("getBannedPlayers")
    public List<String> getBannedPlayers() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Collections.emptyList();

        return Arrays.asList(server.getPlayerList().getBans().getUserList());
    }

    @ApiMethod("getBannedIPs")
    public List<String> getBannedIPs() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Collections.emptyList();

        return Arrays.asList(server.getPlayerList().getIpBans().getUserList());
    }
}
