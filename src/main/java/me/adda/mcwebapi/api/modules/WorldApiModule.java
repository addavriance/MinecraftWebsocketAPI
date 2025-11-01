package me.adda.mcwebapi.api.modules;

import me.adda.mcwebapi.api.BaseApiModule;
import me.adda.mcwebapi.api.annotations.ApiMethod;
import me.adda.mcwebapi.api.annotations.ApiModule;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.Difficulty;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@ApiModule("level")
public class WorldApiModule extends BaseApiModule {

    @ApiMethod("setBlock")
    public boolean setBlock(String levelId, String blockId, int x, int y, int z) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            BlockState blockState = BuiltInRegistries.BLOCK
                    .get(ResourceLocation.parse(blockId))
                    .defaultBlockState();
            return level.setBlockAndUpdate(new BlockPos(x, y, z), blockState);
        }
        return false;
    }

    @ApiMethod("getBlock")
    public String getBlock(String levelId, int x, int y, int z) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            BlockState blockState = level.getBlockState(new BlockPos(x, y, z));
            return BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();
        }
        return null;
    }

    @ApiMethod("getBlockState")
    public Map<String, Object> getBlockState(String levelId, int x, int y, int z) {
        ServerLevel level = getLevel(levelId);
        Map<String, Object> result = new HashMap<>();
        if (level != null) {
            BlockState blockState = level.getBlockState(new BlockPos(x, y, z));
            result.put("block", BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString());
            result.put("properties", blockState.getValues());
            result.put("destroySpeed", blockState.getDestroySpeed(level, new BlockPos(x, y, z)));
            result.put("lightEmission", blockState.getLightEmission());
        }
        return result;
    }

    @ApiMethod("getDayTime")
    public long getDayTime(String levelId) {
        ServerLevel level = getLevel(levelId);
        return level != null ? level.getDayTime() : 0;
    }

    @ApiMethod("setDayTime")
    public boolean setDayTime(String levelId, long time) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            level.setDayTime(time);
            return true;
        }
        return false;
    }

    @ApiMethod("getSeed")
    public long getSeed(String levelId) {
        ServerLevel level = getLevel(levelId);
        return level != null ? level.getSeed() : 0;
    }

    @ApiMethod("getWeather")
    public Map<String, Object> getWeather(String levelId) {
        ServerLevel level = getLevel(levelId);
        Map<String, Object> result = new HashMap<>();
        if (level != null) {
            result.put("isRaining", level.isRaining());
            result.put("isThundering", level.isThundering());
            result.put("rainLevel", level.getRainLevel(1.0F));
            result.put("thunderLevel", level.getThunderLevel(1.0F));
        }
        return result;
    }

    @ApiMethod("setWeather")
    public boolean setWeather(String levelId, boolean raining, boolean thundering) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            level.setWeatherParameters(0, raining ? 6000 : 0, raining, thundering);
            return true;
        }
        return false;
    }

    @ApiMethod("getWorldBorder")
    public Map<String, Object> getWorldBorder(String levelId) {
        ServerLevel level = getLevel(levelId);
        Map<String, Object> result = new HashMap<>();
        if (level != null) {
            WorldBorder border = level.getWorldBorder();
            result.put("centerX", border.getCenterX());
            result.put("centerZ", border.getCenterZ());
            result.put("size", border.getSize());
            result.put("damagePerBlock", border.getDamagePerBlock());
            result.put("damageSafeZone", border.getDamageSafeZone());
            result.put("warningTime", border.getWarningTime());
            result.put("warningBlocks", border.getWarningBlocks());
        }
        return result;
    }

    @ApiMethod("setWorldBorder")
    public boolean setWorldBorder(String levelId, double centerX, double centerZ, double size) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            WorldBorder border = level.getWorldBorder();
            border.setCenter(centerX, centerZ);
            border.setSize(size);
            return true;
        }
        return false;
    }

    @ApiMethod("getHeight")
    public int getHeight(String levelId, int x, int z, String heightmapType) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            Heightmap.Types type = Heightmap.Types.valueOf(heightmapType.toUpperCase());
            return level.getHeight(type, x, z);
        }
        return -1;
    }

    @ApiMethod("getSpawnPoint")
    public Map<String, Object> getSpawnPoint(String levelId) {
        ServerLevel level = getLevel(levelId);
        Map<String, Object> result = new HashMap<>();
        if (level != null) {
            BlockPos spawn = level.getSharedSpawnPos();
            result.put("x", spawn.getX());
            result.put("y", spawn.getY());
            result.put("z", spawn.getZ());
            result.put("angle", level.getSharedSpawnAngle());
        }
        return result;
    }

    @ApiMethod("setSpawnPoint")
    public boolean setSpawnPoint(String levelId, int x, int y, int z, float angle) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            level.setDefaultSpawnPos(new BlockPos(x, y, z), angle);
            return true;
        }
        return false;
    }

    @ApiMethod("getDifficulty")
    public String getDifficulty(String levelId) {
        ServerLevel level = getLevel(levelId);
        return level != null ? level.getDifficulty().name() : null;
    }

    @ApiMethod("setDifficulty")
    public boolean setDifficulty(String levelId, String difficulty) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            try {
                Difficulty diff = Difficulty.valueOf(difficulty.toUpperCase());
                level.getServer().setDifficulty(diff, true);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    @ApiMethod("getPlayers")
    public List<String> getPlayers(String levelId) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            return level.players().stream()
                    .map(Player::getScoreboardName)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @ApiMethod("getEntities")
    public List<String> getEntities(String levelId) {
        ServerLevel level = getLevel(levelId);
        List<String> entities = new ArrayList<>();
        if (level != null) {
            Iterable<Entity> allEntities = level.getAllEntities();
            for (Entity entity : allEntities) {
                entities.add(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
            }
        }
        return entities;
    }

    @ApiMethod("getEntityCount")
    public int getEntityCount(String levelId) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            int count = 0;
            for (Entity ignored : level.getAllEntities()) {
                count++;
            }
            return count;
        }
        return 0;
    }

    @ApiMethod("getPlayerCount")
    public int getPlayerCount(String levelId) {
        ServerLevel level = getLevel(levelId);
        return level != null ? level.players().size() : 0;
    }

    @ApiMethod("getChunkInfo")
    public Map<String, Object> getChunkInfo(String levelId, int chunkX, int chunkZ) {
        ServerLevel level = getLevel(levelId);
        Map<String, Object> result = new HashMap<>();
        if (level != null) {
            result.put("isLoaded", level.hasChunk(chunkX, chunkZ));
            result.put("inhabitedTime", level.getChunk(chunkX, chunkZ).getInhabitedTime());
            result.put("chunkX", chunkX);
            result.put("chunkZ", chunkZ);
        }
        return result;
    }

    @ApiMethod("loadChunk")
    public boolean loadChunk(String levelId, int chunkX, int chunkZ) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            return level.getChunkSource().getChunk(chunkX, chunkZ, true) != null;
        }
        return false;
    }

    @ApiMethod("unloadChunk")
    public boolean unloadChunk(String levelId, int chunkX, int chunkZ) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            level.unload(level.getChunk(chunkX, chunkZ));
            return true;
        }
        return false;
    }

    @ApiMethod("getLightLevel")
    public int getLightLevel(String levelId, int x, int y, int z) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            return level.getMaxLocalRawBrightness(new BlockPos(x, y, z));
        }
        return 0;
    }

    @ApiMethod("getMoonPhase")
    public int getMoonPhase(String levelId) {
        ServerLevel level = getLevel(levelId);
        return level != null ? level.getMoonPhase() : 0;
    }

    @ApiMethod("isDay")
    public boolean isDay(String levelId) {
        ServerLevel level = getLevel(levelId);
        return level != null && level.isDay();
    }

    @ApiMethod("isNight")
    public boolean isNight(String levelId) {
        ServerLevel level = getLevel(levelId);
        return level != null && level.isNight();
    }

    @ApiMethod("getTotalTime")
    public long getTotalTime(String levelId) {
        ServerLevel level = getLevel(levelId);
        return level != null ? level.getGameTime() : 0;
    }

    @ApiMethod("getLevelData")
    public Map<String, Object> getLevelData(String levelId) {
        ServerLevel level = getLevel(levelId);
        Map<String, Object> result = new HashMap<>();
        if (level != null) {
            ServerLevelData data = level.getServer().getWorldData().overworldData();
            result.put("levelName", data.getLevelName());
            result.put("hardcore", data.isHardcore());
            result.put("allowCommands", data.isAllowCommands());
            result.put("gameType", data.getGameType().getName());
        }
        return result;
    }

    @ApiMethod("sendMessageToAll")
    public boolean sendMessageToAll(String levelId, String message) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            Component text = Component.literal(message);
            for (ServerPlayer player : level.players()) {
                player.sendSystemMessage(text);
            }
            return true;
        }
        return false;
    }

    @ApiMethod("explode")
    public boolean explode(String levelId, double x, double y, double z, float power, boolean fire) {
        ServerLevel level = getLevel(levelId);
        if (level != null) {
            level.explode(null, x, y, z, power, fire, Level.ExplosionInteraction.TNT);
            return true;
        }
        return false;
    }

    @ApiMethod("getAvailableLevels")
    public List<String> getAvailableLevels() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.levelKeys().stream()
                    .map(ResourceKey::location)
                    .map(ResourceLocation::toString)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @ApiMethod("getLevelInfo")
    public Map<String, Object> getLevelInfo(String levelId) {
        ServerLevel level = getLevel(levelId);
        Map<String, Object> result = new HashMap<>();
        if (level != null) {
            result.put("dimension", level.dimension().location().toString());
            result.put("seed", level.getSeed());
            result.put("dayTime", level.getDayTime());
            result.put("totalTime", level.getGameTime());
            result.put("raining", level.isRaining());
            result.put("thundering", level.isThundering());
            result.put("playerCount", level.players().size());
            result.put("entityCount", level.getAllEntities());
            result.put("difficulty", level.getDifficulty().name());
        }
        return result;
    }
}