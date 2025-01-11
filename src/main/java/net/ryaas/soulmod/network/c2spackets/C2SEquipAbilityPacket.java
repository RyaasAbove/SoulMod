package net.ryaas.soulmod.network.c2spackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.ryaas.soulmod.powers.AbilityCapability;
import net.ryaas.soulmod.powers.IPlayerAbilities;
import net.ryaas.soulmod.powers.PlayerAbilities;

import java.util.function.Supplier;

public class C2SEquipAbilityPacket {
    private final int slotIndex;
    private final String abilityId;

    public C2SEquipAbilityPacket(int slotIndex, String abilityId) {
        this.slotIndex = slotIndex;
        this.abilityId = abilityId;
    }

    public static void encode(C2SEquipAbilityPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slotIndex);
        buf.writeUtf(msg.abilityId);
    }

    public static C2SEquipAbilityPacket decode(FriendlyByteBuf buf) {
        int slot = buf.readVarInt();
        String ability = buf.readUtf();
        return new C2SEquipAbilityPacket(slot, ability);
    }

    public static void handle(C2SEquipAbilityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.get().getSender();
            if (serverPlayer == null) return;

            serverPlayer.getCapability(AbilityCapability.PLAYER_ABILITIES_CAPABILITY).ifPresent(cap -> {
                cap.unlockAbility(msg.abilityId); // now exists!

                if (msg.abilityId == null || msg.abilityId.isEmpty()) {
                    cap.setAbilityInSlot(msg.slotIndex, "");
                    PlayerAbilities.syncAbilitiesToClient(serverPlayer, cap); // now exists!
                } else {
                    if (cap.canEquipAbility(msg.abilityId)) {
                        cap.setAbilityInSlot(msg.slotIndex, msg.abilityId);
                        PlayerAbilities.syncAbilitiesToClient(serverPlayer, cap); // now exists!
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}