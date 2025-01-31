package net.ryaas.soulmod.network.c2spackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.ryaas.soulmod.powers.RaycastManager;

import java.util.function.Supplier;

public class C2SFireRaycastPacket {
    private final float charge;
    public C2SFireRaycastPacket(float charge){
        this.charge = charge;
    }
    public static C2SFireRaycastPacket decode(FriendlyByteBuf buf) {
        float charge = buf.readFloat();
        return new C2SFireRaycastPacket(charge);
    }

    public static void encode(C2SFireRaycastPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.charge);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            Player player = ctx.getSender();
            if (player != null) {
                // Server-side logic: do the actual raycast
                RaycastManager.performRaycast(player);
            }
        });
        return true;
    }
}
