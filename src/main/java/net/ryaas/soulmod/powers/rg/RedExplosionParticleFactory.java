package net.ryaas.soulmod.powers.rg;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.ryaas.soulmod.powers.rg.RedExplosionParticle;

public class RedExplosionParticleFactory implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet spriteSet;

    public RedExplosionParticleFactory(SpriteSet spriteSet) {
        this.spriteSet = spriteSet;
    }

    @Override
    public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        return new RedExplosionParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet, 1.0F, 1.0F);
    }
}