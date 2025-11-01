package me.adda.mcwebapi.api.modules;

import me.adda.mcwebapi.api.BaseApiModule;
import me.adda.mcwebapi.api.annotations.ApiMethod;
import me.adda.mcwebapi.api.annotations.ApiModule;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.stream.Collectors;

@ApiModule("player")
public class PlayerApiModule extends BaseApiModule {

    @ApiMethod("sendMessage")
    public boolean sendMessage(String identifier, String message) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            player.sendSystemMessage(Component.literal(message));
            return true;
        }
        return false;
    }

    @ApiMethod("getHealth")
    public float getHealth(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        return player != null ? player.getHealth() : -1.0f;
    }

    @ApiMethod("setHealth")
    public boolean setHealth(String identifier, float health) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            player.setHealth(health);
            return true;
        }
        return false;
    }

    @ApiMethod("getMaxHealth")
    public float getMaxHealth(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        return player != null ? player.getMaxHealth() : -1.0f;
    }

    @ApiMethod("getPosition")
    public Map<String, Double> getPosition(String identifier) {
        ServerPlayer player = findPlayer(identifier);
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
    public boolean teleport(String identifier, double x, double y, double z) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            player.teleportTo(x, y, z);
            return true;
        }
        return false;
    }

    @ApiMethod("teleportToPlayer")
    public boolean teleportToPlayer(String identifier, String targetidentifier) {
        ServerPlayer player = findPlayer(identifier);
        ServerPlayer target = findPlayer(targetidentifier);
        if (player != null && target != null) {
            player.teleportTo(target.getX(), target.getY(), target.getZ());
            return true;
        }
        return false;
    }

    @ApiMethod("teleportToDimension")
    public boolean teleportToDimension(String identifier, String dimension, double x, double y, double z) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            ServerLevel targetLevel = server.getLevel(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                    ResourceLocation.parse(dimension)));
            if (targetLevel != null) {
                player.teleportTo(targetLevel, x, y, z, player.getYRot(), player.getXRot());
                return true;
            }
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

    @ApiMethod("kick")
    public boolean kick(String identifier, String reason) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            Component kickMessage = Component.literal(reason);
            player.connection.disconnect(kickMessage);
            return true;
        }
        return false;
    }

    @ApiMethod("getFood")
    public int getFoodLevel(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        return player != null ? player.getFoodData().getFoodLevel() : -1;
    }

    @ApiMethod("setFood")
    public boolean setFoodLevel(String identifier, int foodLevel) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            player.getFoodData().setFoodLevel(foodLevel);
            return true;
        }
        return false;
    }

    @ApiMethod("getSaturation")
    public float getSaturation(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        return player != null ? player.getFoodData().getSaturationLevel() : -1.0f;
    }

    @ApiMethod("setSaturation")
    public boolean setSaturation(String identifier, float saturation) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            player.getFoodData().setSaturation(saturation);
            return true;
        }
        return false;
    }

    @ApiMethod("getExperience")
    public Map<String, Integer> getExperience(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        Map<String, Integer> exp = new HashMap<>();
        if (player != null) {
            exp.put("level", player.experienceLevel);
            exp.put("total", player.totalExperience);
            exp.put("progress", (int)(player.experienceProgress * 100));
        }
        return exp;
    }

    @ApiMethod("setExperience")
    public boolean setExperience(String identifier, int level) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            player.setExperienceLevels(level);
            return true;
        }
        return false;
    }

    @ApiMethod("getGameMode")
    public String getGameMode(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        return player != null ? player.gameMode.getGameModeForPlayer().getName() : null;
    }

    @ApiMethod("setGameMode")
    public boolean setGameMode(String identifier, String gameMode) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            try {
                GameType mode = GameType.byName(gameMode.toLowerCase());
                player.setGameMode(mode);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    @ApiMethod("getInventory")
    public List<Map<String, Object>> getInventory(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        List<Map<String, Object>> inventory = new ArrayList<>();
        if (player != null) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (!item.isEmpty()) {
                    Map<String, Object> itemInfo = new HashMap<>();
                    itemInfo.put("slot", i);
                    itemInfo.put("item", BuiltInRegistries.ITEM.getKey(item.getItem()).toString());
                    itemInfo.put("count", item.getCount());
                    itemInfo.put("damage", item.getDamageValue());
                    inventory.add(itemInfo);
                }
            }
        }
        return inventory;
    }

    @ApiMethod("clearInventory")
    public boolean clearInventory(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            player.getInventory().clearContent();
            return true;
        }
        return false;
    }

    @ApiMethod("getEffects")
    public List<Map<String, Object>> getEffects(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        List<Map<String, Object>> effects = new ArrayList<>();
        if (player != null) {
            for (MobEffectInstance effect : player.getActiveEffects()) {
                Map<String, Object> effectInfo = new HashMap<>();
                effectInfo.put("effect", BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString());
                effectInfo.put("amplifier", effect.getAmplifier());
                effectInfo.put("duration", effect.getDuration());
                effects.add(effectInfo);
            }
        }
        return effects;
    }

    @ApiMethod("addEffect")
    public boolean addEffect(String identifier, String effect, int duration, int amplifier) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effect)).orElse(null);
            if (effectHolder != null) {
                MobEffectInstance effectInstance = new MobEffectInstance(
                        effectHolder,
                        duration,
                        amplifier
                );
                player.addEffect(effectInstance);
                return true;
            }
        }
        return false;
    }

    @ApiMethod("clearEffects")
    public boolean clearEffects(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            player.removeAllEffects();
            return true;
        }
        return false;
    }

    @ApiMethod("getScore")
    public int getScore(String identifier, String objective) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            Scoreboard scoreboard = player.getScoreboard();
            Objective obj = scoreboard.getObjective(objective);
            if (obj != null) {
                return scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(player.getScoreboardName()), obj).get();
            }
        }
        return -1;
    }

    @ApiMethod("setScore")
    public boolean setScore(String identifier, String objective, int score) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            Scoreboard scoreboard = player.getScoreboard();
            Objective obj = scoreboard.getObjective(objective);
            if (obj != null) {
                scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(player.getScoreboardName()), obj).add(score);
                return true;
            }
        }
        return false;
    }

    @ApiMethod("grantAdvancement")
    public boolean grantAdvancement(String identifier, String advancementId) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                net.minecraft.advancements.AdvancementHolder advancement =
                        server.getAdvancements().get(ResourceLocation.parse(advancementId));
                if (advancement != null) {
                    net.minecraft.advancements.AdvancementProgress progress =
                            player.getAdvancements().getOrStartProgress(advancement);
                    for (String criterion : advancement.value().criteria().keySet()) {
                        player.getAdvancements().award(advancement, criterion);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @ApiMethod("revokeAdvancement")
    public boolean revokeAdvancement(String identifier, String advancementId) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                net.minecraft.advancements.AdvancementHolder advancement =
                        server.getAdvancements().get(ResourceLocation.parse(advancementId));
                if (advancement != null) {
                    net.minecraft.advancements.AdvancementProgress progress =
                            player.getAdvancements().getOrStartProgress(advancement);
                    for (String criterion : advancement.value().criteria().keySet()) {
                        player.getAdvancements().revoke(advancement, criterion);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @ApiMethod("getAdvancements")
    public Map<String, Object> getAdvancements(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        Map<String, Object> result = new HashMap<>();

        if (player != null) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                List<Map<String, Object>> completedAdvancements = new ArrayList<>();
                List<Map<String, Object>> inProgressAdvancements = new ArrayList<>();

                for (net.minecraft.advancements.AdvancementHolder advancement : server.getAdvancements().getAllAdvancements()) {
                    net.minecraft.advancements.AdvancementProgress progress =
                            player.getAdvancements().getOrStartProgress(advancement);

                    Map<String, Object> advInfo = new HashMap<>();
                    String advId = advancement.id().toString();
                    advInfo.put("id", advId);

                    if (advancement.value().display().isPresent()) {
                        net.minecraft.advancements.DisplayInfo display = advancement.value().display().get();
                        advInfo.put("title", display.getTitle().getString());
                        advInfo.put("description", display.getDescription().getString());
                        advInfo.put("frame", display.getType().getSerializedName()); // task, goal, challenge
                        advInfo.put("hidden", display.isHidden());
                        advInfo.put("icon", BuiltInRegistries.ITEM.getKey(display.getIcon().getItem()).toString());
                    }

                    if (progress.isDone()) {
                        advInfo.put("completed", true);
                        advInfo.put("completedDate", Objects.requireNonNull(progress.getFirstProgressDate()).toString());
                        completedAdvancements.add(advInfo);
                    } else if (progress.hasProgress()) {
                        advInfo.put("completed", false);

                        Map<String, Boolean> criteria = new HashMap<>();
                        for (String criterion : advancement.value().criteria().keySet()) {
                            criteria.put(criterion, progress.getCriterion(criterion) != null);
                        }
                        advInfo.put("criteria", criteria);

                        int total = advancement.value().criteria().size();
                        int completed = (int) criteria.values().stream().filter(b -> b).count();
                        advInfo.put("progress", String.format("%d/%d", completed, total));
                        advInfo.put("percentage", (completed * 100.0) / total);

                        inProgressAdvancements.add(advInfo);
                    }
                }

                result.put("completed", completedAdvancements);
                result.put("inProgress", inProgressAdvancements);
                result.put("totalCompleted", completedAdvancements.size());
                result.put("totalInProgress", inProgressAdvancements.size());
            }
        }

        return result;
    }

    @ApiMethod("getUUID")
    public String getUUID(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        return player != null ? player.getUUID().toString() : null;
    }

    @ApiMethod("isOnline")
    public boolean isOnline(String identifier) {
        return findPlayer(identifier) != null;
    }

    @ApiMethod("getPing")
    public int getPing(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        return player != null ? player.connection.latency() : -1;
    }

    @ApiMethod("getWorld")
    public String getWorld(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        return player != null ? player.level().dimension().location().toString() : null;
    }

    @ApiMethod("getRotation")
    public Map<String, Float> getRotation(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        Map<String, Float> rotation = new HashMap<>();
        if (player != null) {
            rotation.put("yaw", player.getYRot());
            rotation.put("pitch", player.getXRot());
        }
        return rotation;
    }

    @ApiMethod("setRotation")
    public boolean setRotation(String identifier, float yaw, float pitch) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            player.setYRot(yaw);
            player.setXRot(pitch);
            return true;
        }
        return false;
    }

    @ApiMethod("getVelocity")
    public Map<String, Double> getVelocity(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        Map<String, Double> velocity = new HashMap<>();
        if (player != null) {
            Vec3 motion = player.getDeltaMovement();
            velocity.put("x", motion.x);
            velocity.put("y", motion.y);
            velocity.put("z", motion.z);
        }
        return velocity;
    }

    @ApiMethod("setVelocity")
    public boolean setVelocity(String identifier, double x, double y, double z) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            player.setDeltaMovement(new Vec3(x, y, z));
            return true;
        }
        return false;
    }

    @ApiMethod("getArmor")
    public List<Map<String, Object>> getArmor(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        List<Map<String, Object>> armor = new ArrayList<>();
        if (player != null) {
            for (int i = 0; i < 4; i++) {
                ItemStack item = player.getInventory().getArmor(i);
                if (!item.isEmpty()) {
                    Map<String, Object> itemInfo = new HashMap<>();
                    itemInfo.put("slot", i);
                    itemInfo.put("item", BuiltInRegistries.ITEM.getKey(item.getItem()).toString());
                    itemInfo.put("count", item.getCount());
                    itemInfo.put("damage", item.getDamageValue());
                    armor.add(itemInfo);
                }
            }
        }
        return armor;
    }

    @ApiMethod("getEnderChest")
    public List<Map<String, Object>> getEnderChest(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        List<Map<String, Object>> enderChest = new ArrayList<>();
        if (player != null) {
            for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
                ItemStack item = player.getEnderChestInventory().getItem(i);
                if (!item.isEmpty()) {
                    Map<String, Object> itemInfo = new HashMap<>();
                    itemInfo.put("slot", i);
                    itemInfo.put("item", BuiltInRegistries.ITEM.getKey(item.getItem()).toString());
                    itemInfo.put("count", item.getCount());
                    itemInfo.put("damage", item.getDamageValue());
                    enderChest.add(itemInfo);
                }
            }
        }
        return enderChest;
    }

    @ApiMethod("giveItem")
    public boolean giveItem(String identifier, String itemId, int count) {
        ServerPlayer player = findPlayer(identifier);
        if (player != null) {
            ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId)), count);
            return player.getInventory().add(itemStack);
        }
        return false;
    }

    @ApiMethod("getPlayerInfo")
    public Map<String, Object> getPlayerInfo(String identifier) {
        ServerPlayer player = findPlayer(identifier);
        Map<String, Object> info = new HashMap<>();
        if (player != null) {
            info.put("name", player.getGameProfile().getName());
            info.put("uuid", player.getUUID().toString());
            info.put("health", player.getHealth());
            info.put("maxHealth", player.getMaxHealth());
            info.put("food", player.getFoodData().getFoodLevel());
            info.put("saturation", player.getFoodData().getSaturationLevel());
            info.put("level", player.experienceLevel);
            info.put("gameMode", player.gameMode.getGameModeForPlayer().getName());
            info.put("world", player.level().dimension().location().toString());
            info.put("x", player.getX());
            info.put("y", player.getY());
            info.put("z", player.getZ());
            info.put("ping", player.connection.latency());
            info.put("isSneaking", player.isCrouching());
            info.put("isSprinting", player.isSprinting());
            info.put("isFlying", player.getAbilities().flying);
        }
        return info;
    }
}