package net.ryaas.soulmod.assisting.visuals;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ryaas.soulmod.SoulMod;

public class ModParticleTypes {
    // 1) Create a DeferredRegister for ParticleTypes, using your mod ID
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SoulMod.MODID);


    public static final RegistryObject<SimpleParticleType> BASE_STAR_SMOKE =
            PARTICLE_TYPES.register("base_star_smoke",
                    () -> new SimpleParticleType(true)
            );

    public static final RegistryObject<SimpleParticleType> RED_EXPLOSION =
            PARTICLE_TYPES.register(
                    "red_explosion",
                    // "alwaysShow" = true means it’s rendered from far away
                    () -> new SimpleParticleType(true)
            );
    public static final RegistryObject<SimpleParticleType> RED_ORB =
            PARTICLE_TYPES.register("red_orb",
                    () -> new SimpleParticleType(true) // "alwaysShow" = true for ignoring distance
            );


}

