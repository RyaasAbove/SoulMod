package net.ryaas.soulmod.network.c2spackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.ryaas.soulmod.entities.ModEntities;
import net.ryaas.soulmod.powers.AbilityCapability;
import net.ryaas.soulmod.powers.AbilityLogic;
import net.ryaas.soulmod.powers.starspawn.basestar.BaseStar;

import java.util.function.Supplier;

public class C2SAbilityKeyStatePacket {
    private final boolean pressed;

    public C2SAbilityKeyStatePacket(boolean pressed) {
        this.pressed = pressed;
    }

    public static void encode(C2SAbilityKeyStatePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.pressed);
    }

    public static C2SAbilityKeyStatePacket decode(FriendlyByteBuf buf) {
        return new C2SAbilityKeyStatePacket(buf.readBoolean());
    }

    public static void handle(C2SAbilityKeyStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Lookup which ability is currently active
            player.getCapability(AbilityCapability.PLAYER_ABILITIES_CAPABILITY).ifPresent(cap -> {
                String abilityId = cap.getActiveAbility();
                if (abilityId == null || abilityId.isEmpty()) {
                    System.out.println("AbilityID is null");
                    return; // No active ability selected
                }

                // If the ability is chargeable or instant, we pass it to our helper
                if (msg.pressed) {
                    // Key pressed
                    if (cap.isChargeable(abilityId)) {
                        System.out.println("DEBUG handle() => abilityId: " + abilityId + ", pressed: " + msg.pressed); // <--
                        AbilityLogic.onPressChargeable(player, abilityId);
                    } else {
                        System.out.println("Active ability = " + cap.getActiveAbility());
                        AbilityLogic.onInstantAbility(player, abilityId);
                    }
                } else {
                    // Key released
                    if (cap.isChargeable(abilityId)) {
                        AbilityLogic.onReleaseChargeable(player, abilityId);
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}