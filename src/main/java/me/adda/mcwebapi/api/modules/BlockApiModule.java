package me.adda.mcwebapi.api.modules;

import me.adda.mcwebapi.api.BaseApiModule;
import me.adda.mcwebapi.api.annotations.ApiMethod;
import me.adda.mcwebapi.api.annotations.ApiModule;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@ApiModule("block")
public class BlockApiModule extends BaseApiModule {
    private <T> T executeOnServerThread(java.util.function.Supplier<T> supplier) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;

        if (server.isSameThread()) {
            return supplier.get();
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        server.execute(() -> {
            try {
                future.complete(supplier.get());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    @ApiMethod("getBlock")
    public Map<String, Object> getBlock(String levelId, int x, int y, int z) {
        return executeOnServerThread(() -> {
            ServerLevel level = getLevel(levelId);
            if (level == null) return null;

            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);

            Map<String, Object> blockInfo = new HashMap<>();
            blockInfo.put("type", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
            blockInfo.put("x", x);
            blockInfo.put("y", y);
            blockInfo.put("z", z);

            Map<String, String> properties = new HashMap<>();
            for (Property<?> property : state.getProperties()) {
                properties.put(property.getName(), state.getValue(property).toString());
            }
            blockInfo.put("properties", properties);

            blockInfo.put("lightLevel", level.getLightEmission(pos));
            blockInfo.put("skyLight", level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos));
            blockInfo.put("blockLight", level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos));
            blockInfo.put("hasBlockEntity", blockEntity != null);

            if (blockEntity != null) {
                blockInfo.put("blockEntityType", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()).toString());
            }

            return blockInfo;
        });
    }

    @ApiMethod("setBlock")
    public boolean setBlock(String levelId, int x, int y, int z, String blockId) {
        return Boolean.TRUE.equals(executeOnServerThread(() -> {
            ServerLevel level = getLevel(levelId);
            if (level == null) return false;

            BlockPos pos = new BlockPos(x, y, z);
            net.minecraft.world.level.block.Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));

            if (block != null) {
                return level.setBlock(pos, block.defaultBlockState(), 3);
            }
            return false;
        }));
    }

    @ApiMethod("breakBlock")
    public boolean breakBlock(String levelId, int x, int y, int z, boolean dropItems) {
        return Boolean.TRUE.equals(executeOnServerThread(() -> {
            ServerLevel level = getLevel(levelId);
            if (level == null) return false;

            BlockPos pos = new BlockPos(x, y, z);
            return level.destroyBlock(pos, dropItems);
        }));
    }

    @ApiMethod("getInventory")
    public Map<String, Object> getInventory(String levelId, int x, int y, int z) {
        return executeOnServerThread(() -> {
            ServerLevel level = getLevel(levelId);
            if (level == null) {
                return Map.of("error", "Level not found");
            }

            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);

            Map<String, Object> result = new HashMap<>();
            result.put("blockType", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
            result.put("hasBlockEntity", blockEntity != null);

            if (blockEntity == null) {
                result.put("error", "Block entity not found");
                result.put("hasBlockEntityType", state.hasBlockEntity());
                return result;
            }

            result.put("blockEntityType", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()).toString());

            if (!(blockEntity instanceof Container container)) {
                result.put("error", "Block is not a container");
                return result;
            }

            List<Map<String, Object>> inventory = new ArrayList<>();
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack item = container.getItem(i);
                if (!item.isEmpty()) {
                    Map<String, Object> itemInfo = new HashMap<>();
                    itemInfo.put("slot", i);
                    itemInfo.put("item", BuiltInRegistries.ITEM.getKey(item.getItem()).toString());
                    itemInfo.put("count", item.getCount());
                    itemInfo.put("maxStackSize", item.getMaxStackSize());

                    if (item.isDamageableItem()) {
                        itemInfo.put("damage", item.getDamageValue());
                        itemInfo.put("maxDamage", item.getMaxDamage());
                    }

                    Component displayName = item.getHoverName();
                    if (displayName != null && !displayName.getString().isEmpty()) {
                        itemInfo.put("displayName", displayName.getString());
                    }

                    inventory.add(itemInfo);
                }
            }

            result.put("inventory", inventory);
            result.put("size", container.getContainerSize());
            result.remove("error");

            return result;
        });
    }

    @ApiMethod("setInventorySlot")
    public boolean setInventorySlot(String levelId, int x, int y, int z, int slot, String itemId, int count) {
        return Boolean.TRUE.equals(executeOnServerThread(() -> {
            ServerLevel level = getLevel(levelId);
            if (level == null) return false;

            BlockPos pos = new BlockPos(x, y, z);
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (!(blockEntity instanceof Container container)) {
                return false;
            }

            if (slot < 0 || slot >= container.getContainerSize()) {
                return false;
            }

            ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId)), count);
            container.setItem(slot, itemStack);
            blockEntity.setChanged();

            return true;
        }));
    }

    @ApiMethod("clearInventory")
    public boolean clearInventory(String levelId, int x, int y, int z) {
        return Boolean.TRUE.equals(executeOnServerThread(() -> {
            ServerLevel level = getLevel(levelId);
            if (level == null) return false;

            BlockPos pos = new BlockPos(x, y, z);
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (!(blockEntity instanceof Container container)) {
                return false;
            }

            container.clearContent();
            blockEntity.setChanged();

            return true;
        }));
    }

    @ApiMethod("getFurnaceInfo")
    public Map<String, Object> getFurnaceInfo(String levelId, int x, int y, int z) {
        return executeOnServerThread(() -> {
            ServerLevel level = getLevel(levelId);
            if (level == null) return null;

            BlockPos pos = new BlockPos(x, y, z);
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (!(blockEntity instanceof AbstractFurnaceBlockEntity furnace)) {
                return null;
            }

            Map<String, Object> info = new HashMap<>();
            info.put("type", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(furnace.getType()).toString());

            CompoundTag nbt = furnace.saveWithoutMetadata(level.registryAccess());
            info.put("isBurning", nbt.getShort("BurnTime") > 0);
            info.put("burnTime", nbt.getShort("BurnTime"));
            info.put("cookTime", nbt.getShort("CookTime"));
            info.put("cookTimeTotal", nbt.getShort("CookTimeTotal"));

            ItemStack input = furnace.getItem(0);
            ItemStack fuel = furnace.getItem(1);
            ItemStack output = furnace.getItem(2);

            if (!input.isEmpty()) {
                info.put("input", BuiltInRegistries.ITEM.getKey(input.getItem()).toString());
                info.put("inputCount", input.getCount());
            }

            if (!fuel.isEmpty()) {
                info.put("fuel", BuiltInRegistries.ITEM.getKey(fuel.getItem()).toString());
                info.put("fuelCount", fuel.getCount());
            }

            if (!output.isEmpty()) {
                info.put("output", BuiltInRegistries.ITEM.getKey(output.getItem()).toString());
                info.put("outputCount", output.getCount());
            }

            return info;
        });
    }

    @ApiMethod("fillArea")
    public boolean fillArea(String levelId, int x1, int y1, int z1, int x2, int y2, int z2, String blockId) {
        return Boolean.TRUE.equals(executeOnServerThread(() -> {
            ServerLevel level = getLevel(levelId);
            if (level == null) return false;

            net.minecraft.world.level.block.Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
            if (block == null) return false;

            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);

            if ((maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1) > 10000) {
                return false;
            }

            BlockState blockState = block.defaultBlockState();

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        level.setBlock(pos, blockState, 3);
                    }
                }
            }

            return true;
        }));
    }
}