package net.ryaas.soulmod.network.s2cpackets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.ryaas.soulmod.powers.soulshot.SoulShot;

import java.util.function.Supplier;


/**
 * Optional: If you only want a traveling projectile, you can delete this entire file.
 * Below is a "dummy" packet that could spawn client-side particles.
 */
public class S2CSoulShotBeamPacket  {
    private final double startX;
    private final double startY;
    private final double startZ;
    private final double endX;
    private final double endY;
    private final double endZ;

    public S2CSoulShotBeamPacket(double sx, double sy, double sz, double ex, double ey, double ez) {
        this.startX = sx;
        this.startY = sy;
        this.startZ = sz;
        this.endX = ex;
        this.endY = ey;
        this.endZ = ez;
    }

    // Decode
    public static S2CSoulShotBeamPacket decode(FriendlyByteBuf buf) {
        double sx = buf.readDouble();
        double sy = buf.readDouble();
        double sz = buf.readDouble();
        double ex = buf.readDouble();
        double ey = buf.readDouble();
        double ez = buf.readDouble();
        return new S2CSoulShotBeamPacket(sx, sy, sz, ex, ey, ez);
    }

    // Encode
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(startX);
        buf.writeDouble(startY);
        buf.writeDouble(startZ);
        buf.writeDouble(endX);
        buf.writeDouble(endY);
        buf.writeDouble(endZ);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // CLIENT-SIDE ONLY
            // If you want optional particles or a short "beam" line, do it here:
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                // e.g., spawn some quick flame particles between (startX, startY, startZ) and (endX, endY, endZ)
                // purely visual
            }
        });
        return true;
    }
}