package net.ryaas.soulmod.powers.voidsong;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.ryaas.soulmod.entities.ModEntities;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.List;
import java.util.UUID;

public class VoidSongProjectile extends Entity implements GeoAnimatable {

    private Vec3 center;
    private boolean spawnNext;   // If true, this projectile will spawn a second on collision

    private UUID ownerUUID;

    public VoidSongProjectile(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noCulling = true;
    }

    /**
     * Custom constructor that allows specifying whether we spawn a second projectile on collision,
     * and the center vector to aim at.
     */
    public VoidSongProjectile(EntityType<?> pEntityType, Level pLevel, boolean spawnNext, Vec3 centerpoint) {
        super(pEntityType, pLevel);
        this.noCulling = true;
        this.spawnNext = spawnNext;
        this.center = centerpoint;  // might be the collision center or target center
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        // SERVER side movement & collision checks
        if (!this.level().isClientSide) {
            // Simple "gravity" example
            Vec3 velocity = this.getDeltaMovement().add(0, -0.1, 0);
            this.setDeltaMovement(velocity);
            this.move(MoverType.SELF, velocity);

            // If block collision, handle it
            if (horizontalCollision || verticalCollision) {
                handleCollision(true);
            }

            // Check entity collisions (excluding owner)
            List<Entity> hits = level().getEntities(
                    this,
                    getBoundingBox().inflate(0.05),
                    e -> e.isAlive() && e != this && !isOwner(e)
            );
            if (!hits.isEmpty()) {
                handleCollision(false);
            }
        }
    }

    /**
     * Called when this projectile collides with a block or entity
     */
    private void handleCollision(boolean isBlockCollision) {
        Vec3 impactPos = this.position();

        // 1) Explode
        explodeAndDealDamage(impactPos, 6, 8);

        // 2) If this projectile is supposed to spawn another, do so
        if (this.spawnNext) {
            spawnNextProjectile();
        }

        // 3) Discard yourself so you don’t keep moving
        discard();
    }

    /**
     * Spawns the second projectile if 'spawnNext' was true.
     */
    private void spawnNextProjectile() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        // Create a second projectile that does NOT spawn a third
        VoidSongProjectile second = new VoidSongProjectile(
                ModEntities.VOIDSONG_PROJ.get(),
                serverLevel,
                false,  // <--- spawnNext = false (no third projectile)
                this.center
        );

        // Put it where the first one collided
        second.moveTo(getX(), getY(), getZ(), 0.0F, 0.0F);

        // Velocity aimed at the center (with some upward arc)
        Vec3 dir = center.subtract(second.position()).normalize();
        Vec3 velocity = dir.scale(0.3).add(0, 0.05, 0);
        second.setDeltaMovement(velocity);

        // Spawn in world
        serverLevel.addFreshEntity(second);
    }

    /**
     * Explosion + AoE damage
     * @param center the explosion center
     * @param explosionPower radius for the explosion
     * @param damage direct damage dealt to entities in the radius
     */
    private void explodeAndDealDamage(Vec3 center, float explosionPower, float damage) {
        if (this.level().isClientSide) return;

        // 1) Vanilla explosion (visual, block damage, knockback) - set to NONE if you don't want to break blocks
        this.level().explode(
                this,
                center.x, center.y, center.z,
                explosionPower,
                Level.ExplosionInteraction.NONE
        );

        // 2) Additional AoE damage
        double r = explosionPower; // or use a different radius
        AABB area = new AABB(center.x - r, center.y - r, center.z - r,
                center.x + r, center.y + r, center.z + r);

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity target : nearby) {
            if (isOwner(target)) continue; // skip owner if desired

            double dist = center.distanceTo(target.position());
            if (dist <= r) {
                // Full damage if inside radius
                target.hurt(this.damageSources().magic(), damage);
            }
        }
    }

    private boolean isOwner(Entity e) {
        return (this.ownerUUID != null && e != null && this.ownerUUID.equals(e.getUUID()));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    // ---------------- Geckolib Stuff ----------------
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return null;
    }

    @Override
    public double getTick(Object o) {
        return 0;
    }
}
