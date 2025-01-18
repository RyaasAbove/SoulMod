package net.ryaas.soulmod.powers;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireballAbilityLogic  {

    public void onInstantAbility(ServerPlayer player) {
        Level world = player.level();
        if (world.isClientSide()) return;

        Vec3 look = player.getLookAngle();
        double x = player.getX() + look.x;
        double y = player.getEyeY() + 0.5;
        double z = player.getZ() + look.z;

        SmallFireball fireball = new SmallFireball(EntityType.SMALL_FIREBALL, world);
        fireball.setPos(x, y, z);
        fireball.setOwner(player);

        float velocity = 1.5F;
        float inaccuracy = 0.0F;
        fireball.shoot(look.x, look.y, look.z, velocity, inaccuracy);

        world.addFreshEntity(fireball);
        world.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);

        System.out.println("[Server] Fired a fireball!");
    }
}