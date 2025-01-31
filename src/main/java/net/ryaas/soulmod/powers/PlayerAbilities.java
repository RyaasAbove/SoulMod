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

    // --- Multiple Slots ---
    private final String[] slots = new String[NUM_SLOTS];

    // --- Single "active" ability ---
    private String activeAbility = "";

    // --- Generic charging logic ---
    // Which ability is charging ("" if none)
    private String chargingAbility = "";
    // How many ticks we've been charging
    private int currentChargeTicks = 0;
    // If you want a timestamp approach, you can store it as well
    private long chargeStartTime = 0;

    // --- Constructor ---
    public PlayerAbilities() {
        // Initialize slots to empty
        for (int i = 0; i < NUM_SLOTS; i++) {
            slots[i] = "";
        }
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

    @Override
    public void unlockAbility(String abilityId) {
        // No-op unless you want to track "unlocked" abilities
    }

    /* =========================
       Generic Charging
    ========================= */
    @Override
    public String getChargingAbility() {
        return chargingAbility;
    }

    @Override
    public void setCharging(boolean charging) {
        if (!charging) {
            // stop any charging
            this.chargingAbility = "";
            this.currentChargeTicks = 0;
        }
        // If charging == true, do nothing *here* unless you want to set a default.
        // You can rely on setChargingAbility(...) for the actual ability name
    }
    @Override
    public void setChargingAbility(String abilityId) {
        this.chargingAbility = (abilityId != null) ? abilityId : "";
    }

    @Override
    public boolean isCharging() {
        // If chargingAbility is NOT empty, we say the player is charging something
        return !this.chargingAbility.isEmpty();
    }

    @Override
    public int getChargeTicks() {
        return this.currentChargeTicks;
    }

    @Override
    public void setChargeTicks(int ticks) {
        this.currentChargeTicks = ticks;
    }

    @Override
    public void incrementChargeTicks() {
        this.currentChargeTicks++;
    }

    @Override
    public void execute(Player player, float charge) {

    }

    @Override
    public String getID() {
        return null;
    }

    @Override
    public float getCost() {
        return 0;
    }

    @Override
    public float getCooldown() {
        return 0;
    }

    @Override
    public AbilityType getType() {
        return null;
    }

    /* =========================
       Timestamp Approach
    ========================= */
    @Override
    public void startCharge(long time) {
        this.chargeStartTime = time;
    }

    @Override
    public long getChargeStart() {
        return this.chargeStartTime;
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
        tag.putString("ActiveAbility", this.activeAbility);

        // 3) Save which ability is charging
        tag.putString("ChargingAbility", this.chargingAbility);

        // 4) Save the current charge ticks
        tag.putInt("CurrentChargeTicks", this.currentChargeTicks);

        // 5) Save the chargeStartTime if you're actually using it
        tag.putLong("ChargeStartTime", this.chargeStartTime);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        // 1) Load the slots
        int count = nbt.getInt("NumSlots");
        for (int i = 0; i < NUM_SLOTS && i < count; i++) {
            String ab = nbt.getString("Slot_" + i);
            slots[i] = (ab != null) ? ab : "";
        }

        // 2) Load the active ability
        this.activeAbility = nbt.getString("ActiveAbility");

        // 3) Load charging ability
        this.chargingAbility = nbt.getString("ChargingAbility");

        // 4) Load current charge ticks
        this.currentChargeTicks = nbt.getInt("CurrentChargeTicks");

        // 5) Load the chargeStartTime
        this.chargeStartTime = nbt.getLong("ChargeStartTime");
    }

    /* =========================
       Sync to Client
    ========================= */
    public static void syncAbilitiesToClient(ServerPlayer player, IPlayerAbilities abilities) {
        if (player.level().isClientSide) return; // server-only

        CompoundTag data = abilities.serializeNBT();
        String playerId = player.getUUID().toString();

        NetworkHandler.INSTANCE.sendTo(
                new S2CSyncAbilitiesPacket(playerId, data),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }
}