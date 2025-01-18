package net.ryaas.soulmod.network.c2spackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.ryaas.soulmod.network.NetworkHandler;
import net.ryaas.soulmod.network.s2cpackets.S2CSyncPlayerAnimationPacket;

import java.util.function.Supplier;

public class C2SSetAnimationServerPacket {
    private final int animId;

    /**
     * @param animId e.g. 0 = NONE (stop), 1 = CHARGING, etc.
     */
    public C2SSetAnimationServerPacket(int animId) {
        this.animId = animId;
    }

    public static C2SSetAnimationServerPacket decode(FriendlyByteBuf buf) {
        int animId = buf.readInt();
        return new C2SSetAnimationServerPacket(animId);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.animId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            // The player who sent the packet
            var sender = ctx.getSender();
            if (sender == null) return;

            // Let’s broadcast to all players, including this one
            // We do so by sending SyncPlayerAnimationPacket:
            NetworkHandler.sendToPlayersNearbyAndSelf(
                    new S2CSyncPlayerAnimationPacket(sender.getId(), this.animId),
                    sender
            );
        });
        return true;
    }
}

