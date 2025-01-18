package net.ryaas.soulmod.powers;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RaycastManager {
    // Max range of the hitscan
    private static final double LASER_RANGE = 50.0;

    public static void performRaycast(Player player) {
        // 1) Raycast for blocks
        HitResult blockHit = raycastBlock(player, LASER_RANGE);

        // 2) Raycast for entities
        EntityHitResult entityHit = raycastEntity(player, LASER_RANGE);

        // Figure out which is closer, block or entity:
        double blockDist = blockHit != null ? blockHit.getLocation().distanceTo(player.position()) : Double.MAX_VALUE;
        double entityDist = entityHit != null ? entityHit.getLocation().distanceTo(player.position()) : Double.MAX_VALUE;

        if (blockDist == Double.MAX_VALUE && entityDist == Double.MAX_VALUE) {
            // No hits at all
            return;
        }

        if (blockDist < entityDist) {
            // We hit a block first
            handleBlockHit(player, blockHit);
        } else {
            // We hit an entity first
            handleEntityHit(player, entityHit);
        }
    }

    // Simple block raycast
    public static HitResult raycastBlock(Player player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));

        ClipContext context = new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );

        HitResult result = player.level().clip(context);
        if (result.getType() == HitResult.Type.MISS) {
            return null;
        }
        return result;
    }

    // Raycast for entities (a simple version)
    public static EntityHitResult raycastEntity(Player player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));

        // This uses built-in utility: getEntityHitResult
        // We can do a bounding-box-based search and pick the closest intersecting entity.
        // One approach: net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult
        // Another approach: create an AABB from 'start' to 'end' and check collisions manually.

        return net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                start,
                end,
                player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D),
                // "canHitEntity" predicate: we typically don't want to hit the shooter
                (entity) -> !entity.isSpectator() && entity.isPickable() && entity != player
        );
    }

    private static void handleBlockHit(Player player, HitResult blockHit) {
        // For example, create a sound or particle at the hit location
        // Or break the block if you want. Up to you.
        player.level().playSound(
                null,
                blockHit.getLocation().x,
                blockHit.getLocation().y,
                blockHit.getLocation().z,
                SoundEvents.NOTE_BLOCK_PLING.get(),
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    private static void handleEntityHit(Player player, EntityHitResult entityHit) {
        if (entityHit.getEntity() instanceof LivingEntity living) {
            // Example: do damage
            living.hurt(
                    player.damageSources().playerAttack(player),
                    5.0F // damage value
            );

            // Possibly apply knockback or other effects
            // living.knockback(1.0, player.getX() - living.getX(), player.getZ() - living.getZ());

            // Play sound/particles
            living.level().playSound(
                    null,
                    living.getX(), living.getY(), living.getZ(),
                    net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }
    }
}

