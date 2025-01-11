package net.ryaas.soulmod.powers;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ryaas.soulmod.SoulMod;
@Mod.EventBusSubscriber(modid = SoulMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AbilityCapability {
    public static final Capability<IPlayerAbilities> PLAYER_ABILITIES_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            // Create the backing implementation
            IPlayerAbilities abilities = new PlayerAbilities();
            // Wrap it in a LazyOptional
            LazyOptional<IPlayerAbilities> lazy = LazyOptional.of(() -> abilities);

            event.addCapability(
                    new ResourceLocation(SoulMod.MODID, "player_abilities"),
                    new PlayerAbilitiesProvider(lazy)
            );
        }
    }

    public static class PlayerAbilitiesProvider implements ICapabilitySerializable<CompoundTag> {
        private final LazyOptional<IPlayerAbilities> instance;

        public PlayerAbilitiesProvider(LazyOptional<IPlayerAbilities> instance) {
            this.instance = instance;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            if (cap == PLAYER_ABILITIES_CAPABILITY) {
                return instance.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return instance.orElseThrow(
                    () -> new IllegalStateException("No abilities instance!")
            ).serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.orElseThrow(
                    () -> new IllegalStateException("No abilities instance!")
            ).deserializeNBT(nbt);
        }
    }
}