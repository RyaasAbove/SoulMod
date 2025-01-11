package net.ryaas.soulmod.network.s2cpackets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.ryaas.soulmod.powers.AbilityCapability;

import java.util.function.Supplier;

public class S2CSyncAbilitiesPacket {
    private final String playerId;
    final CompoundTag abilitiesData;

    public S2CSyncAbilitiesPacket(String playerId, CompoundTag abilitiesData) {
        this.playerId = playerId;
        this.abilitiesData = abilitiesData;
    }

    public static void encode(S2CSyncAbilitiesPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.playerId);
        buf.writeNbt(msg.abilitiesData);
    }

    public static S2CSyncAbilitiesPacket decode(FriendlyByteBuf buf) {
        String playerId = buf.readUtf();
        CompoundTag data = buf.readNbt();
        return new S2CSyncAbilitiesPacket(playerId, data);
    }

    public static void handle(S2CSyncAbilitiesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            System.out.println("Client: received sync packet with data=" + msg.abilitiesData);
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                if (player != null) {
                    player.getCapability(AbilityCapability.PLAYER_ABILITIES_CAPABILITY).ifPresent(cap -> {
                        System.out.println("Client: after deserialize, slot 0=" + cap.getAbilityInSlot(0));
                        cap.deserializeNBT(msg.abilitiesData);

                        // If your GUI screen class is CharSheet and has loadEquippedAbilities()
                        if (mc.screen instanceof net.ryaas.soulmod.screen.CharSheet screen) {
                            screen.loadEquippedAbilities();
                        }
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}