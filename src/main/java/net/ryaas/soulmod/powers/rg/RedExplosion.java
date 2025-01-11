package net.ryaas.soulmod.powers.rg;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.ryaas.soulmod.assisting.ModSounds;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public class RedExplosion extends Entity implements GeoAnimatable {

    //================================================
    // ENTITY DATA + CONSTANTS
    //================================================
    // Damage and Blast Radius Constants
    private static final float DEFAULT_DAMAGE = 20.0F;
    private static final float MAX_DAMAGE = 60.0F;
    private static final float BLAST_RADIUS_LARGE = 10.0F;
    private static final float BLAST_RADIUS_SMALL = 5.0F;
    private static final double KNOCKBACK_STRENGTH = 1.5D;
    private static final int FIRE_DURATION = 100; // ticks (5 seconds)
    private boolean startedAnim = false;

    // Synched data keys - using STRING for OWNER_UUID
    public static final EntityDataAccessor<Float> DAMAGE =
            SynchedEntityData.defineId(RedExplosion.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> BLAST_RADIUS =
            SynchedEntityData.defineId(RedExplosion.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> IS_LARGE =
            SynchedEntityData.defineId(RedExplosion.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> OWNER_UUID =
            SynchedEntityData.defineId(RedExplosion.class, EntityDataSerializers.STRING);

    // Animation Fields
    private boolean spawnAnimDone = false;
    private static final RawAnimation SMALL_EXPLOSION = RawAnimation.begin()
            .then("animation.redexplosion.small", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation LARGE_EXPLOSION = RawAnimation.begin()
            .then("animation.redexplosion.large", Animation.LoopType.PLAY_ONCE);

    // Geckolib animation caching
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    //================================================
    // CONSTRUCTOR
    //================================================
    // Required constructor for EntityType registration
    public RedExplosion(EntityType<? extends RedExplosion> type, Level level) {
        super(type, level);

        // Set entity size to 0 since it's invisible and only for logic/animations
        this.setBoundingBox(new AABB(this.getX(), this.getY(), this.getZ(), this.getX(), this.getY(), this.getZ()));
        this.noPhysics = true; // Entity doesn't move
    }

    //================================================
    // SYNCHED DATA
    //================================================
    @Override
    protected void defineSynchedData() {
        this.entityData.define(DAMAGE, DEFAULT_DAMAGE);
        this.entityData.define(BLAST_RADIUS, BLAST_RADIUS_SMALL);
        this.entityData.define(IS_LARGE, false);
        this.entityData.define(OWNER_UUID, ""); // Initialize OWNER_UUID as empty string
    }

    //================================================
    // TICK
    //================================================
    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            performExplosionLogic();
            this.discard();

        }
    }

    //================================================
    // SAVE/LOAD DATA
    //================================================
    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("Damage")) {
            this.entityData.set(DAMAGE, compoundTag.getFloat("Damage"));
        }
        if (compoundTag.contains("BlastRadius")) {
            this.entityData.set(BLAST_RADIUS, compoundTag.getFloat("BlastRadius"));
        }
        if (compoundTag.contains("IsLarge")) {
            this.entityData.set(IS_LARGE, compoundTag.getBoolean("IsLarge"));
        }
        if (compoundTag.contains("OwnerUUID")) {
            this.entityData.set(OWNER_UUID, compoundTag.getString("OwnerUUID"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putFloat("Damage", this.entityData.get(DAMAGE));
        compoundTag.putFloat("BlastRadius", this.entityData.get(BLAST_RADIUS));
        compoundTag.putBoolean("IsLarge", this.entityData.get(IS_LARGE));
        if (this.entityData.get(OWNER_UUID) != null && !this.entityData.get(OWNER_UUID).isEmpty()) {
            compoundTag.putString("OwnerUUID", this.entityData.get(OWNER_UUID));
        }
    }

    //================================================
    // EXPLOSION LOGIC
    //================================================
    private void performExplosionLogic() {
        Vec3 impactLocation = new Vec3(this.getX(), this.getY(), this.getZ());

        // Apply damage, knockback, and fire to nearby entities
        applyEffectsToEntities(impactLocation);

        // Ignite nearby blocks (optional)


        // Play explosion sound
        level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.RED_GIANT_EXPLOSION.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    private void applyEffectsToEntities(Vec3 impactLocation) {
        float blastRadius = this.entityData.get(BLAST_RADIUS); // Use synchronized BLAST_RADIUS
        AABB area = new AABB(
                impactLocation.x() - blastRadius, impactLocation.y() - blastRadius, impactLocation.z() - blastRadius,
                impactLocation.x() + blastRadius, impactLocation.y() + blastRadius, impactLocation.z() + blastRadius
        );

        List<Entity> entities = level().getEntities(null, area);

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity)) continue; // Only affect living entities

            // Skip owner if applicable
            String ownerUuidString = this.entityData.get(OWNER_UUID);
            if (ownerUuidString != null && !ownerUuidString.isEmpty()) {
                UUID ownerUuid = UUID.fromString(ownerUuidString);
                if (entity.getUUID().equals(ownerUuid)) {
                    continue; // Skip applying effects to the owner
                }
            }

            // Calculate distance factor
            double distanceSq = entity.distanceToSqr(impactLocation);
            double distance = Math.sqrt(distanceSq);
            if (distance > blastRadius) continue;

            double distanceFactor = 1.0 - (distance / blastRadius);

            // Calculate damage based on distance and blast size
            float entityDamage = calculateDamage(distanceFactor);

            // Apply damage
            ((LivingEntity) entity).hurt(damageSources().explosion(this, this), entityDamage);

            // Apply knockback
            applyKnockback((LivingEntity) entity, impactLocation, distanceFactor);

            // Ignite on fire
            ((LivingEntity) entity).setSecondsOnFire(FIRE_DURATION / 20);
        }
    }

    private float calculateDamage(double distanceFactor) {
        boolean isLarge = this.entityData.get(IS_LARGE); // Retrieve from synchronized data
        float scaledDamage = isLarge ? MAX_DAMAGE : DEFAULT_DAMAGE;
        return scaledDamage * (float) distanceFactor;
    }

    private void applyKnockback(LivingEntity entity, Vec3 impactLocation, double distanceFactor) {
        Vec3 entityPos = entity.position();
        Vec3 direction = entityPos.subtract(impactLocation).normalize();

        double knockbackX = direction.x * KNOCKBACK_STRENGTH * distanceFactor;
        double knockbackY = direction.y * KNOCKBACK_STRENGTH * distanceFactor * 0.75; // Reduced Y knockback
        double knockbackZ = direction.z * KNOCKBACK_STRENGTH * distanceFactor;

        entity.setDeltaMovement(entity.getDeltaMovement().add(knockbackX, knockbackY, knockbackZ));
        entity.hasImpulse = true;
    }



    //================================================
    // GECKOLIB ANIMATION LOGIC
    //================================================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "explosion_controller",
                0, // Update interval
                this::explosionPredicate
        ));
    }

    private <E extends GeoAnimatable> PlayState explosionPredicate(AnimationState<E> state) {
        AnimationController<?> controller = state.getController();



        // After spawn, play the appropriate explosion animation
        boolean isLarge = this.entityData.get(IS_LARGE);
        if (isLarge) {
            if(startedAnim != true){
                if (!"animation.rg.explosion_large".equals(controller.getCurrentAnimation())) {
                    controller.setAnimation(LARGE_EXPLOSION);
                    startedAnim = true;
                }
            }

        } else {
            if(startedAnim != true) {
                if (!"animation.rg.explosion_small".equals(controller.getCurrentAnimation())) {
                    controller.setAnimation(SMALL_EXPLOSION);
                    startedAnim = true;

                }
            }
        }

        // Optionally, mark the animation as complete to allow entity discarding if necessary
        if (controller.hasAnimationFinished()) {
            this.discard();
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object o) {
        return 0;
    }

    //================================================
    // REMOVED INCORRECT METHOD
    //================================================
    // Removed the getTick(Object o) method as it's not part of the Entity class hierarchy.
}