package net.ryaas.soulmod.network.c2spackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SToggleBlockBreakPacket {
    private final boolean newValue;

    public C2SToggleBlockBreakPacket(boolean newValue) {
        this.newValue = newValue;
    }

    // Deserialize
    public static C2SToggleBlockBreakPacket decode(FriendlyByteBuf buf) {
        Boolean newValue = buf.readBoolean();
        return new C2SToggleBlockBreakPacket((newValue));
    }

    // Serialize
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(newValue);
    }

    public static void handle(C2SToggleBlockBreakPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                // Store per-player setting in persistent data
                sender.getPersistentData().putBoolean("AbilitiesBreakBlocks", message.newValue);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
