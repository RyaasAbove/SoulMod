package net.ryaas.soulmod.assisting.visuals.comettrail;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class RedCometTrailProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet spriteSet;

    public RedCometTrailProvider(SpriteSet spriteSet) {
        this.spriteSet = spriteSet;
    }

    @Override
    public RedCometTrailParticle createParticle(SimpleParticleType typeIn,
                                                ClientLevel worldIn,
                                                double x, double y, double z,
                                                double vx, double vy, double vz) {
        // Create the particle
        RedCometTrailParticle particle = new RedCometTrailParticle(worldIn, x, y, z, vx, vy, vz);

        // Assign a random sprite (or just one) from the spriteSet
        particle.pickSprite(this.spriteSet);

        return particle;
    }
}