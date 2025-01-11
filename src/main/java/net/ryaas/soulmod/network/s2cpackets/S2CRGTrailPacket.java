package net.ryaas.soulmod.network.s2cpackets;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.ryaas.soulmod.assisting.visuals.ModParticleTypes;

import java.util.function.Supplier;

public class S2CRGTrailPacket {

    private final double oldX, oldY, oldZ;  // previous position
    private final double newX, newY, newZ;  // current position

    // Server uses this constructor, passing old + new coords
    public S2CRGTrailPacket(double oldX, double oldY, double oldZ,
                            double newX, double newY, double newZ) {
        this.oldX = oldX;
        this.oldY = oldY;
        this.oldZ = oldZ;
        this.newX = newX;
        this.newY = newY;
        this.newZ = newZ;
    }

    // Decode (deserialize) from the network buffer
    public static S2CRGTrailPacket decode(FriendlyByteBuf buf) {
        double oldX = buf.readDouble();
        double oldY = buf.readDouble();
        double oldZ = buf.readDouble();
        double newX = buf.readDouble();
        double newY = buf.readDouble();
        double newZ = buf.readDouble();
        return new S2CRGTrailPacket(oldX, oldY, oldZ, newX, newY, newZ);
    }

    // Encode (serialize) to the network buffer
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(oldX);
        buf.writeDouble(oldY);
        buf.writeDouble(oldZ);
        buf.writeDouble(newX);
        buf.writeDouble(newY);
        buf.writeDouble(newZ);
    }

    // Handle on the client side
    @OnlyIn(Dist.CLIENT)
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                spawnInterpolatedBeam(level, oldX, oldY, oldZ, newX, newY, newZ);
            }
        });
        context.setPacketHandled(true);
    }

    /**
     * Spawns a "beam" of particles by interpolating between oldPos and newPos.
     * This ensures no gaps, even if the entity moves quickly.
     */
    private void spawnInterpolatedBeam(Level level,
                                       double oldX, double oldY, double oldZ,
                                       double newX, double newY, double newZ) {

        // 1) Compute star's displacement (old->new). This simulates star's direction.
        double starDx = newX - oldX;
        double starDy = newY - oldY;
        double starDz = newZ - oldZ;


        // 2) We'll spawn multiple segments from old->new to form a smooth line
        int segments = 8;

        for (int i = 0; i <= segments; i++) {
            double t = (double) i / (double) segments; // from 0..1
            double px = Mth.lerp(t, oldX, newX);
            double py = Mth.lerp(t, oldY, newY);
            double pz = Mth.lerp(t, oldZ, newZ);

            // 3) Let's add a small random offset if we want a flame "spread"
            // e.g., 0.2 =>  +-0.1 in each axis
            double offsetFactor = 1.5;
            double offsetX = (level.random.nextDouble() - 0.5) * offsetFactor;
            double offsetY = (level.random.nextDouble() - 0.5) * offsetFactor;
            double offsetZ = (level.random.nextDouble() - 0.5) * offsetFactor;

            // 4) Each flame moves opposite the star's direction (negative starDx,etc.)
            //    but slower. Let's pick a random scale between 0.1 - 0.3
            double randomScale = 1.0+ (level.random.nextDouble() * 0.5); // 0.1..0.3
            double vx = -starDx * randomScale;
            double vy = -starDy * randomScale;
            double vz = -starDz * randomScale;

            // 5) Spawn your flame-like particle
            level.addParticle(
                    ModParticleTypes.RED_ORB.get(),  // or any flame particle
                    px + offsetX,
                    py + offsetY,
                    pz + offsetZ,
                    vx, vy, vz
            );
            level.addParticle(
                    ParticleTypes.FLAME,  // or any flame particle
                    px + offsetX,
                    py + offsetY,
                    pz + offsetZ,
                    vx, vy, vz
            );
        }
    }
}