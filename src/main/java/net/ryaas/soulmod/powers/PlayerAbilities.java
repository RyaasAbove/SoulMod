package net.ryaas.soulmod.powers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.ryaas.soulmod.network.NetworkHandler;
import net.ryaas.soulmod.network.s2cpackets.S2CSyncAbilitiesPacket;
import net.ryaas.soulmod.powers.IPlayerAbilities;

import java.util.HashSet;
import java.util.Set;

public class PlayerAbilities implements IPlayerAbilities {
    private static final int NUM_SLOTS = 5;

    // For multi-slot:
    private final String[] slots = new String[NUM_SLOTS];

    // For single "active" ability:
    private String activeAbility = "";

    // For press-and-hold logic:
    private long chargeStartTime = 0;

    public PlayerAbilities() {
        // Initialize slots to empty
        for (int i = 0; i < NUM_SLOTS; i++) {
            slots[i] = "";
        }
    }
    @Override
    public void unlockAbility(String abilityId) {
        // If you have an "unlockedAbilities" set, add it there.
        // If not, do nothing:
        // unlockedAbilities.add(abilityId);
    }

    /* =========================
       Multiple Slots
       ========================= */
    @Override
    public int getSlotCount() {
        return NUM_SLOTS;
    }

    @Override
    public String getAbilityInSlot(int slot) {
        if (slot < 0 || slot >= NUM_SLOTS) return "";
        return slots[slot];
    }

    @Override
    public void setAbilityInSlot(int slot, String abilityId) {
        if (slot < 0 || slot >= NUM_SLOTS) return;
        if (abilityId == null) abilityId = "";
        slots[slot] = abilityId;
    }

    /* =========================
       Single "Active" Ability
       ========================= */
    @Override
    public String getActiveAbility() {
        return activeAbility;
    }

    @Override
    public void setActiveAbility(String abilityId) {
        if (abilityId == null) abilityId = "";
        this.activeAbility = abilityId;
    }

    /* =========================
       Press-and-Hold
       ========================= */
    @Override
    public void startCharge(long time) {
        this.chargeStartTime = time;
    }

    @Override
    public long getChargeStart() {
        return chargeStartTime;
    }

    @Override
    public void clearCharge() {
        this.chargeStartTime = 0;
    }

    /* =========================
       NBT Serialization
       ========================= */
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        // 1) Save the slots
        tag.putInt("NumSlots", NUM_SLOTS);
        for (int i = 0; i < NUM_SLOTS; i++) {
            tag.putString("Slot_" + i, slots[i]);
        }

        // 2) Save the active ability
        tag.putString("ActiveAbility", activeAbility);

        // 3) Save the charge time
        tag.putLong("ChargeStart", chargeStartTime);

        return tag;
    }
    public static void syncAbilitiesToClient(ServerPlayer player, IPlayerAbilities abilities) {
        if (player.level().isClientSide) return; // do server-only

        // 1. Serialize the capability data
        CompoundTag data = abilities.serializeNBT();
        String playerId = player.getUUID().toString();

        // 2. Create a packet that sends "playerId" and "data"
        // (You must define a matching S2CSyncAbilitiesPacket or similar)
        NetworkHandler.INSTANCE.sendTo(
                new S2CSyncAbilitiesPacket(playerId, data),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        // 1) Load the slots
        int count = nbt.getInt("NumSlots");
        for (int i = 0; i < NUM_SLOTS && i < count; i++) {
            String ab = nbt.getString("Slot_" + i);
            slots[i] = ab != null ? ab : "";
        }

        // 2) Load the active ability
        this.activeAbility = nbt.getString("ActiveAbility");

        // 3) Load the charge time
        this.chargeStartTime = nbt.getLong("ChargeStart");
    }
}
