package net.ryaas.soulmod.powers.darkspark;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.ryaas.soulmod.entities.ModEntities;

import java.util.List;
import java.util.UUID;

public class DarkSpark extends Entity implements IEntityAdditionalSpawnData {

    // How many ticks this spark remains alive
    private static final int MAX_LIFE = 10;
    private int life;

    // Used for the bolt’s random “zigzag” shape
    public long seed;
    private int flashes;

    // If true, does no damage or side effects (for demonstration)
    private boolean visualOnly = false;
    private int chargeLevel = 0;

    // Bolt extends from startPos to endPos
    private Vec3 startPos;
    private UUID ownerUUID;
    private Vec3 endPos;

    /**
     * Default constructor required by EntityType.
     */
    public DarkSpark(EntityType<? extends DarkSpark> type, Level level) {
        super(type, level);
        this.noCulling = true; // Don’t cull out of view frustum
        this.life = MAX_LIFE;

        // Random seed for the shape
        this.seed = this.random.nextLong();
        this.flashes = this.random.nextInt(3) + 1;

        // Default start/end if not specified
        this.startPos = Vec3.ZERO;
        this.endPos = Vec3.ZERO;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        // Tells Forge to create a spawn packet that calls writeSpawnData on the server
        // and readSpawnData on the client
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        // This runs on the server side,
        // sending start/end to the client
        buf.writeDouble(startPos.x);
        buf.writeDouble(startPos.y);
        buf.writeDouble(startPos.z);

        buf.writeDouble(endPos.x);
        buf.writeDouble(endPos.y);
        buf.writeDouble(endPos.z);
        buf.writeInt(chargeLevel);

    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        // This runs on the client side,
        // receiving start/end from the server
        double sx = buf.readDouble();
        double sy = buf.readDouble();
        double sz = buf.readDouble();

        double ex = buf.readDouble();
        double ey = buf.readDouble();
        double ez = buf.readDouble();



        this.chargeLevel = buf.readInt();

        this.startPos = new Vec3(sx, sy, sz);
        this.endPos = new Vec3(ex, ey, ez);

        // Recalculate midpoint
        Vec3 mid = startPos.add(endPos).scale(0.5);
        setPos(mid.x, mid.y, mid.z);
    }



    public void setOwner(Player player) {
        this.ownerUUID = player.getUUID();
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    /**
     * Custom constructor to set the start and end points for the bolt.
     */
    public DarkSpark(EntityType<? extends DarkSpark> type, Level level,
                           Vec3 startPos, Vec3 endPos) {
        this(type, level);
        this.seed = this.random.nextLong();
        this.startPos = startPos;
        this.endPos = endPos;

        // Position the entity in the middle so it stays loaded
        Vec3 midpoint = startPos.add(endPos).scale(0.5);
        this.setPos(midpoint.x, midpoint.y, midpoint.z);
    }

    public void setChargeLevel(int chargeLevel) {
        this.chargeLevel = chargeLevel;
    }

    public int getChargeLevel() {
        return this.chargeLevel;
    }

    @Override
    public void tick() {
        super.tick();

        // Typical lifespan logic, etc.
        this.life--;

        if (this.level().isClientSide) {
//            spawnBoltParticles();
        }

        if (!this.level().isClientSide) {
            // Let's do a bounding box check or a line check from start to end
            double stepSize = 1.0;
            Vec3 start = this.getStartPos();
            Vec3 end = this.getEndPos();
            Vec3 dir = end.subtract(start).normalize();
            double dist = start.distanceTo(end);

            for (double i = 0; i < dist; i += stepSize) {
                Vec3 point = start.add(dir.scale(i));
                // A small bounding box around the line
                AABB checkAABB = new AABB(
                        point.x - 0.3, point.y - 0.3, point.z - 0.3,
                        point.x + 0.3, point.y + 0.3, point.z + 0.3
                );

                List<Entity> entitiesHit = this.level().getEntities(this, checkAABB);
                for (Entity e : entitiesHit) {
                    // Skip the caster if e == caster
                    if (e.getUUID().equals(ownerUUID)) {
                        continue;
                    }

                    if (e instanceof LivingEntity living && !e.getUUID().equals(ownerUUID)) {
                        float baseDamage = 1.0F;
                        float scaledDamage = baseDamage + this.chargeLevel * 1.0F;
                        living.hurt(damageSources().magic(), scaledDamage);
                        living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0));
                    }
                }
            }
        }

        // Lifespan removal
        if (this.life < 0) {
            if (this.flashes == 0) {
                this.discard();
            } else if (this.life < -random.nextInt(5)) {
                this.flashes--;
                this.life = 1;
                this.seed = random.nextLong();
            }
        }
    }

    public static void shootDarkSparkBolt(Level level, Player player, int finalCharge) {
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 look = player.getLookAngle();
        double distance = 20.0;
        Vec3 end = start.add(look.scale(distance));

        // Create the lightning-like entity
        DarkSpark spark = new DarkSpark(ModEntities.DARKSPARK.get(), level, start, end);

        // **IMPORTANT**: set the charge level so we actually get scaled damage
        spark.setChargeLevel(finalCharge);

        spark.setOwner(player);

        level.addFreshEntity(spark);
    }


//    private void spawnBoltParticles() {
//        Vec3 start = this.getStartPos();
//        Vec3 end = this.getEndPos();
//        double distance = start.distanceTo(end);
//
//        // Decide how "dense" you want the particles.
//        // If you step by 0.5, you'll place a particle roughly every 0.5 blocks.
//        double stepSize = 0.5;
//
//        // Normalize the direction
//        Vec3 dir = end.subtract(start).normalize();
//
//        for (double d = 0; d < distance; d += stepSize) {
//            // The exact point along the bolt
//            Vec3 point = start.add(dir.scale(d));
//
//            // If you want some random jitter around that line, add small random offsets:
//            double offsetX = (this.random.nextDouble() - 0.5) * 0.1; // ±0.05
//            double offsetY = (this.random.nextDouble() - 0.5) * 0.1;
//            double offsetZ = (this.random.nextDouble() - 0.5) * 0.1;
//
//            // The final position for the particle
//            double px = point.x + offsetX;
//            double py = point.y + offsetY;
//            double pz = point.z + offsetZ;
//
//            // Add the particle: e.g. "end rod" for sparkly magic,
//            // Or "smoke", "flame", or a custom particle
//            this.level().addParticle(
//                    ParticleTypes.GLOW,
//                    px, py, pz,
//                    0.0, 0.0, 0.0 // velocity
//            );
//        }
//    }
    @Override
    protected void defineSynchedData() {
        // Not used in this example
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // If you want to load data (e.g. from disk)
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // If you want to save data (e.g. to disk)
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        // Larger range to be visible (like vanilla lightning)
        double d0 = 64.0 * getViewScale();
        return distance < d0 * d0;
    }

    public boolean isVisualOnly() {
        return visualOnly;
    }

    public void setVisualOnly(boolean visualOnly) {
        this.visualOnly = visualOnly;
    }

    public Vec3 getStartPos() {
        return startPos;
    }

    public Vec3 getEndPos() {
        return endPos;
    }
}