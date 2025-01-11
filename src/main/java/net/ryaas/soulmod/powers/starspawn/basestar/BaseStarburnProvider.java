package net.ryaas.soulmod.powers.starspawn.basestar;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class BaseStarburnProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet spriteSet;

    public BaseStarburnProvider(SpriteSet spriteSet) {
        this.spriteSet = spriteSet;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                   double x, double y, double z,
                                   double vx, double vy, double vz) {
        // create and return our custom particle
        return new BaseStarburnParticle(level, x, y, z, vx, vy, vz, this.spriteSet);
    }
}