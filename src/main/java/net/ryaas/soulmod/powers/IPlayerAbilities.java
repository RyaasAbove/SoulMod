package net.ryaas.soulmod.powers;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Set;





public interface IPlayerAbilities extends INBTSerializable<CompoundTag> {
    /* =========================
       Multiple Slots
    ========================= */
    /**
     * How many ability slots this player can equip.
     */
    int getSlotCount();

    /**
     * Returns the ability ID in the given slot index, or "" if none.
     */
    String getAbilityInSlot(int slot);

    /**
     * Sets the given slot to the specified ability ID, or "" to clear.
     */
    void setAbilityInSlot(int slot, String abilityId);

    /* =========================
       Active Ability
    ========================= */
    /**
     * Which ability is currently set as "active" for press/hold usage.
     */
    String getActiveAbility();

    /**
     * Sets the active ability ID. If "" or null, means none.
     */
    void setActiveAbility(String abilityId);

    /**
     * Potentially used to "unlock" an ability before equipping.
     * Can be a no-op if your design doesn't require unlocking.
     */
    void unlockAbility(String abilityId);

    /* =========================
       Charging Logic
    ========================= */
    /**
     * Returns true if ANY ability is currently charging.
     * Typically driven by "chargingAbility != ''".
     */
    boolean isCharging();

    /**
     * For a generic approach, store which ability is currently charging.
     * If "", then nothing is charging.
     */
    String getChargingAbility();
    void setChargingAbility(String abilityId);

    /**
     * Current "charge ticks" that get incremented while isCharging() == true.
     */
    int getChargeTicks();
    void setCharging(boolean charging);
    void setChargeTicks(int ticks);
    void incrementChargeTicks();

    void execute(Player player, float charge);

    String getID();

    float getCost();

    float getCooldown();

    AbilityType getType();

    enum AbilityType {
        PROJECTILE,
        BUFF,
        DEBUFF,
        UTILITY
        // Add more types as needed
    }

    /**
     * If you want to keep a start time in addition to ticks, you can,
     * but it's optional. Shown here as an example.
     */
    void startCharge(long time);
    long getChargeStart();
    void clearCharge();

    /* =========================
       Logic Helpers
    ========================= */
    default boolean canEquipAbility(String abilityId) {
        return true;
    }

    default boolean isChargeable(String abilityId) {
        return AbilityRegistry.isChargeable(abilityId);
    }
}