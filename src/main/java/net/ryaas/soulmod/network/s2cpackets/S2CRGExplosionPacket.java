package net.ryaas.soulmod.network.s2cpackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.ryaas.soulmod.assisting.visuals.ModParticleTypes;

import java.util.function.Supplier;

public class S2CRGExplosionPacket {
    private final double x;
    private final double y;
    private final double z;
    private final boolean large;

    public S2CRGExplosionPacket(double x, double y, double z, boolean large) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.large = large;
    }

    // Encode the data into the buffer
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeBoolean(large);
    }

    // Decode the data from the buffer
    public static S2CRGExplosionPacket decode(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        boolean large = buf.readBoolean();
        return new S2CRGExplosionPacket(x, y, z, large);
    }

    // Handle the packet on the client side
    @OnlyIn(Dist.CLIENT)
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                // Spawn custom particles with radius based on 'large' flag
                spawnCustomParticles(level, x, y, z, large);
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnCustomParticles(Level level, double x, double y, double z, boolean large) {
        // Define the number of particles to spawn
        int particleCount = 300; // Adjust as needed

        // Define the max radius based on explosion size
        double maxRadius = large ? 10.0 : 5.0; // Example radii for large and small explosions

        // Define an inner radius for clustering
        double innerRadius = maxRadius * 0.3; // 30% of maxRadius

        for (int i = 0; i < particleCount; i++) {
            // Generate a random direction using spherical coordinates
            double theta = level.random.nextDouble() * 2 * Math.PI; // Angle around the Y-axis
            double phi = Math.acos(2 * level.random.nextDouble() - 1); // Polar angle

            // Generate a random radius within the inner sphere
            double r = level.random.nextDouble() * innerRadius;

            // Convert spherical coordinates to Cartesian coordinates
            double offsetX = r * Math.sin(phi) * Math.cos(theta);
            double offsetY = r * Math.sin(phi) * Math.sin(theta);
            double offsetZ = r * Math.cos(phi);

            // Adjust velocity to make particles move outward from the center
            double velocityX = (offsetX / innerRadius) * 0.2; // Adjust speed as needed
            double velocityY = (offsetY / innerRadius) * 0.2;
            double velocityZ = (offsetZ / innerRadius) * 0.2;

            // Spawn the custom particle at the calculated position with the calculated velocity
            level.addParticle(ModParticleTypes.RED_EXPLOSION.get(), x + offsetX, y + offsetY, z + offsetZ,
                    velocityX, velocityY, velocityZ);
        }

        // **Debug Message for Particle Spawn**
        System.out.println("[DEBUG] Spawned " + particleCount + " clustered particles for RedGiant Explosion. Large: " + large);

        // Define the number of particles to spawn


        // Define the blast radius based on explosion size
        double radius = large ? 10.0 : 5.0; // Example radii for large and small explosions

        for (int i = 0; i < particleCount; i++) {
            // Generate a random direction using spherical coordinates
            double theta = level.random.nextDouble() * 2 * Math.PI; // Angle around the Y-axis
            double phi = Math.acos(2 * level.random.nextDouble() - 1); // Polar angle

            // Fixed radius to place particles on the sphere's surface
            double r = radius;

            // Convert spherical coordinates to Cartesian coordinates
            double offsetX = r * Math.sin(phi) * Math.cos(theta);
            double offsetY = r * Math.sin(phi) * Math.sin(theta);
            double offsetZ = r * Math.cos(phi);

            // Adjust velocity to make particles move outward from the center
            double velocityX = (offsetX / radius) * 0.2; // Adjust speed as needed
            double velocityY = (offsetY / radius) * 0.2;
            double velocityZ = (offsetZ / radius) * 0.2;

            // Spawn the custom particle at the calculated position with the calculated velocity
            level.addParticle(ModParticleTypes.RED_EXPLOSION.get(), x + offsetX, y + offsetY, z + offsetZ,
                    velocityX, velocityY, velocityZ);
            level.addParticle(ModParticleTypes.RED_ORB.get(), x + offsetX, y + offsetY, z + offsetZ,
                    velocityX, velocityY, velocityZ);
        }

        // **Debug Message for Particle Spawn**
        System.out.println("[DEBUG] Spawned " + particleCount + " shell particles for RedGiant Explosion. Large: " + large);
    }
}