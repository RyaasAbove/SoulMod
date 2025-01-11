package net.ryaas.soulmod.powers;


import net.minecraft.nbt.CompoundTag;
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
       Single "Active" Ability
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
       Press-and-Hold Methods
       ========================= */
    /**
     * Called when the key is first pressed for a chargeable ability.
     * Typically stores the start time in ticks or System time.
     */
    void startCharge(long time);

    /**
     * Returns the stored "start time" for the current charge,
     * or 0 if not charging.
     */
    long getChargeStart();

    /**
     * Clears any stored charge state when the key is released.
     */
    void clearCharge();

    /* =========================
       Equip & Charge Helpers
       ========================= */
    /**
     * If you have logic that restricts equipping certain abilities,
     * override this. If not, always true.
     */
    default boolean canEquipAbility(String abilityId) {
        return true;
    }

    /**
     * If you have logic that checks if an ability is "chargeable."
     * For example, "starspawn" or "bowlike" abilities might be chargeable.
     */
    default boolean isChargeable(String abilityId) {
        return AbilityRegistry.isChargeable(abilityId);
    }
}