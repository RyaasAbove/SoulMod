package net.ryaas.soulmod.powers;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerAbilitiesProvider implements ICapabilitySerializable<CompoundTag> {
    private final LazyOptional<IPlayerAbilities> optional;

    public PlayerAbilitiesProvider(LazyOptional<IPlayerAbilities> optional) {
        this.optional = optional;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == AbilityCapability.PLAYER_ABILITIES_CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return optional.map(IPlayerAbilities::serializeNBT).orElseGet(CompoundTag::new);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        optional.ifPresent(cap -> cap.deserializeNBT(nbt));
    }
}