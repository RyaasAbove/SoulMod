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
    private static final double LASER_RANGE = 100.0;

    public static HitResult performRaycast(Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(LASER_RANGE));

        HitResult result = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                start,
                end,
                player.getBoundingBox().expandTowards(look.scale(LASER_RANGE)).inflate(1.0D),
                // "canHitEntity" predicate: we typically don't want to hit the shooter
                (entity) -> !entity.isSpectator() && entity.isPickable() && entity != player
        );


        return result;

    }
}


