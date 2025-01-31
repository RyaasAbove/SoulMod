package net.ryaas.soulmod.network.c2spackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.ryaas.soulmod.powers.Ability;
import net.ryaas.soulmod.powers.AbilityRegistry;

import java.util.UUID;
import java.util.function.Supplier;

public class C2SAbilityUsePacket {
    private final String abilityID;
    private final UUID playerUUID;
    private final long heldDuration;
    private final float cost;
    private final float cooldown;
    private final int type;

    public C2SAbilityUsePacket(String abilityID, UUID playerUUID, long heldDuration, float cost, float cooldown, int type) {
        this.abilityID = abilityID;
        this.playerUUID = playerUUID;
        this.heldDuration = heldDuration;
        this.cost = cost;
        this.cooldown = cooldown;
        this.type = type;
    }

    // Encoding the packet data
    public static void encode(C2SAbilityUsePacket msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.abilityID);
        buffer.writeUUID(msg.playerUUID);
        buffer.writeLong(msg.heldDuration);
        buffer.writeFloat(msg.cost);
        buffer.writeFloat(msg.cooldown);
        buffer.writeInt(msg.type);
    }

    // Decoding the packet data
    public static C2SAbilityUsePacket decode(FriendlyByteBuf buffer) {
        String abilityID = buffer.readUtf();
        UUID playerUUID = buffer.readUUID();
        long heldDuration = buffer.readLong();
        float cost = buffer.readFloat();
        float cooldown = buffer.readFloat();
        int type = buffer.readInt();
        return new C2SAbilityUsePacket(abilityID, playerUUID, heldDuration, cost, cooldown, type);
    }

    // Handling the packet
    public static void handle(C2SAbilityUsePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            // Handle the ability use on the server
            Player player = ctx.getSender();
            if (player != null && player.getUUID().equals(msg.playerUUID)) {
                // Retrieve the ability from the registry
                Ability ability = AbilityRegistry.getAbility(msg.abilityID);
                if (ability != null) {
                    // Execute the ability with the provided charge
                    float charge = Math.min(msg.heldDuration / 20.0F, 5.0F); // Example normalization
                    ability.execute(player, charge);

                    // Apply cooldown if necessary
                    // Implement cooldown management here
                } else {
                    // Handle unknown ability ID (optional)
                    System.out.println("Unknown ability ID: " + msg.abilityID);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
