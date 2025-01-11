package net.ryaas.soulmod.assisting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "soulmod");

    // Register your custom explosion sound
    // Register your custom explosion sound
    public static final RegistryObject<SoundEvent> RED_GIANT_EXPLOSION = SOUND_EVENTS.register("red_giant_explosion",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("soulmod", "red_giant_explosion")));
}