package net.ryaas.soulmod.powers.soulshot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.ryaas.soulmod.entities.ModEntities;
import net.ryaas.soulmod.powers.AbilityCapability;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public class SoulShot extends Projectile implements GeoAnimatable {
    // ===========================
    // GECKOLIB Fields
    // ===========================
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // ===========================
    // SoulShot Fields
    // ===========================
    private UUID ownerUUID;
    private boolean isCharging = false;
    private boolean inFlight = false;

    // Simple charge level: 0..100
    private int chargeLevel = 0;
    private int maxChargeLevel = 100;

    // Track finalCharge if you want that info (e.g., for damage)
    private long finalCharge = 0;

    // Track last position for “trail” or other effects if desired
    private double prevPosX, prevPosY, prevPosZ;

    // ===========================
    // Constructors
    // ===========================
    public SoulShot(EntityType<? extends SoulShot> entityType, Level level) {
        super(entityType, level);
    }

    /** Convenience constructor if you want to spawn it at X/Y/Z directly. */
    public SoulShot(Level level, double x, double y, double z) {
        this(ModEntities.SOUL_SHOT.get(), level);
        this.setPos(x, y, z);
    }

    private boolean isOwner(Entity e) {
        return ownerUUID != null && ownerUUID.equals(e.getUUID());
    }

    // ===========================
    // Synched Data (unused)
    // ===========================
    @Override
    protected void defineSynchedData() {
        // If you want to sync fields automatically, define them here using entityData.
    }

    // ===========================
    // Tick Logic
    // ===========================
    @Override
    public void tick() {
        super.tick();

        // If we are on the server side, handle logic
        if (!level().isClientSide) {
            // ============== CHARGING ==============
            if (isCharging) {
                if (chargeLevel < maxChargeLevel) {
                    chargeLevel++;
                }
                // Float near the owner => "in-hand" effect
                Player owner = getOwnerAsPlayer();
                if (owner != null) {
                    double offsetUp = -0.5;
                    this.setPos(
                            owner.getX() + 0.5,
                            owner.getY() + owner.getEyeHeight() + offsetUp,
                            owner.getZ() + 0.5
                    );
                }
            }
            // ============== IN FLIGHT ==============
            else if (inFlight) {
                // 1) Move by current velocity
                move(MoverType.SELF, getDeltaMovement());

                // 2) Apply friction (0.98) to slow it slightly
                double friction = 0.98;
                Vec3 velocity = getDeltaMovement().multiply(friction, friction, friction);

                // 3) No gravity => skip
                // If you wanted gravity, you'd do velocity = velocity.add(0, -gravityValue, 0);

                // 4) Update velocity
                setDeltaMovement(velocity);

                // 5) Check block collisions
                if (horizontalCollision || verticalCollision) {
                    handleCollision(true);
                }

                // 6) Check entity collisions
                List<Entity> list = level().getEntities(this, getBoundingBox().inflate(0.05),
                        e -> e.isAlive() && e != this && !isOwner(e));
                if (!list.isEmpty()) {
                    handleCollision(false);
                }

                // 7) Rotate entity to face direction of travel
                rotateToVelocity(velocity);
            }

            // Update old position
            this.prevPosX = this.getX();
            this.prevPosY = this.getY();
            this.prevPosZ = this.getZ();
        }
    }

    /**
     * Called by your ability logic to end charging and start flight.
     */
    public void releaseCharge() {
        this.isCharging = false;
        this.inFlight   = true;
        this.noPhysics  = false; // re-enable normal physics

        // Example: scale speed from 0..30 based on chargeLevel
        double speed = 0.3 * this.chargeLevel;
        Player owner = getOwnerAsPlayer();
        if (owner != null) {
            Vec3 lookVec = owner.getLookAngle().normalize();
            this.setDeltaMovement(lookVec.scale(speed));
            // Sound
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.BLAZE_SHOOT,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    /**
     * Called if we collide with a block or entity.
     * By default, we just discard the projectile.
     * You can add damage or explosion logic here.
     */
    private void handleCollision(boolean isBlockCollision) {
        // Just remove the projectile
        discard();

        // If you want damage or other effects, do it here. E.g.:
        // if (!isBlockCollision) {
        //     // We presumably hit an entity => do damage
        //     // living.hurt(...)
        // }
    }

    /**
     * Smoothly rotates the entity to face the velocity direction.
     */
    private void rotateToVelocity(Vec3 velocity) {
        double dx = velocity.x;
        double dy = velocity.y;
        double dz = velocity.z;
        double speedSq = dx * dx + dy * dy + dz * dz;
        if (speedSq > 1.0E-7) {
            float newYaw = (float)(Math.toDegrees(Math.atan2(dx, dz))) - 90.0F;
            double horizMag = Math.sqrt(dx * dx + dz * dz);
            float newPitch = (float)(Math.toDegrees(Math.atan2(dy, horizMag)));

            float maxTurn = 20.0F; // limit turning speed
            float finalYaw = Mth.approachDegrees(getYRot(), newYaw, maxTurn);
            float finalPitch = Mth.approachDegrees(getXRot(), newPitch, maxTurn);

            setYRot(finalYaw);
            setXRot(finalPitch);

            this.yRotO = finalYaw;
            this.xRotO = finalPitch;
        }
    }

    // ===========================
    // OWNER / CHARGE / GETTERS
    // ===========================
    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public Player getOwnerAsPlayer() {
        if (ownerUUID == null) return null;
        if (this.level() instanceof ServerLevel sLevel) {
            Entity e = sLevel.getEntity(ownerUUID);
            if (e instanceof Player p) {
                return p;
            }
        }
        return null;
    }

    public void setCharging(boolean charging) {
        this.isCharging = charging;
        if (charging) {
            // freeze in place
            this.noPhysics = true;
            setDeltaMovement(Vec3.ZERO);
        }
    }
    public boolean isCharging() {
        return this.isCharging;
    }

    public int getChargeLevel() {
        return this.chargeLevel;
    }




    public void setChargeLevel(int level) {
        this.chargeLevel = level;
    }

    public long getFinalCharge() {
        return finalCharge;
    }
    public void setFinalCharge(long finalCharge) {
        this.finalCharge = finalCharge;
    }

    // ===========================
    // Networking
    // ===========================
    /**
     * Must override to sync entity from server to client properly.
     */


    // ===========================
    // Save/Load
    // ===========================
    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.hasUUID("OwnerUUID")) {
            this.ownerUUID = nbt.getUUID("OwnerUUID");
        }
        this.isCharging = nbt.getBoolean("IsCharging");
        this.inFlight   = nbt.getBoolean("InFlight");
        this.chargeLevel = nbt.getInt("ChargeLevel");
        this.finalCharge = nbt.getLong("FinalCharge");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        if (ownerUUID != null) {
            nbt.putUUID("OwnerUUID", ownerUUID);
        }
        nbt.putBoolean("IsCharging", isCharging);
        nbt.putBoolean("InFlight", inFlight);
        nbt.putInt("ChargeLevel", chargeLevel);
        nbt.putLong("FinalCharge", finalCharge);
    }

    // ===========================
    // GeckoLib Methods
    // ===========================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // If you have animations, define them here
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object o) {
        return this.tickCount;
    }
}
