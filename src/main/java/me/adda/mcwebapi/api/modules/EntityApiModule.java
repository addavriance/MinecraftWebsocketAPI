package me.adda.mcwebapi.api.modules;

import me.adda.mcwebapi.api.BaseApiModule;
import me.adda.mcwebapi.api.annotations.ApiMethod;
import me.adda.mcwebapi.api.annotations.ApiModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

@ApiModule("entity")
public class EntityApiModule extends BaseApiModule {

    @ApiMethod("spawn")
    public Map<String, Object> spawn(String levelId, String entityTypeId, double x, double y, double z) {
        ServerLevel level = getLevel(levelId);
        if (level == null) {
            return createErrorResult("Level not found");
        }

        return executeOnServerThread(() -> {
            try {
                ResourceLocation entityId = ResourceLocation.parse(entityTypeId);
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);

                if (entityType == null) {
                    return createErrorResult("Entity type not found");
                }

                Entity entity = entityType.create(level);
                if (entity == null) {
                    return createErrorResult("Failed to create entity");
                }

                entity.moveTo(x, y, z);
                level.addFreshEntity(entity);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("uuid", entity.getUUID().toString());
                result.put("type", entityTypeId);
                result.put("x", entity.getX());
                result.put("y", entity.getY());
                result.put("z", entity.getZ());
                return result;
            } catch (Exception e) {
                return createErrorResult(e.getMessage());
            }
        });
    }

    @ApiMethod("remove")
    public boolean remove(String levelId, String entityUuid) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return false;

        return executeOnServerThread(() -> {
            try {
                UUID uuid = UUID.fromString(entityUuid);
                Entity entity = level.getEntity(uuid);
                if (entity != null) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    return true;
                }
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });
    }

    @ApiMethod("kill")
    public boolean kill(String levelId, String entityUuid) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return false;

        return executeOnServerThread(() -> {
            try {
                UUID uuid = UUID.fromString(entityUuid);
                Entity entity = level.getEntity(uuid);
                if (entity != null) {
                    entity.kill();
                    return true;
                }
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });
    }

    @ApiMethod("getInfo")
    public Map<String, Object> getInfo(String levelId, String entityUuid) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return null;

        return executeOnServerThread(() -> {
            try {
                UUID uuid = UUID.fromString(entityUuid);
                Entity entity = level.getEntity(uuid);
                if (entity == null) return null;

                Map<String, Object> info = new HashMap<>();
                info.put("uuid", entity.getUUID().toString());
                info.put("type", BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
                info.put("x", entity.getX());
                info.put("y", entity.getY());
                info.put("z", entity.getZ());
                info.put("yaw", entity.getYRot());
                info.put("pitch", entity.getXRot());
                info.put("isAlive", entity.isAlive());
                info.put("isOnGround", entity.onGround());
                info.put("isSilent", entity.isSilent());
                info.put("isGlowing", entity.isCurrentlyGlowing());
                info.put("isInvulnerable", entity.isInvulnerable());
                info.put("fireImmune", entity.fireImmune());
                info.put("remainingFireTicks", entity.getRemainingFireTicks());

                if (entity.hasCustomName()) {
                    info.put("customName", entity.getCustomName().getString());
                }

                Vec3 motion = entity.getDeltaMovement();
                Map<String, Double> velocity = new HashMap<>();
                velocity.put("x", motion.x);
                velocity.put("y", motion.y);
                velocity.put("z", motion.z);
                info.put("velocity", velocity);

                return info;
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    @ApiMethod("getPosition")
    public Map<String, Double> getPosition(String levelId, String entityUuid) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return null;

        return executeOnServerThread(() -> {
            try {
                UUID uuid = UUID.fromString(entityUuid);
                Entity entity = level.getEntity(uuid);
                if (entity == null) return null;

                Map<String, Double> pos = new HashMap<>();
                pos.put("x", entity.getX());
                pos.put("y", entity.getY());
                pos.put("z", entity.getZ());
                return pos;
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    @ApiMethod("teleport")
    public boolean teleport(String levelId, String entityUuid, double x, double y, double z) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return false;

        return executeOnServerThread(() -> {
            try {
                UUID uuid = UUID.fromString(entityUuid);
                Entity entity = level.getEntity(uuid);
                if (entity != null) {
                    entity.teleportTo(x, y, z);
                    return true;
                }
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });
    }

    @ApiMethod("setVelocity")
    public boolean setVelocity(String levelId, String entityUuid, double x, double y, double z) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return false;

        return executeOnServerThread(() -> {
            try {
                UUID uuid = UUID.fromString(entityUuid);
                Entity entity = level.getEntity(uuid);
                if (entity != null) {
                    entity.setDeltaMovement(x, y, z);
                    return true;
                }
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });
    }

    @ApiMethod("getCustomName")
    public String getCustomName(String levelId, String entityUuid) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return null;

        return executeOnServerThread(() -> {
            try {
                UUID uuid = UUID.fromString(entityUuid);
                Entity entity = level.getEntity(uuid);
                if (entity != null && entity.hasCustomName()) {
                    return entity.getCustomName().getString();
                }
                return null;
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    @ApiMethod("setCustomName")
    public boolean setCustomName(String levelId, String entityUuid, String name) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return false;

        return executeOnServerThread(() -> {
            try {
                UUID uuid = UUID.fromString(entityUuid);
                Entity entity = level.getEntity(uuid);
                if (entity != null) {
                    entity.setCustomName(Component.literal(name));
                    entity.setCustomNameVisible(true);
                    return true;
                }
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });
    }

    @ApiMethod("setGlowing")
    public boolean setGlowing(String levelId, String entityUuid, boolean glowing) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return false;

        return executeOnServerThread(() -> {
            try {
                UUID uuid = UUID.fromString(entityUuid);
                Entity entity = level.getEntity(uuid);
                if (entity != null) {
                    entity.setGlowingTag(glowing);
                    return true;
                }
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });
    }

    @ApiMethod("setInvulnerable")
    public boolean setInvulnerable(String levelId, String entityUuid, boolean invulnerable) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return false;

        return executeOnServerThread(() -> {
            try {
                UUID uuid = UUID.fromString(entityUuid);
                Entity entity = level.getEntity(uuid);
                if (entity != null) {
                    entity.setInvulnerable(invulnerable);
                    return true;
                }
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });
    }

    @ApiMethod("setFireTicks")
    public boolean setFireTicks(String levelId, String entityUuid, int ticks) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return false;

        return executeOnServerThread(() -> {
            try {
                UUID uuid = UUID.fromString(entityUuid);
                Entity entity = level.getEntity(uuid);
                if (entity != null) {
                    entity.setRemainingFireTicks(ticks);
                    return true;
                }
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });
    }

    @ApiMethod("getEntitiesInRadius")
    public List<Map<String, Object>> getEntitiesInRadius(String levelId, double x, double y, double z, double radius) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return Collections.emptyList();

        return executeOnServerThread(() -> {
            AABB aabb = new AABB(x - radius, y - radius, z - radius,
                                 x + radius, y + radius, z + radius);

            return level.getEntities((Entity) null, aabb).stream()
                    .map(this::createEntitySummary)
                    .collect(Collectors.toList());
        });
    }

    @ApiMethod("getEntitiesByType")
    public List<Map<String, Object>> getEntitiesByType(String levelId, String entityTypeId) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return Collections.emptyList();

        return executeOnServerThread(() -> {
            ResourceLocation entityId = ResourceLocation.parse(entityTypeId);
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);

            if (entityType == null) {
                return Collections.emptyList();
            }

            return level.getAllEntities().stream()
                    .filter(entity -> entity.getType() == entityType)
                    .map(this::createEntitySummary)
                    .collect(Collectors.toList());
        });
    }

    @ApiMethod("getAllEntities")
    public List<Map<String, Object>> getAllEntities(String levelId) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return Collections.emptyList();

        return executeOnServerThread(() ->
            level.getAllEntities().stream()
                    .map(this::createEntitySummary)
                    .collect(Collectors.toList())
        );
    }

    @ApiMethod("getEntityCount")
    public int getEntityCount(String levelId) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return -1;

        return executeOnServerThread(() -> level.getAllEntities().size());
    }

    @ApiMethod("getEntityCountByType")
    public int getEntityCountByType(String levelId, String entityTypeId) {
        ServerLevel level = getLevel(levelId);
        if (level == null) return -1;

        return executeOnServerThread(() -> {
            ResourceLocation entityId = ResourceLocation.parse(entityTypeId);
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);

            if (entityType == null) {
                return -1;
            }

            return (int) level.getAllEntities().stream()
                    .filter(entity -> entity.getType() == entityType)
                    .count();
        });
    }

    private Map<String, Object> createEntitySummary(Entity entity) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("uuid", entity.getUUID().toString());
        summary.put("type", BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
        summary.put("x", entity.getX());
        summary.put("y", entity.getY());
        summary.put("z", entity.getZ());
        summary.put("isAlive", entity.isAlive());

        if (entity.hasCustomName()) {
            summary.put("customName", entity.getCustomName().getString());
        }

        return summary;
    }

    private Map<String, Object> createErrorResult(String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", error);
        return result;
    }
}
