package net.ryaas.soulmod.player.soulshot;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.ryaas.soulmod.entities.ModEntities;
import net.ryaas.soulmod.powers.soulshot.SoulShot;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

public class ArmCannon extends Entity implements GeoAnimatable {

    //================================
    //  ANIMATIONS
    //================================
    private static final RawAnimation SPAWN_ANIM =
            RawAnimation.begin().then("animation.armcannon.spawn", Animation.LoopType.PLAY_ONCE);

    private static final RawAnimation CHARGE1_ANIM =
            RawAnimation.begin().then("animation.armcannon.charging1", Animation.LoopType.LOOP);
    private static final RawAnimation CHARGE2_ANIM =
            RawAnimation.begin().then("animation.armcannon.charging2", Animation.LoopType.LOOP);
    private static final RawAnimation CHARGE3_ANIM =
            RawAnimation.begin().then("animation.armcannon.charging3", Animation.LoopType.LOOP);

    private static final RawAnimation FIRE1_ANIM =
            RawAnimation.begin().then("animation.armcannon.fire1", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation FIRE2_ANIM =
            RawAnimation.begin().then("animation.armcannon.fire2", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation FIRE3_ANIM =
            RawAnimation.begin().then("animation.armcannon.fire3", Animation.LoopType.PLAY_ONCE);

    //================================
    //  DATAWATCHERS
    //================================
    private static final EntityDataAccessor<Boolean> DATA_SPAWN_DONE =
            SynchedEntityData.defineId(ArmCannon.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(ArmCannon.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_PLAYING_FIRE =
            SynchedEntityData.defineId(ArmCannon.class, EntityDataSerializers.BOOLEAN);

    // The integer for which firing animation we pick
    private static final EntityDataAccessor<Integer> DATA_FIRE_STATE =
            SynchedEntityData.defineId(ArmCannon.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> DATA_YAW =
            SynchedEntityData.defineId(ArmCannon.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PITCH =
            SynchedEntityData.defineId(ArmCannon.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Integer> DATA_CHARGE_LEVEL =
            SynchedEntityData.defineId(ArmCannon.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> DATA_HAS_FIRING_QUEUED =
            SynchedEntityData.defineId(ArmCannon.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> DATA_LOCAL_FIRE_STATE =
            SynchedEntityData.defineId(ArmCannon.class, EntityDataSerializers.INT);


    //================================
    //  FIELDS
    //================================
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private UUID ownerUUID;
    private boolean attachedToOwner = false;

    // Time-based spawn
    private boolean spawnHasStarted = false;
    private int spawnStartTick = 0;
    private static final int SPAWN_DURATION_TICKS = 10;  // e.g. 0.5s

    // local counters for charging/firing
    private int chargingTicks = 0;
    private static final int CHARGE1_DURATION = 40;
    private static final int CHARGE2_DURATION = 80;

    // We'll store the "fireState" in DATA_FIRE_STATE, but keep a local copy
    private int fireStartTick = 0;
    private static final int FIRE_DURATION_TICKS = 20;  // e.g. 1 second

    public ArmCannon(EntityType<? extends ArmCannon> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true; // We'll handle position
    }

    //================================
    //  GETTERS/SETTERS for Data
    //================================
    public boolean isSpawnDone() {
        return this.entityData.get(DATA_SPAWN_DONE);
    }

    public void setSpawnDone(boolean value) {
        this.entityData.set(DATA_SPAWN_DONE, value);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING);
    }

    public void setCharging(boolean value) {
        this.entityData.set(DATA_CHARGING, value);
    }

    public boolean isPlayingFire() {
        return this.entityData.get(DATA_PLAYING_FIRE);
    }

    public void setPlayingFire(boolean value) {
        this.entityData.set(DATA_PLAYING_FIRE, value);
    }

    public int getFireState() {
        return this.entityData.get(DATA_FIRE_STATE);
    }

    public void setFireState(int val) {
        this.entityData.set(DATA_FIRE_STATE, val);
    }

    public int getChargeLevel() {
        return this.entityData.get(DATA_CHARGE_LEVEL);
    }

    public void setChargeLevel(int lvl) {
        this.entityData.set(DATA_CHARGE_LEVEL, lvl);
    }

    //================================
    //  DEFINE SYNCHED DATA
    //================================
    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_SPAWN_DONE, false);
        this.entityData.define(DATA_CHARGING, false);
        this.entityData.define(DATA_PLAYING_FIRE, false);
        this.entityData.define(DATA_FIRE_STATE, 0);

        this.entityData.define(DATA_YAW, 0f);
        this.entityData.define(DATA_PITCH, 0f);
        this.entityData.define(DATA_CHARGE_LEVEL, 0);
        this.entityData.define(DATA_HAS_FIRING_QUEUED, false);
        this.entityData.define(DATA_LOCAL_FIRE_STATE, 0);
    }

    //================================
    //  OWNER
    //================================
    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Nullable
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



    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setAttachedToOwner(boolean attached) {
        this.attachedToOwner = attached;
    }

    public boolean isAttachedToOwner() {
        return this.attachedToOwner;
    }

    //================================
    //  getter/setter
    //================================
    public boolean getHasFiringAnimBeenQueued() {
        return this.entityData.get(DATA_HAS_FIRING_QUEUED);
    }
    public void setHasFiringAnimBeenQueued(boolean value) {
        this.entityData.set(DATA_HAS_FIRING_QUEUED, value);
    }

    public int getLocalFireState() {
        return this.entityData.get(DATA_LOCAL_FIRE_STATE);
    }
    public void setLocalFireState(int val) {
        this.entityData.set(DATA_LOCAL_FIRE_STATE, val);
    }

    //================================
    //  onReleaseCharge
    //================================
    public void onReleaseCharge() {
        // Stop charging
        setCharging(false);

        // Decide which firing anim
        int theFireState;
        if (chargingTicks < CHARGE1_DURATION) {
            theFireState = 1;
        } else if (chargingTicks < CHARGE2_DURATION) {
            theFireState = 2;
        } else {
            theFireState = 3;
        }

        // Now sync that
        setFireState(theFireState);
        setPlayingFire(true);

        fireStartTick = this.tickCount;
    }

    //================================
    //  TICK
    //================================
    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            applySyncedRotation();
        }

        // If we are charging
        if (isCharging()) {
            chargingTicks++;
            int newCharge = Math.min(getChargeLevel() + 1, 100);
            setChargeLevel(newCharge);
        }

        // If attached
        if (!level().isClientSide && attachedToOwner) {
            Player owner = getOwnerAsPlayer();
            if (owner != null) {
                float playerYaw = owner.yHeadRot;
                float playerPitch = 0;

                double offsetForward = 0.3;
                double offsetRight = 0.1;
                double offsetUp = 0;

                float yawRad = (float) Math.toRadians(playerYaw);
                double forwardX = -Mth.sin(yawRad) * offsetForward;
                double forwardZ = Mth.cos(yawRad) * offsetForward;

                float yawRightRad = yawRad + (float) Math.PI / 2F;
                double rightX = -Mth.sin(yawRightRad) * offsetRight;
                double rightZ = Mth.cos(yawRightRad) * offsetRight;

                double finalX = owner.getX() + forwardX + rightX - 0.2;
                double finalY = owner.getY() + offsetUp;
                double finalZ = owner.getZ() + forwardZ + rightZ - 0.3;

                this.setPos(finalX, finalY, finalZ);
                setRotationFromServer(playerYaw, playerPitch);
            } else {
                discard();
            }

            // If firing, remove after ~1s
            if (isPlayingFire()) {
                int elapsed = this.tickCount - fireStartTick;
                if (elapsed >= FIRE_DURATION_TICKS) {
                    discard();
                }
            }
        }
    }

    private void applySyncedRotation() {
        float syncedYaw = this.entityData.get(DATA_YAW);
        float syncedPitch = this.entityData.get(DATA_PITCH);
        this.setYRot(syncedYaw);
        this.setXRot(syncedPitch);
    }

    public void setRotationFromServer(float yaw, float pitch) {
        this.entityData.set(DATA_YAW, yaw);
        this.entityData.set(DATA_PITCH, pitch);
    }

    //================================
    //  SAVE/LOAD
    //================================
    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.hasUUID("OwnerUUID")) {
            this.ownerUUID = nbt.getUUID("OwnerUUID");
        }
        this.attachedToOwner = nbt.getBoolean("AttachedToOwner");
        this.entityData.set(DATA_YAW, nbt.getFloat("CannonYaw"));
        this.entityData.set(DATA_PITCH, nbt.getFloat("CannonPitch"));

        // spawn logic
        this.spawnHasStarted = nbt.getBoolean("SpawnHasStarted");
        this.spawnStartTick = nbt.getInt("SpawnStartTick");
        setSpawnDone(nbt.getBoolean("SpawnDone"));

        // charge
        setCharging(nbt.getBoolean("IsCharging"));
        this.chargingTicks = nbt.getInt("ChargingTicks");
        setChargeLevel(nbt.getInt("ChargeLevel"));

        // fire
        setPlayingFire(nbt.getBoolean("PlayingFire"));
        setFireState(nbt.getInt("FireState"));
        this.fireStartTick = nbt.getInt("FireStartTick");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        if (ownerUUID != null) {
            nbt.putUUID("OwnerUUID", ownerUUID);
        }
        nbt.putBoolean("AttachedToOwner", attachedToOwner);

        nbt.putFloat("CannonYaw", this.entityData.get(DATA_YAW));
        nbt.putFloat("CannonPitch", this.entityData.get(DATA_PITCH));

        // spawn
        nbt.putBoolean("SpawnHasStarted", spawnHasStarted);
        nbt.putInt("SpawnStartTick", spawnStartTick);
        nbt.putBoolean("SpawnDone", isSpawnDone());

        // charge
        nbt.putBoolean("IsCharging", isCharging());
        nbt.putInt("ChargingTicks", chargingTicks);
        nbt.putInt("ChargeLevel", getChargeLevel());

        // fire
        nbt.putBoolean("PlayingFire", isPlayingFire());
        nbt.putInt("FireState", getFireState());
        nbt.putInt("FireStartTick", fireStartTick);
    }

    //================================
    //  GECKOLIB
    //================================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<ArmCannon> controller =
                new AnimationController<>(this, "armcannon_controller", 5, this::predicate);
        controllers.add(controller);

        //event listener
        controller.setCustomInstructionKeyframeHandler(event -> {
            // The 'event' has 'getKeyframeData()', which is a CustomInstructionKeyframeData object
            // Instead of 'instruction()', you call something like 'getInstructions()'
            var data = event.getKeyframeData();
            System.out.println("[DEBUG] customKeyframeHandler event fired. data=" + data);

            // For example, if the method is called 'instructions()'
            // (Check your version's source or docs for the exact method name)
            String combined = data.getInstructions(); // e.g. "shootProjectile,doMuzzleFlash"
            System.out.println("[DEBUG] instructions raw string= " + combined);

            String[] instructions = combined.split(","); // or your chosen delimiter // or data.getInstructions()


            for (String instr : instructions) {
                System.out.println("[DEBUG] Fired custom instruction: " + instr);

                if ("shootProjectile".equals(instr)) {
                    onShootProjectileEvent();
                }
                // else if ("someOtherInstruction".equals(instr)) ...
            }
        });
    }


    private <E extends GeoAnimatable> PlayState predicate(AnimationState<E> event) {
        AnimationController<?> controller = event.getController();

        // 1) If spawn not done, do time-based logic
        if (!isSpawnDone()) {
            if (!spawnHasStarted) {
                spawnHasStarted = true;
                spawnStartTick = this.tickCount;
                controller.setAnimation(SPAWN_ANIM);
                System.out.println("[DEBUG] Starting spawn at tick=" + this.tickCount);
            } else {
                int elapsed = this.tickCount - spawnStartTick;
                // If user is charging => skip spawn
                if (isCharging()) {
                    setSpawnDone(true);
                    System.out.println("[DEBUG] Skipping spawn because user started charging");
                } else if (elapsed >= SPAWN_DURATION_TICKS) {
                    setSpawnDone(true);
                    System.out.println("[DEBUG] Spawn ended at tick=" + this.tickCount);
                } else {
                    controller.setAnimation(SPAWN_ANIM);
                    System.out.println("[DEBUG] Still in spawn, elapsed=" + elapsed);
                }
            }
            return PlayState.CONTINUE;
        }

        // 2) If charging => show charge anim
        if (isCharging()) {
            int t = this.chargingTicks;
            System.out.println("[DEBUG] CHARGING block, ticks=" + t);
            if (t < CHARGE1_DURATION) {
                controller.setAnimation(CHARGE1_ANIM);
            } else if (t < CHARGE2_DURATION) {
                controller.setAnimation(CHARGE2_ANIM);
            } else {
                controller.setAnimation(CHARGE3_ANIM);
            }
            return PlayState.CONTINUE;
        }

        // 3) If firing => pick fire1,2,3 from data param
        if (!getHasFiringAnimBeenQueued()) {
            setHasFiringAnimBeenQueued(true);
            switch (getFireState()) {
                case 1 -> controller.setAnimation(FIRE1_ANIM);
                case 2 -> controller.setAnimation(FIRE2_ANIM);
                case 3 -> controller.setAnimation(FIRE3_ANIM);
            }
            System.out.println("[DEBUG] Just set the firing anim once!");
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

    private void onShootProjectileEvent() {
        if (!level().isClientSide) {
            System.out.println("[DEBUG] onShootProjectileEvent triggered server-side!");

            // 1) Either find an existing SoulShot if you were spawning it in onPress
            //    or just create a new one. For example:

            // We'll create a brand-new SoulShot:
            SoulShot shot = ModEntities.SOUL_SHOT.get().create(level());
            if (shot == null) {
                System.out.println("[DEBUG] Failed to create SoulShot entity in ArmCannon!");
                return;
            }

            // 2) Set ownership
            shot.setOwnerUUID(this.getOwnerUUID());  // same owner as the cannon

            // 3) Place the projectile near the cannon muzzle or player look
            Player owner = this.getOwnerAsPlayer();
            if (owner != null) {
                // For example, place at player's eye
                shot.setPos(owner.getX(), owner.getEyeY(), owner.getZ());

                // 4) Decide final speed or logic. E.g. do the old "shootSoulShot" math
                int chargeLevel = 100; // or maybe your ArmCannon has a 'chargingTicks' or something
                double baseSpeed = 1.0;
                double maxExtra = 3.0;
                double scale = Math.min(baseSpeed + (chargeLevel / 100.0 * maxExtra),
                        baseSpeed + maxExtra);
                var velocity = owner.getLookAngle().normalize().scale(scale);

                shot.setDeltaMovement(velocity);
                shot.noPhysics = false; // let it move normally
                shot.setNoGravity(false);

                System.out.println("[DEBUG] Cannon launching SoulShot with velocity: " + velocity);
            } else {
                // If no owner found, just spawn at cannon's position
                shot.setPos(this.getX(), this.getY(), this.getZ());
            }

            // 5) Finally, spawn it
            level().addFreshEntity(shot);
        }
    }
}