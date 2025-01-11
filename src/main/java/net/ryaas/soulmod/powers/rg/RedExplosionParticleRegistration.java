package net.ryaas.soulmod.powers.rg;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public class RedExplosionParticleRegistration<T extends ParticleOptions> implements ParticleEngine.SpriteParticleRegistration<T> {
    @Override
    public ParticleProvider<T> create(SpriteSet spriteSet) {
        // Casting is safe because we know we're dealing with SimpleParticleType
        @SuppressWarnings("unchecked")
        ParticleProvider<T> provider = (ParticleProvider<T>) new RedExplosionParticleFactory(spriteSet);
        return provider;
    }


}