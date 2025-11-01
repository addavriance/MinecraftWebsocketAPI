package me.adda.mcwebapi.api.modules;

import me.adda.mcwebapi.api.BaseApiModule;
import me.adda.mcwebapi.api.annotations.ApiMethod;
import me.adda.mcwebapi.api.annotations.ApiModule;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@ApiModule("world")
public class WorldApiModule extends BaseApiModule {

    @ApiMethod("setBlock")
    public boolean setBlock(String blockId, int x, int y, int z) {
        ServerLevel level = getServerLevel();
        if (level != null) {
            BlockState blockState = BuiltInRegistries.BLOCK
                    .get(ResourceLocation.parse(blockId))
                    .defaultBlockState();
            return level.setBlockAndUpdate(new BlockPos(x, y, z), blockState);
        }
        return false;
    }

    @ApiMethod("getBlock")
    public String getBlock(int x, int y, int z) {
        ServerLevel level = getServerLevel();
        if (level != null) {
            BlockState blockState = level.getBlockState(new BlockPos(x, y, z));
            return BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();
        }
        return null;
    }

    @ApiMethod("getTime")
    public long getTime() {
        ServerLevel level = getServerLevel();
        return level != null ? level.getDayTime() : 0;
    }

    @ApiMethod("setTime")
    public boolean setTime(long time) {
        ServerLevel level = getServerLevel();
        if (level != null) {
            level.setDayTime(time);
            return true;
        }
        return false;
    }

    @ApiMethod("getSeed")
    public long getSeed() {
        ServerLevel level = getServerLevel();
        return level != null ? level.getSeed() : 0;
    }

    private ServerLevel getServerLevel() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getLevel(Level.OVERWORLD) : null;
    }
}
