package net.ryaas.soulmod.powers.voidsong;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import net.ryaas.soulmod.entities.ModEntities;
import net.ryaas.soulmod.network.NetworkHandler;
import net.ryaas.soulmod.network.s2cpackets.S2CSpawnParticlePacket;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public class VoidSong extends Projectile implements GeoAnimatable {

    // -- Constants -----------------------------------------------------
    private static final double FRICTION = 0.98;
    private static final double GRAVITY = 0.0;
    private static final double VACUUM_RADIUS = 4.0;
    private static final float  MAX_DAMAGE = 10.0f;
    private int age = 0;

    // Maximum lifetime: how long until it discards itself
    private int maxLifetime = 100;

    // Distance mechanics
    private static final double MAX_DISTANCE = 8.0D;   // max distance from player
    private static final double MOVE_SPEED = 0.7D;     // blocks per tick outward

    private int vacuumDamageCooldown = 0;

    // Synched data key for finalChargeValue
    private static final EntityDataAccessor<Integer> CHARGE_VALUE =
            SynchedEntityData.defineId(VoidSong.class, EntityDataSerializers.INT);

    // -- Fields --------------------------------------------------------
    private boolean isCharging = false;
    private UUID ownerUUID;

    private boolean isStuck = false;
    private int vacuumTicksRemaining = 0;
    private double stuckX;
    private double stuckY;
    private double stuckZ;

    // Geckolib:
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // ------------------------------------------------------------------
    // REQUIRED CONSTRUCTOR
    // ------------------------------------------------------------------
    public VoidSong(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    // ------------------------------------------------------------------
    // Owner management
    // ------------------------------------------------------------------
    @Override
    public void setOwner(Entity owner) {
        super.setOwner(owner);
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
        } else {
            this.ownerUUID = null;
        }
    }

    // ------------------------------------------------------------------
    // Synched data / Save & Load
    // ------------------------------------------------------------------
    @Override
    protected void defineSynchedData() {
        this.entityData.define(CHARGE_VALUE, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.isCharging = nbt.getBoolean("IsCharging");
        this.setFinalChargeValue(nbt.getInt("FinalCharge"));
        if (nbt.hasUUID("OwnerUUID")) {
            this.ownerUUID = nbt.getUUID("OwnerUUID");
        }

        this.age = nbt.getInt("Age");
        this.maxLifetime = nbt.getInt("MaxLife");


        this.isStuck = nbt.getBoolean("IsStuck");
        this.vacuumTicksRemaining = nbt.getInt("VacuumTicks");
        this.stuckX = nbt.getDouble("StuckX");
        this.stuckY = nbt.getDouble("StuckY");
        this.stuckZ = nbt.getDouble("StuckZ");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putBoolean("IsCharging", this.isCharging);
        nbt.putInt("FinalCharge", this.getFinalChargeValue());
        if (this.ownerUUID != null) {
            nbt.putUUID("OwnerUUID", this.ownerUUID);

        }

        nbt.putInt("Age", this.age);
        nbt.putInt("MaxLife", this.maxLifetime);

        nbt.putBoolean("IsStuck", this.isStuck);
        nbt.putInt("VacuumTicks", this.vacuumTicksRemaining);
        nbt.putDouble("StuckX", this.stuckX);
        nbt.putDouble("StuckY", this.stuckY);
        nbt.putDouble("StuckZ", this.stuckZ);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double camDistSqr) {
        double maxDist = 256.0D;
        return camDistSqr < (maxDist * maxDist);
    }

    // ------------------------------------------------------------------
    // Geckolib setup (if you have animations)
    // ------------------------------------------------------------------
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // no-op
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object o) {
        return 0;
    }

    // ------------------------------------------------------------------
    // Getters / Setters
    // ------------------------------------------------------------------
    public void setCharging(boolean charging) {
        this.isCharging = charging;
        this.setNoGravity(charging); // optional: no gravity while charging
    }

    public boolean isCharging() {
        return this.isCharging;
    }

    public int getFinalChargeValue() {
        return this.entityData.get(CHARGE_VALUE);
    }

    public void setFinalChargeValue(int value) {
        this.entityData.set(CHARGE_VALUE, value);
    }

    public boolean isSucking() {
        return this.isStuck && this.vacuumTicksRemaining > 0;
    }

    public boolean getIsStuck() {
        return this.isStuck;
    }

    private Player getOwnerAsPlayer() {
        if (this.ownerUUID == null) return null;
        if (!(this.level() instanceof ServerLevel serverLevel)) return null;
        return serverLevel.getPlayerByUUID(this.ownerUUID);
    }

    // ------------------------------------------------------------------
    // Main tick logic
    // ------------------------------------------------------------------
    @Override
    public void tick() {
        super.tick();

        // Only run the "logic" on the server side
        if (!level().isClientSide) {
            this.age++;
            Player owner = getOwnerAsPlayer();
            if (owner == null) {
                // No valid owner => remove
                this.discard();
                return;
            }

            if (vacuumDamageCooldown > 0) {
                vacuumDamageCooldown--;
            }

            // If we've been alive too long, remove
            if (this.age >= this.maxLifetime) {
                this.discard();
                return;
            }

            // While charging, skip normal movement
            if (this.isCharging && !this.isStuck) {
                handleCharging();
                return;
            }

            // Normal movement
            if (!this.noPhysics && !this.isStuck) {
                // Pull nearby entities each tick
                doContinuousVacuum(this.position());

                // 1) Get player's eye position & look direction
                Vec3 eyePos = new Vec3(owner.getX(), owner.getEyeY(), owner.getZ());
                Vec3 look   = owner.getLookAngle().normalize();

                // 2) Calculate desired velocity (how fast we want to go)
                double desiredSpeed = MOVE_SPEED;  // e.g. 0.55
                Vec3 desiredVelocity = look.scale(desiredSpeed);

                // 3) Lerp from current velocity to desired velocity for smoother turning
                Vec3 currentVel   = this.getDeltaMovement();
                double blendFactor = 0.3;  // how quickly we "turn" toward the new direction
                Vec3 newVel = currentVel.lerp(desiredVelocity, blendFactor);

                // 4) Figure out where we want to be after this tick
                Vec3 oldPos         = this.position();
                Vec3 idealTargetPos = oldPos.add(newVel);

                // Optional: If you want to clamp max distance from player's eyes
                double nextDist = eyePos.distanceTo(idealTargetPos);
                if (nextDist > MAX_DISTANCE) {
                    Vec3 overshootDir = idealTargetPos.subtract(eyePos).normalize();
                    idealTargetPos    = eyePos.add(overshootDir.scale(MAX_DISTANCE));
                    // Recompute velocity to match the new (clamped) target
                    newVel = idealTargetPos.subtract(oldPos);
                }

                // 5) Raytrace from oldPos to idealTargetPos for collisions / block-break
                ClipContext ctx = new ClipContext(
                        oldPos,
                        idealTargetPos,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        this
                );
                BlockHitResult blockHit = level().clip(ctx);

                // If the player has a "can break blocks" ability
                boolean canBreakBlocks = owner.getPersistentData().getBoolean("AbilitiesBreakBlocks");

                if (blockHit.getType() == HitResult.Type.BLOCK) {
                    // Example: break the block you collide with
                    if (canBreakBlocks) {
                        level().destroyBlock(blockHit.getBlockPos(), true);
                    }
                    // If you *don't* want to pass through the block:
                    // idealTargetPos = blockHit.getLocation();
                    // newVel         = idealTargetPos.subtract(oldPos);
                }

                // Also break blocks in a small radius at the final position
                if (canBreakBlocks) {
                    breakBlocksInRadius(idealTargetPos);
                }

                // 6) Move the projectile
                this.move(MoverType.SELF, newVel);
                this.setPos(idealTargetPos.x, idealTargetPos.y, idealTargetPos.z);
                this.setDeltaMovement(newVel);
                this.hasImpulse = true; // Helps sync motion to clients

                // 7) Spawn particles if the charge is high enough
                int charge = getFinalChargeValue();
                if (charge > 60) {
                    spawnOrbitingParticleTrail(2);
                } else if (charge > 30) {
                    spawnOrbitingParticleTrail(1);
                }

                // 8) Check collisions with entities (if you want to damage them, etc.)
                List<Entity> hits = level().getEntities(
                        this,
                        getBoundingBox().inflate(0.05),
                        e -> e.isAlive() && e != this && !isOwner(e)
                );
                // e.g., damage logic can go here if needed
            }
        }
    }

    // ------------------------------------------------------------------
    // handleCharging()
    // ------------------------------------------------------------------
    private void handleCharging() {
        // 1) Keep bounding box tiny => no collisions
        setBoundingBox(new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()));

        // 2) Increase finalChargeValue
        if (this.ownerUUID != null && this.level() instanceof ServerLevel) {
            Player owner = ServerLifecycleHooks.getCurrentServer()
                    .getPlayerList()
                    .getPlayer(this.ownerUUID);

            if (owner != null) {
                int newCharge = Math.min(this.getFinalChargeValue() + 1, 100);
                this.setFinalChargeValue(newCharge);

                this.maxLifetime = 60 + (newCharge);

                // 3) Position entity near player's face
                Vec3 eyePos  = owner.getEyePosition();
                Vec3 lookDir = owner.getLookAngle();
                double offsetDistance = 1.0;
                double offsetDown     = 0.25;

                Vec3 targetPos = eyePos
                        .subtract(0, offsetDown, 0)
                        .add(lookDir.scale(offsetDistance));

                setPos(targetPos.x, targetPos.y, targetPos.z);
            }
        }
    }

    // ------------------------------------------------------------------
    // doContinuousVacuum: pulls entities in each tick
    // ------------------------------------------------------------------

    private void doContinuousVacuum(Vec3 center) {
        double r = VACUUM_RADIUS;
        AABB area = new AABB(
                center.x - r, center.y - r, center.z - r,
                center.x + r, center.y + r, center.z + r
        );

        // Pull all nearby living entities
        List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity target : nearby) {
            if (isOwner(target)) continue;

            // 1) Pull them every tick
            Vec3 toCenter = center.subtract(target.position());
            double dist = Math.max(0.01, toCenter.length());
            Vec3 dir = toCenter.normalize();
            double pullStrength = 2.0;
            target.setDeltaMovement(dir.scale(pullStrength));
        }

        // 2) Damage all of them only when the cooldown is 0
        if (vacuumDamageCooldown == 0 && !nearby.isEmpty()) {
            for (LivingEntity target : nearby) {
                if (isOwner(target)) continue;

                float damagePerTick = 1.0F; // or however much you want
                target.hurt(this.damageSources().magic(), damagePerTick);

                // Reset invulnerability so we can damage again next time
                target.invulnerableTime = 0;
            }

            // Set tick cooldown
            vacuumDamageCooldown = 10;
        }
    }

    // ------------------------------------------------------------------
    // breakBlocksInRadius: radius determined by charge
    // ------------------------------------------------------------------
    private void breakBlocksInRadius(Vec3 center) {
        double radius = getBreakRadiusByCharge();
        int minX = (int) Math.floor(center.x - radius);
        int maxX = (int) Math.floor(center.x + radius);
        int minY = (int) Math.floor(center.y - radius);
        int maxY = (int) Math.floor(center.y + radius);
        int minZ = (int) Math.floor(center.z - radius);
        int maxZ = (int) Math.floor(center.z + radius);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    double dist = distanceFromBlockPos(pos, center);
                    if (dist <= radius) {
                        // Example check for unbreakable blocks, if desired:
                        // float hardness = level().getBlockState(pos).getDestroySpeed(level(), pos);
                        // if (hardness < 0.0f || hardness > 50.0f) {
                        //     continue; // skip bedrock or very hard blocks
                        // }

                        this.level().destroyBlock(pos, true);
                    }
                }
            }
        }
    }

    /**
     * Manual distance calc from the center of a blockpos to a Vec3.
     */
    private double distanceFromBlockPos(BlockPos pos, Vec3 center) {
        double dx = (pos.getX() + 0.5) - center.x;
        double dy = (pos.getY() + 0.5) - center.y;
        double dz = (pos.getZ() + 0.5) - center.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Get the destruction radius based on the charge level.
     * e.g.: <30 = 1.0, >=30 = 2.0, >=60 = 3.0, >=100 = 5.0
     */
    private double getBreakRadiusByCharge() {
        int charge = getFinalChargeValue();
        if (charge >= 100) {
            return 5.0;
        } else if (charge >= 60) {
            return 3.0;
        } else if (charge >= 30) {
            return 2.0;
        } else {
            return 1.0;
        }
    }

    // ------------------------------------------------------------------
    // doVacuumDamage: one-time big damage vacuum (if you want it)
    // ------------------------------------------------------------------
    private void doVacuumDamage(Vec3 center) {
        double r = VACUUM_RADIUS;
        AABB area = new AABB(center.x - r, center.y - r, center.z - r,
                center.x + r, center.y + r, center.z + r);

        List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class, area);
        float damage = calculateDamageFromCharge(this.getFinalChargeValue());

        for (LivingEntity target : nearby) {
            if (isOwner(target)) continue;

            Vec3 toCenter = center.subtract(target.position()).normalize();
            double dist = Math.max(0.01, center.distanceTo(target.position()));
            double pullStrength = 0.5;
            double scaled = pullStrength / dist;
            target.setDeltaMovement(target.getDeltaMovement().add(toCenter.scale(scaled)));

            target.hurt(this.damageSources().magic(), damage);
        }
    }

    /**
     * Basic damage formula: up to 10 damage at 100 charge
     */
    private float calculateDamageFromCharge(int c) {
        float factor = Math.min(c / 100f, 1f);
        return MAX_DAMAGE * factor;
    }

    // ------------------------------------------------------------------
    // Particle spawning
    // ------------------------------------------------------------------
    private void spawnOrbitingParticleTrail(int particleCount) {
        double orbitRadius = 1.5;
        double rotationSpeed = 1.0;
        double rotation = this.tickCount * rotationSpeed;

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i / particleCount) + rotation;

            double offsetX = orbitRadius * Math.cos(angle);
            double offsetY = orbitRadius * Math.sin(angle);

            double px = this.getX() + offsetX;
            double py = this.getY() + offsetY;
            double pz = this.getZ();

            double velocityScale = 0.005;
            double vx = -velocityScale * Math.sin(angle);
            double vy =  velocityScale * Math.cos(angle);
            double vz =  0.0;

            String particleId = "minecraft:witch";

            S2CSpawnParticlePacket packet = new S2CSpawnParticlePacket(
                    this.ownerUUID,
                    particleId,
                    px, py, pz,
                    vx, vy, vz
            );
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> this),
                    packet
            );
        }
    }

    // ------------------------------------------------------------------
    // Example Fireball spawns if you need them
    // ------------------------------------------------------------------
    private void spawnOneFireball(Vec3 center) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        VoidSongProjectile single = new VoidSongProjectile(
                ModEntities.VOIDSONG_PROJ.get(),
                serverLevel,
                false,
                center
        );
        single.moveTo(center.x, center.y, center.z, 0, 0);
        single.setDeltaMovement(new Vec3(0, 0.2, 0));
        serverLevel.addFreshEntity(single);
    }

    private void spawn2Fireballs(Vec3 center) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        VoidSongProjectile single = new VoidSongProjectile(
                ModEntities.VOIDSONG_PROJ.get(),
                serverLevel,
                true,
                center
        );
        single.moveTo(center.x, center.y, center.z, 0, 0);
        single.setDeltaMovement(new Vec3(0, 0.2, 0));
        serverLevel.addFreshEntity(single);
    }

    // ------------------------------------------------------------------
    // Helper: check if entity is the owner
    // ------------------------------------------------------------------
    private boolean isOwner(Entity e) {
        if (this.ownerUUID == null || e == null) return false;
        return this.ownerUUID.equals(e.getUUID());
    }
}
