package net.ryaas.soulmod.powers.starspawn.basestar;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BaseStarburn extends Entity {
    // We'll store which stage we are: 1,2,3
    private int stage = 1;          // default stage
    private int lifetime = 100;     // will adjust based on stage
    private int damagePerTick = 2;  // will adjust based on stage
    private float explosionRadius = 2.0F;

    private boolean explodedOnSpawn = false;

    public BaseStarburn(EntityType<? extends BaseStarburn> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            // explode on spawn
            if (!explodedOnSpawn) {
                explodedOnSpawn = true;
                doInitialExplosion();
            }

            // burn/damage nearby
            burnNearbyEntities();

            // decrement lifetime
            lifetime--;
            if (lifetime <= 0) {
                discard();
            }
        }
    }

    private void doInitialExplosion() {
        // Explosion that depends on stage
        level().explode(
                this,                // source
                getX(), getY(), getZ(),
                explosionRadius,     // stage-based radius
                false,               // setFire: false
                Level.ExplosionInteraction.MOB
        );

        // Some quick flame particles
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FLAME,
                    getX(), getY(), getZ(),
                    20, 1.0, 1.0, 1.0, 0.01
            );
        }
    }





    private void burnNearbyEntities() {
        double radius = 2.0; // base for AoE detection, or could scale with stage
        // if you want the same area for all stages, keep it 2.0
        // or do e.g. radius = 1.0 + stage; to expand for bigger stages

        AABB area = AABB.unitCubeFromLowerCorner(this.position()).inflate(radius);

        List<LivingEntity> list = level().getEntitiesOfClass(
                LivingEntity.class,
                area,
                e -> e.isAlive()
        );

        for (LivingEntity e : list) {
            // Possibly skip the owner or same team:
            // if (isFriendly(e)) continue;
            e.hurt(damageSources().inFire(), damagePerTick);
            e.setSecondsOnFire(2); // or scale by stage
        }
    }

    /**
     * Called after we create the entity to set stage-based parameters.
     */
    public void setStage(int stage) {
        this.stage = stage;

        switch (stage) {
            case 1:
                this.damagePerTick = 2;
                this.lifetime = 100;
                this.explosionRadius = 2.0F;
                break;
            case 2:
                this.damagePerTick = 4;
                this.lifetime = 140;
                this.explosionRadius = 3.0F;
                break;
            case 3:
            default:
                this.damagePerTick = 6;
                this.lifetime = 180;
                this.explosionRadius = 4.0F;
                break;
        }
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.stage = nbt.getInt("Stage");
        this.damagePerTick = nbt.getInt("DamagePerTick");
        this.lifetime = nbt.getInt("Lifetime");
        this.explosionRadius = nbt.getFloat("ExplosionRadius");
        this.explodedOnSpawn = nbt.getBoolean("ExplodedOnSpawn");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Stage", this.stage);
        nbt.putInt("DamagePerTick", this.damagePerTick);
        nbt.putInt("Lifetime", this.lifetime);
        nbt.putFloat("ExplosionRadius", this.explosionRadius);
        nbt.putBoolean("ExplodedOnSpawn", this.explodedOnSpawn);
    }
}