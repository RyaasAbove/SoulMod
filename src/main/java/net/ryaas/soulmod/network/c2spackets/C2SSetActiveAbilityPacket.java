package net.ryaas.soulmod.network.c2spackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.ryaas.soulmod.powers.AbilityCapability;

import java.util.function.Supplier;

public class C2SSetActiveAbilityPacket {
    private final String abilityId;

    public C2SSetActiveAbilityPacket(String abilityId) {
        this.abilityId = abilityId;
    }

    public static void encode(C2SSetActiveAbilityPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.abilityId);
    }

    public static C2SSetActiveAbilityPacket decode(FriendlyByteBuf buf) {
        return new C2SSetActiveAbilityPacket(buf.readUtf());
    }

    public static void handle(C2SSetActiveAbilityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Set the 'activeAbility' in the player's capability
            player.getCapability(AbilityCapability.PLAYER_ABILITIES_CAPABILITY).ifPresent(cap -> {
                cap.setActiveAbility(msg.abilityId);
                System.out.println("[Server] " + player.getName().getString()
                        + " set active ability to " + msg.abilityId);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}