package net.ryaas.soulmod.network.s2cpackets;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;
import java.util.function.Supplier;

public class S2CSpawnParticlePacket {


    private final UUID playerUuid;
    private final String particleId;
    private final double x;
    private final double y;
    private final double z;
    private final double velocityX;
    private final double velocityY;
    private final double velocityZ;

    public S2CSpawnParticlePacket(UUID playerUuid, String particleId, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        this.playerUuid = playerUuid;
        this.particleId = particleId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
    }

    public static S2CSpawnParticlePacket decode(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        String particleId = buf.readUtf(32767);
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        double velocityX = buf.readDouble();
        double velocityY = buf.readDouble();
        double velocityZ = buf.readDouble();
        return new S2CSpawnParticlePacket(uuid, particleId, x, y, z, velocityX, velocityY, velocityZ);
    }

    public static void encode(S2CSpawnParticlePacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.playerUuid);
        buf.writeUtf(packet.particleId, 32767);
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
        buf.writeDouble(packet.velocityX);
        buf.writeDouble(packet.velocityY);
        buf.writeDouble(packet.velocityZ);
    }

    public static void handle(S2CSpawnParticlePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            // Ensure this is executed on the client thread
            if (!ctx.getDirection().getReceptionSide().isClient()) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            // Retrieve the particle type from the registry
            var particleType = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(packet.particleId));

            if (particleType == null) {
                return;
            }

            // Spawn the particle
            // For particles that don't require additional data, you can cast directly
            // Note: Some particles may require specific data; handle accordingly
            if (particleType instanceof SimpleParticleType) {
                mc.level.addParticle((SimpleParticleType) particleType, packet.x, packet.y, packet.z, packet.velocityX, packet.velocityY, packet.velocityZ);
            } else {
                // Handle other particle types or custom particles here
                // For custom particles with data, you might need to construct ParticleOptions instances accordingly
            }
        });
        ctx.setPacketHandled(true);
    }

}
