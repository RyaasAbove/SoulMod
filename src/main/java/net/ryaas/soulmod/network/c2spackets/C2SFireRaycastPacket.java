package net.ryaas.soulmod.network.c2spackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.ryaas.soulmod.powers.RaycastManager;

import java.util.function.Supplier;

public class C2SFireRaycastPacket {
    public static C2SFireRaycastPacket decode(FriendlyByteBuf buf) {
        // no data needed
        return new C2SFireRaycastPacket();
    }

    public void encode(FriendlyByteBuf buf) {
        // no data needed
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
