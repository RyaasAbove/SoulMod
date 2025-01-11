package net.ryaas.soulmod.powers.rg;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.ryaas.soulmod.assisting.ModSounds;
import net.ryaas.soulmod.network.NetworkHandler;
import net.ryaas.soulmod.network.s2cpackets.S2CRGTrailPacket;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;


import java.util.List;
import java.util.UUID;

public class RedGiant extends Entity implements GeoAnimatable {

    //================================================
    // ENTITY DATA + CONSTANTS
    //================================================
    private static final EntityDataAccessor<Integer> CHARGE_LEVEL =
            SynchedEntityData.defineId(RedGiant.class, EntityDataSerializers.INT);

    public static final EntityDataAccessor<Boolean> IS_LARGE =
            SynchedEntityData.defineId(RedGiant.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> IN_FLIGHT =
            SynchedEntityData.defineId(RedGiant.class, EntityDataSerializers.BOOLEAN);


    private static final int MAX_CHARGE_LEVEL = 65;
    private static final float BLAST_RADIUS_LARGE = 10.0F;
    private static final float BLAST_RADIUS_SMALL = 5.0F;
    private static final double KNOCKBACK_STRENGTH_SMALL = 1.5D;  // Knockback when not fully charged
    private static final double KNOCKBACK_STRENGTH_LARGE = 3.0D;  // Knockback when fully charged
    private static final int FIRE_DURATION = 100; // ticks (5 seconds)

    //================================================
    // FIELDS
    //================================================
    private boolean isCharging = false;     // Is the player currently charging?
    private UUID ownerUUID;
    private boolean inFlight = false;
    private long finalCharge = 0;          // Optionally store the final release value
    // Track whether we’ve finished playing the spawn animation.
    private boolean spawnAnimDone = false;


    // Track the tick when spawn animation first finished.
    private long spawnAnimDoneTick = 0;

    // Time in ticks to play the idle animation before going into full charge.
// 2 seconds = 40 ticks
    private static final int IDLE_CHARGE_DURATION = 40;

    // Geckolib animation caching
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    //================================================
    // ANIMATION CONSTANTS
    //================================================
    private static final RawAnimation SPAWN_ANIM = RawAnimation.begin().then("animation.rg.spawn", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation IDLE_ANIM  = RawAnimation.begin().thenLoop("animation.rg.idle");
    private static final RawAnimation FULLCHARGE_ANIM = RawAnimation.begin().thenLoop("animation.rg.fullcharge");
    private static final RawAnimation IDLEFLY_ANIM = RawAnimation.begin().thenLoop("animation.rg.idlefly");
    private static final RawAnimation FULLCHARGEFLY_ANIM = RawAnimation.begin().thenLoop("animation.rg.fullchargefly");

    private boolean hasPlayedFullChargeSound = false;


    //================================================
    // CONSTRUCTOR
    //================================================
    public RedGiant(EntityType<? extends RedGiant> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(CHARGE_LEVEL, 0);
        this.entityData.define(IS_LARGE, false);
        this.entityData.define(IN_FLIGHT, false);
        this.isCharging = true;
        this.inFlight   = false;

    }

    //================================================
    // TICK
    //================================================
    private double prevPosX, prevPosY, prevPosZ;
    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            //========================================
            // WHILE CHARGING
            //========================================
            if (isCharging) {
                if (ownerUUID != null) {
                    Player owner = ServerLifecycleHooks.getCurrentServer()
                            .getPlayerList().getPlayer(ownerUUID);
                    if (owner != null) {
                        // Increase charge each tick
                        int newCharge = Math.min(getChargeLevel() + 1, MAX_CHARGE_LEVEL);
                        setChargeLevel(newCharge);
                        // 2) If we just reached max charge & haven't played the sound yet
                        if (newCharge >= MAX_CHARGE_LEVEL && !hasPlayedFullChargeSound) {
                            hasPlayedFullChargeSound = true;

                            level().playSound(
                                    null,                        // player = null => audible to all nearby
                                    this.getX(), this.getY(), this.getZ(),
                                    SoundEvents.BLAZE_SHOOT,  // or SoundEvents.NOTE_BLOCK_PLING, etc.
                                    SoundSource.PLAYERS,
                                    1.0F,                        // volume
                                    1.0F                         // pitch
                            );
                        }

                        // Float above the player's head (adjust as needed)
                        double offsetUp = 1.0;
                        this.setPos(owner.getX(),
                                owner.getY() + owner.getEyeHeight() + offsetUp,
                                owner.getZ());
                    }
                }
            }
            //========================================
            // WHEN NOT CHARGING (PROJECTILE IN FLIGHT)
            //========================================
            else if (!this.noPhysics) {
                // 1) Move by current velocity
                move(MoverType.SELF, getDeltaMovement());

                // 2) Apply friction (optional)
                double friction = 0.98;
                Vec3 velocity = getDeltaMovement().multiply(friction, friction, friction);

                // 3) Apply gravity
                //    Increase this value to make the projectile drop faster
                double gravity = 0.05;
                velocity = velocity.add(0, -gravity, 0);

                // 4) Update velocity
                setDeltaMovement(velocity);

                // 5) Collision checks
                if (horizontalCollision || verticalCollision) {
                    handleCollision(true);
                }
                // 6) Entity collision checks
                List<Entity> list = level().getEntities(
                        this,
                        getBoundingBox().inflate(0.05),
                        e -> e.isAlive() && e != this && !isOwner(e)
                );
                if (!list.isEmpty()) {
                    handleCollision(false);
                }
            }


            if (this.entityData.get(IN_FLIGHT)) {
                Vec3 vel = this.getDeltaMovement();
                double dx = vel.x;
                double dy = vel.y;
                double dz = vel.z;

                double speedSq = dx * dx + dy * dy + dz * dz;
                if (speedSq > 1.0E-7) {
                    // === 1) Calculate Yaw (horizontal rotation) ===
                    // Arrow logic: yaw is based on XZ plane, with -90 shift for "face -Z"
                    float newYaw = (float) (Math.toDegrees(Math.atan2(dx, dz))) - 90.0F;

                    // === 2) Calculate Pitch (vertical tilt) ===
                    // pitch is the angle from horizontal =>  + downward, - upward in vanilla
                    double horizontalMag = Math.sqrt(dx * dx + dz * dz);
                    float newPitch = (float) (Math.toDegrees(Math.atan2(dy, horizontalMag)));

                    // === 3) Set the entity's rotation ===
                    //  (A) Snap instantly:
                    // this.setYRot(newYaw);
                    // this.setXRot(newPitch);

                    //  or (B) Smoothly approach for less jitter:
                    float MAX_TURN_SPEED = 20.0F; // degrees per tick
                    float finalYaw = Mth.approachDegrees(this.getYRot(), newYaw, MAX_TURN_SPEED);
                    float finalPitch = Mth.approachDegrees(this.getXRot(), newPitch, MAX_TURN_SPEED);

                    this.setYRot(finalYaw);
                    this.setXRot(finalPitch);

                    // Prevent flicker:
                    this.yRotO = finalYaw;
                    this.xRotO = finalPitch;
                }
            }
            // If in flight, send a packet with BOTH old and new positions
            if (this.entityData.get(IN_FLIGHT)) {
                double newX = this.getX();
                double newY = this.getY() + (this.getBbHeight() / 2.0);
                double newZ = this.getZ();

                NetworkHandler.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY.with(() -> this),
                        new S2CRGTrailPacket(prevPosX, prevPosY, prevPosZ, newX, newY, newZ)
                );


            // Update old pos for next tick
            this.prevPosX = this.getX();
            this.prevPosY = this.getY() + (this.getBbHeight() / 2.0);
            this.prevPosZ = this.getZ();
        }

        }
    }





    /**
     * Handles collision by determining blast radius based on charge level.
     *
     * @param isBlockCollision true if collision was with a block, false otherwise
     */
    private void handleCollision(boolean isBlockCollision) {
        Vec3 impactLocation = new Vec3(this.getX(), this.getY(), this.getZ());

        // Determine if fully charged
        boolean isLarge = getChargeLevel() >= MAX_CHARGE_LEVEL;

        // Update the IS_LARGE flag
        this.entityData.set(IS_LARGE, isLarge);

        // Choose blast radius based on charge state
        float blastRadius = isLarge ? BLAST_RADIUS_LARGE : BLAST_RADIUS_SMALL;

        // Trigger explosion
        createAndTriggerRedGiantExplosion(level(), this, blastRadius, impactLocation, isLarge);

        // Discard the entity after explosion
        discard();

        // Debug logging
        System.out.println("RedGiant collided. BlastRadius: " + blastRadius + ", isLarge: " + isLarge);

    }

    //================================================
    // RELEASING THE CHARGE
    //================================================

    /**
     * Called externally (e.g. from your key handler or packet)
     * when the player stops holding the charge button.
     */
    public void releaseCharge() {
        this.isCharging = false;
        this.entityData.set(IN_FLIGHT, true);  // Or your chosen boolean name

        // The rest of your projectile velocity logic...
        this.noPhysics = false;



        // Calculate velocity from player's look direction
        Player player = getOwnerAsPlayer();
        if (player != null) {
            // 1) Get the player's look vector
            Vec3 lookVec = player.getLookAngle().normalize();

            // 2) (Optional) Clamp upward angle to reduce high arcs
            double maxUp = 0.0; // ~11° above horizontal
            if (lookVec.y > maxUp) {
                lookVec = new Vec3(lookVec.x, maxUp, lookVec.z).normalize();
            }

            // 3) Set speed based on charge level
            double speed = 0.1 * getChargeLevel();

            // 4) Apply velocity
            this.setDeltaMovement(
                    lookVec.x * speed,
                    lookVec.y * speed,
                    lookVec.z * speed
            );

            // Debug logging (optional)
            System.out.println("[DEBUG] RedGiant released with speed=" + speed + " and lookVec=" + lookVec);
        }
    }

    //================================================
    // HELPER METHODS
    //================================================
    private boolean isOwner(Entity e) {
        if (ownerUUID == null) return false;
        return (e instanceof Player) && ownerUUID.equals(e.getUUID());
    }

    public Player getOwnerAsPlayer() {
        if (ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity e = serverLevel.getEntity(ownerUUID);
            if (e instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    //================================================
    // GETTERS/SETTERS
    //================================================
    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setFinalCharge(long finalCharge) {
        this.finalCharge = finalCharge;
    }

    public long getFinalCharge() {
        return finalCharge;
    }

    public void setCharging(boolean charging) {
        this.isCharging = charging;
    }

    public void setFlying(boolean flying) {
        this.inFlight = flying;
    }

    public boolean isCharging() {
        return this.isCharging;
    }

    public int getChargeLevel() {
        return this.entityData.get(CHARGE_LEVEL);
    }

    public void setChargeLevel(int charge) {
        this.entityData.set(CHARGE_LEVEL, charge);
    }

    //================================================
    // SAVE/LOAD
    //================================================
    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.contains("OwnerUUID")) {
            this.ownerUUID = nbt.getUUID("OwnerUUID");
        }
        this.isCharging = nbt.getBoolean("IsCharging");
        this.finalCharge = nbt.getLong("FinalCharge");

        if (nbt.contains("ChargeLevel")) {
            this.setChargeLevel(nbt.getInt("ChargeLevel"));
        }
        this.spawnAnimDone = nbt.getBoolean("SpawnAnimDone");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        if (this.ownerUUID != null) {
            nbt.putUUID("OwnerUUID", this.ownerUUID);
        }
        nbt.putBoolean("IsCharging", this.isCharging);
        nbt.putLong("FinalCharge", this.finalCharge);
        nbt.putInt("ChargeLevel", this.getChargeLevel());
        nbt.putBoolean("SpawnAnimDone", this.spawnAnimDone);
    }

    //================================================
    // GECKOLIB ANIMATION LOGIC
    //================================================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "rg_controller",
                5,
                this::predicate
        ));
    }

    private <E extends GeoAnimatable> PlayState predicate(AnimationState<E> state) {
        AnimationController<?> controller = state.getController();
        boolean clientInFlight = this.entityData.get(IN_FLIGHT);
        // 1) SPawn logic
        if (!this.spawnAnimDone) {

            controller.setAnimation(SPAWN_ANIM);
            if (controller.hasAnimationFinished()) {
                this.spawnAnimDone    = true;
                this.spawnAnimDoneTick = this.tickCount;
            }
            return PlayState.CONTINUE;
        }

        // 2) If it's not in flight yet, that means we are "charging in player’s hands"
        if (!clientInFlight) {
            // A) if we haven't charged for 2 seconds, idle
            if ( (this.tickCount - this.spawnAnimDoneTick) < IDLE_CHARGE_DURATION ) {
                controller.setAnimation(IDLE_ANIM);
            } else {
                // B) beyond 2s of charging => "fullcharge" animation
                controller.setAnimation(FULLCHARGE_ANIM);
            }
            return PlayState.CONTINUE;
        }

        // 3) Otherwise, it *is* in flight
        //    => pick idlefly or fullchargefly depending on isLarge
        if (this.entityData.get(IS_LARGE)) {
            controller.setAnimation(FULLCHARGEFLY_ANIM);
        } else {
            controller.setAnimation(IDLEFLY_ANIM);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object o) {
        return this.tickCount;
    }

    //================================================
    // EXPLOSION / COLLISION LOGIC
    //================================================

    private void createAndTriggerRedGiantExplosion(Level level, RedGiant redGiant, float radius, Vec3 impactLocation, boolean isLarge) {
        // Create explosion with specified radius
        Explosion explosion = new Explosion(level, redGiant, null, null, impactLocation.x(), impactLocation.y(), impactLocation.z(), radius, false, Explosion.BlockInteraction.KEEP);
        explosion.explode();
        explosion.finalizeExplosion(true);

        // Apply additional effects
        applyEffectsToEntities(impactLocation, isLarge);

        // Play explosion sound
        level.playSound(null, impactLocation.x(), impactLocation.y(), impactLocation.z(), ModSounds.RED_GIANT_EXPLOSION.get(), SoundSource.HOSTILE, 1.0F, 1.0F);

        if(!level().isClientSide)
        {
            if(isLarge) {
                NetworkHandler.sendExplosionPacket((ServerLevel) level, impactLocation.x, impactLocation.y, impactLocation.z, true);
            }
            else {
                NetworkHandler.sendExplosionPacket((ServerLevel) level, impactLocation.x, impactLocation.y, impactLocation.z, false);

            }
        }
        // Spawn particles (optional)
        // You can add particle effects here if desired

        // Discard the entity after explosion

    }

    private void applyEffectsToEntities(Vec3 impactLocation, boolean isLarge) {
        float blastRadius = isLarge ? BLAST_RADIUS_LARGE : BLAST_RADIUS_SMALL;
        AABB area = new AABB(
                impactLocation.x() - blastRadius, impactLocation.y() - blastRadius, impactLocation.z() - blastRadius,
                impactLocation.x() + blastRadius, impactLocation.y() + blastRadius, impactLocation.z() + blastRadius
        );

        List<Entity> entities = level().getEntities(null, area);

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity)) continue; // Only affect living entities

            // Skip owner if applicable
            if (isOwner(entity)) {
                continue; // Skip applying effects to the owner
            }

            // Calculate distance factor
            double distanceSq = entity.distanceToSqr(impactLocation);
            double distance = Math.sqrt(distanceSq);
            if (distance > blastRadius) continue;

            double distanceFactor = 1.0 - (distance / blastRadius);

            // Calculate damage based on distance and blast size
            float entityDamage = calculateDamageBasedOnDistance(distanceFactor, isLarge);

            // Apply damage
            ((LivingEntity) entity).hurt(damageSources().explosion(this, this), entityDamage);

            System.out.println("[DEBUG] RedGiant dealt " + entityDamage + " damage to " + entity.getName().getString());

            // Apply knockback
            applyKnockback((LivingEntity) entity, impactLocation, distanceFactor, isLarge);

            // Ignite on fire
            ((LivingEntity) entity).setSecondsOnFire(FIRE_DURATION / 20);
        }
    }

    private float calculateDamageBasedOnDistance(double distanceFactor, boolean isLarge) {
        float scaledDamage = isLarge ? 60.0F : 20.0F; // MAX_DAMAGE : BASE_DAMAGE
        return scaledDamage * (float) distanceFactor;
    }

    private void applyKnockback(LivingEntity entity, Vec3 impactLocation, double distanceFactor, boolean isLarge) {
        Vec3 entityPos = entity.position();
        Vec3 direction = entityPos.subtract(impactLocation).normalize();

        double knockbackStrength = isLarge ? KNOCKBACK_STRENGTH_LARGE : KNOCKBACK_STRENGTH_SMALL;

        double knockbackX = direction.x * knockbackStrength * distanceFactor;
        double knockbackY = direction.y * knockbackStrength * distanceFactor * 0.75; // Reduced Y knockback
        double knockbackZ = direction.z * knockbackStrength * distanceFactor;

        entity.setDeltaMovement(entity.getDeltaMovement().add(knockbackX, knockbackY, knockbackZ));
        entity.hasImpulse = true;
    }
}