package net.ryaas.soulmod.powers.starspawn.basestar;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.ryaas.soulmod.entities.ModEntities;
import net.ryaas.soulmod.event.BaseStarExplosion;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BaseStar extends Entity implements GeoAnimatable {
    // Sync: how many ticks of charging so far
    private static final EntityDataAccessor<Integer> CHARGE_LEVEL =
            SynchedEntityData.defineId(BaseStar.class, EntityDataSerializers.INT);

    // If you want an overall max charge, we keep 100
    private static final int MAX_CHARGE_LEVEL = 100;
    private long finalCharge = 0;

    // For press-and-hold logic
    private boolean isCharging = false;

    private UUID ownerUUID;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // region Animations
    // We'll define 4 distinct animations: Idle, Stage1, Stage2, Stage3
    private static final RawAnimation IDLE_ANIM   = RawAnimation.begin().thenLoop("animation.base_star.idle");
    private static final RawAnimation STAGE1_ANIM = RawAnimation.begin().thenLoop("animation.base_star.charge1");
    private static final RawAnimation STAGE2_ANIM = RawAnimation.begin().thenLoop("animation.base_star.charge2");
    private static final RawAnimation STAGE3_ANIM = RawAnimation.begin().thenLoop("animation.base_star.charge3");
    // endregion

    public BaseStar(EntityType<? extends BaseStar> type, Level level) {
        super(type, level);
        this.noPhysics = true;       // floats / no collisions
        this.setNoGravity(true);      // no gravity
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(CHARGE_LEVEL, 0);
    }

    // region ChargeLevel
    public int getChargeLevel() {
        return this.entityData.get(CHARGE_LEVEL);
    }

    public void setChargeLevel(int charge) {
        this.entityData.set(CHARGE_LEVEL, charge);
    }
    // endregion

    // region Owner / Charging
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
        // If we stop charging, reset to 0 or do something else
        if (!charging) {
            setChargeLevel(0);
        }
    }
    public boolean isCharging() {
        return this.isCharging;
    }
    // endregion

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            // If charging, follow player's hand
            if (isCharging) {
                if (ownerUUID != null) {
                    Player owner = ServerLifecycleHooks.getCurrentServer()
                            .getPlayerList().getPlayer(ownerUUID);
                    if (owner != null) {
                        // Increase charge each tick
                        int newCharge = Math.min(getChargeLevel() + 1, MAX_CHARGE_LEVEL);
                        setChargeLevel(newCharge);

                        // Float near player's hand
                        float yaw = owner.getYRot();
                        double rad = Math.toRadians(yaw);
                        double offsetSide = 0.6;
                        double offsetUp   = 0.8;
                        double x = owner.getX() + Math.cos(rad + Math.PI / 2) * offsetSide;
                        double y = owner.getY() + offsetUp;
                        double z = owner.getZ() + Math.sin(rad + Math.PI / 2) * offsetSide;
                        this.setPos(x, y, z);
                    }
                }
            }
            // Otherwise, if not charging and noPhysics == false, let the star fly
            else if (!noPhysics) {
                // 1) Move by velocity
                move(MoverType.SELF, getDeltaMovement());

                // 2) Basic friction (optional)
                double friction = 0.98;
                setDeltaMovement(getDeltaMovement().multiply(friction, friction, friction));

                // If you want gravity:
                // setDeltaMovement(getDeltaMovement().add(0, -0.04, 0)); // or your chosen gravity

                // 3) Check for block collision
                if (horizontalCollision || verticalCollision) {
                    System.out.println("Discarded Star");
                    createAndTriggerBaseStarExplosion(level(), this);
                    discard();
                    return;
                }

                // 4) Check for entity collisions
                // We'll skip if the entity is the owner or ourselves
                List<Entity> list = level().getEntities(
                        this,
                        getBoundingBox().inflate(0.05),
                        e -> e.isAlive() && e != this && !isOwner(e)
                );

                if (!list.isEmpty()) {
                    // Collided with some entity that isn't us or the owner
                    createAndTriggerBaseStarExplosion(level(), this);
                    discard();
                }
            }
        }

    }

    public static void createAndTriggerBaseStarExplosion(Level level, @Nullable Entity source) {
        if (source == null) return;


        if (!(source instanceof BaseStar star)) return;

        // Decide stage by star's charge
        int c = star.getChargeLevel();
        int stage;
        if (c < 35) stage = 1;
        else if (c < 70) stage = 2;
        else stage = 3;

        // We'll spawn our BaseStarburn entity with the chosen stage
        var burnType = ModEntities.BASE_STARBURN.get();  // adjust if named differently
        BaseStarburn burn = burnType.create(level);
        if (burn != null) {
            // Position at the star's location
            burn.setPos(star.getX(), star.getY(), star.getZ());
            // The BaseStarburn has a setStage(...) or similar method
            burn.setStage(stage);

            level.addFreshEntity(burn);
        }
    }

    /**
     * Returns true if the given entity is our owner.
     */
    private boolean isOwner(Entity e) {
        if (ownerUUID == null) return false;
        return (e instanceof Player) && ownerUUID.equals(e.getUUID());
    }


    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.hasUUID("OwnerUUID")) {
            this.ownerUUID = nbt.getUUID("OwnerUUID");
        }
        this.isCharging = nbt.getBoolean("IsCharging");
        this.setChargeLevel(nbt.getInt("ChargeLevel"));
        this.finalCharge = nbt.getLong("FinalCharge");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        if (this.ownerUUID != null) {
            nbt.putUUID("OwnerUUID", this.ownerUUID);
        }
        nbt.putBoolean("IsCharging", this.isCharging);
        nbt.putInt("ChargeLevel", this.getChargeLevel());
        nbt.putLong("FinalCharge", this.finalCharge);
    }

    /*
     * ================================
     * GECKOLIB: Animation Setup
     * ================================
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "base_star_controller",
                5, // update every 5 ticks
                this::predicate
        ));
    }

    private PlayState predicate(AnimationState<BaseStar> state) {
        int currentCharge = getChargeLevel();

        if (currentCharge == 0) {
            // Not charging => idle
            state.getController().setAnimation(STAGE1_ANIM);
        } else if (currentCharge < 35) {
            // Stage 1
            state.getController().setAnimation(STAGE1_ANIM);
        } else if (currentCharge < 70) {
            // Stage 2
            state.getController().setAnimation(STAGE2_ANIM);
        } else {
            // Stage 3
            state.getController().setAnimation(STAGE3_ANIM);
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
}