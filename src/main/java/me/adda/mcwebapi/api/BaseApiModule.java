package me.adda.mcwebapi.api;

import me.adda.mcwebapi.api.annotations.ApiMethod;
import me.adda.mcwebapi.api.annotations.ApiModule;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class BaseApiModule {
    protected final Map<String, CacheEntry<ServerLevel>> levelCache = new ConcurrentHashMap<>();
    protected final Map<String, CacheEntry<ServerPlayer>> playerByNameCache = new ConcurrentHashMap<>();
    protected final Map<UUID, CacheEntry<ServerPlayer>> playerByUuidCache = new ConcurrentHashMap<>();

    private static final int MAX_CACHE_SIZE = 100;
    private static final int MAX_PLAYER_CACHE_SIZE = 500;
    private static final long CACHE_TTL = 60000; // 1 minute
    private static final long PLAYER_CACHE_TTL = 30000; // 30 seconds

    private final Map<String, Method> methodCache = new ConcurrentHashMap<>();
    private final String moduleName;

    private static final List<BaseApiModule> ALL_MODULES = new ArrayList<>(); // all instances for proper cache invalidation

    public static void clearAllLevelCaches() {
        for (BaseApiModule module : ALL_MODULES) {
            module.levelCache.clear();
        }
    }

    public static void clearAllPlayerCaches() {
        for (BaseApiModule module : ALL_MODULES) {
            module.playerByNameCache.clear();
            module.playerByUuidCache.clear();
        }
    }

    public static void removePlayerFromAllCaches(String playerName) {
        for (BaseApiModule module : ALL_MODULES) {
            module.playerByNameCache.remove(playerName);
        }
    }

    public static void removePlayerFromAllCaches(UUID playerUuid) {
        for (BaseApiModule module : ALL_MODULES) {
            module.playerByUuidCache.remove(playerUuid);
        }
    }

    public static void refreshPlayerInAllCaches(ServerPlayer player) {
        for (BaseApiModule module : ALL_MODULES) {
            module.playerByNameCache.put(player.getScoreboardName(),
                    new CacheEntry<>(player, PLAYER_CACHE_TTL));
            module.playerByUuidCache.put(player.getUUID(),
                    new CacheEntry<>(player, PLAYER_CACHE_TTL));
        }
    }

    public BaseApiModule() {
        ApiModule annotation = this.getClass().getAnnotation(ApiModule.class);
        this.moduleName = annotation != null ? annotation.value() :
                this.getClass().getSimpleName().toLowerCase().replace("module", "");

        ALL_MODULES.add(this);
    }

    public String getModuleName() {
        return moduleName;
    }

    public Method getMethod(String methodName) {
        return methodCache.get(methodName.toLowerCase());
    }

    public void discoverMethods() {
        methodCache.clear();
        for (Method method : this.getClass().getMethods()) {
            if (method.isAnnotationPresent(ApiMethod.class)) {
                ApiMethod annotation = method.getAnnotation(ApiMethod.class);
                String name = annotation.value().isEmpty() ?
                        method.getName() : annotation.value();
                methodCache.put(name.toLowerCase(), method);

                System.out.println("Discovered method: " + moduleName + "." + name);
            }
        }
    }

    public Set<String> getAvailableMethods() {
        return methodCache.keySet();
    }

    protected ServerLevel getLevel(String levelId) {
        if (levelCache.size() >= MAX_CACHE_SIZE) {
            levelCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }

        CacheEntry<ServerLevel> entry = levelCache.get(levelId);
        if (entry != null) {
            if (entry.isExpired()) {
                levelCache.remove(levelId);
            } else {
                ServerLevel level = entry.getValue();
                if (level != null) {
                    return level;
                } else {
                    levelCache.remove(levelId);
                }
            }
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(levelId));
        ServerLevel level = server.getLevel(dimension);

        if (level != null) {
            levelCache.put(levelId, new CacheEntry<>(level));
        }

        return level;
    }

    protected ServerPlayer findPlayer(String identifier) {
        ServerPlayer player = findPlayerByName(identifier);
        if (player != null) return player;

        try {
            UUID uuid = UUID.fromString(identifier);
            return findPlayerByUuid(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ServerPlayer findPlayerByName(String playerName) {
        return findPlayerInCache(playerName, playerByNameCache,
                server -> server.getPlayerList().getPlayerByName(playerName));
    }

    private ServerPlayer findPlayerByUuid(UUID uuid) {
        return findPlayerInCache(uuid, playerByUuidCache,
                server -> server.getPlayerList().getPlayer(uuid));
    }

    private <T> ServerPlayer findPlayerInCache(T key, Map<T, CacheEntry<ServerPlayer>> cache,
                                               Function<MinecraftServer, ServerPlayer> playerFinder) {
        if (cache.size() >= MAX_PLAYER_CACHE_SIZE) {
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }

        CacheEntry<ServerPlayer> entry = cache.get(key);
        if (entry != null) {
            if (entry.isExpired()) {
                cache.remove(key);
            } else {
                ServerPlayer player = entry.getValue();
                if (player != null && isValidPlayer(player)) {
                    return player;
                } else {
                    cache.remove(key);
                }
            }
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;

        ServerPlayer player = playerFinder.apply(server);

        if (player != null) {
            cache.put(key, new CacheEntry<>(player, PLAYER_CACHE_TTL));
            if (key instanceof String) {
                playerByUuidCache.put(player.getUUID(), new CacheEntry<>(player, PLAYER_CACHE_TTL));
            } else if (key instanceof UUID) {
                playerByNameCache.put(player.getScoreboardName(), new CacheEntry<>(player, PLAYER_CACHE_TTL));
            }
        }

        return player;
    }

    private boolean isValidPlayer(ServerPlayer player) {
        return player.connection.getConnection().isConnected() &&
                player.server != null &&
                player.server.getPlayerList().getPlayerByName(player.getScoreboardName()) != null;
    }

    public static class CacheEntry<T> {
        private final SoftReference<T> value;
        private final long timestamp;
        private final long ttl;

        public CacheEntry(T value) {
            this(value, CACHE_TTL);
        }

        public CacheEntry(T value, long ttl) {
            this.value = new SoftReference<>(value);
            this.timestamp = System.currentTimeMillis();
            this.ttl = ttl;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ttl;
        }

        public T getValue() {
            return value.get();
        }
    }
}