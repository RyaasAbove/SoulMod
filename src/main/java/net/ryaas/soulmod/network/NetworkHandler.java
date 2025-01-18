package net.ryaas.soulmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.ryaas.soulmod.SoulMod;
import net.ryaas.soulmod.network.c2spackets.*;
//import net.ryaas.soulmod.network.c2spackets.C2SActivateSelectedAbilityPacket;
import net.ryaas.soulmod.network.s2cpackets.*;
import net.ryaas.soulmod.powers.AbilityCapability;

import java.util.function.Supplier;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("soulmod", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        INSTANCE.registerMessage(id++, S2CSyncAbilitiesPacket.class, S2CSyncAbilitiesPacket::encode, S2CSyncAbilitiesPacket::decode, S2CSyncAbilitiesPacket::handle);
        INSTANCE.registerMessage(id++, C2SEquipAbilityPacket.class, C2SEquipAbilityPacket::encode, C2SEquipAbilityPacket::decode, C2SEquipAbilityPacket::handle);
        INSTANCE.registerMessage(id++, S2CSoulShotBeamPacket.class, S2CSoulShotBeamPacket::encode, S2CSoulShotBeamPacket::decode, S2CSoulShotBeamPacket::handle);
        INSTANCE.registerMessage(id++, C2SAbilityKeyStatePacket.class, C2SAbilityKeyStatePacket::encode, C2SAbilityKeyStatePacket::decode, C2SAbilityKeyStatePacket::handle);
        INSTANCE.registerMessage(id++, C2SSetActiveAbilityPacket.class, C2SSetActiveAbilityPacket::encode, C2SSetActiveAbilityPacket::decode, C2SSetActiveAbilityPacket::handle);
        INSTANCE.registerMessage(id++, S2CRGExplosionPacket.class, S2CRGExplosionPacket::encode, S2CRGExplosionPacket::decode, S2CRGExplosionPacket::handle);
        INSTANCE.registerMessage(id++, S2CRGTrailPacket.class, S2CRGTrailPacket::encode, S2CRGTrailPacket::decode, S2CRGTrailPacket::handle);
        INSTANCE.registerMessage(id++, S2CSyncPlayerAnimationPacket.class, S2CSyncPlayerAnimationPacket::encode, S2CSyncPlayerAnimationPacket::decode, S2CSyncPlayerAnimationPacket::handle);
        INSTANCE.registerMessage(id++, C2SSetAnimationServerPacket.class, C2SSetAnimationServerPacket::encode, C2SSetAnimationServerPacket::decode, C2SSetAnimationServerPacket::handle);
        INSTANCE.registerMessage(id++, C2SFireRaycastPacket.class, C2SFireRaycastPacket::encode, C2SFireRaycastPacket::decode, C2SFireRaycastPacket::handle);
        System.out.println("[DEBUG] final registered message ID = " + id);

    }


    // Packet class for syncing abilities
    public static class SyncAbilitiesPacket {
        private final String playerId; // UUID as String
        private final CompoundTag abilitiesData;

        public SyncAbilitiesPacket(String playerId, CompoundTag abilitiesData) {
            this.playerId = playerId;
            this.abilitiesData = abilitiesData;
        }

        public static void encode(SyncAbilitiesPacket msg, FriendlyByteBuf buf) {
            buf.writeUtf(msg.playerId);
            buf.writeNbt(msg.abilitiesData);
        }

        public static SyncAbilitiesPacket decode(FriendlyByteBuf buf) {
            String playerId = buf.readUtf();
            CompoundTag data = buf.readNbt();
            return new SyncAbilitiesPacket(playerId, data);
        }
        public static void handle(SyncAbilitiesPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                    Minecraft mc = Minecraft.getInstance();
                    LocalPlayer player = mc.player;
                    if (player != null) {
                        player.getCapability(AbilityCapability.PLAYER_ABILITIES_CAPABILITY).ifPresent(cap -> {
                            cap.deserializeNBT(msg.abilitiesData);

                            // If your GUI is currently open, refresh it
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

    public static void sendExplosionPacket(ServerLevel serverLevel, double x, double y, double z, boolean large) {
        S2CRGExplosionPacket packet = new S2CRGExplosionPacket(x, y, z, large);
        // Use 'dimension()' or 'dimensionKey()' based on your version
        ResourceKey<Level> dimensionKey = serverLevel.dimension();
        INSTANCE.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 64, dimensionKey)), packet);
    }

    public static void sendToPlayersNearbyAndSelf(Object packet, ServerPlayer sender) {
        // Send to players around the sender
        INSTANCE.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        sender.getX(), sender.getY(), sender.getZ(),
                        64.0D, sender.level().dimension()
                )),
                packet
        );
        // Send to the sender, too
        INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> sender),
                packet
        );
    }


}
