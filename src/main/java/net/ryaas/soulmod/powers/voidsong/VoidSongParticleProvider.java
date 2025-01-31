package net.ryaas.soulmod.powers.voidsong;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class VoidSongParticleProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet spriteSet;


    public VoidSongParticleProvider(SpriteSet spriteSet){
        this.spriteSet = spriteSet;
    }

    @Override
    public Particle createParticle(SimpleParticleType typeIn, ClientLevel level,
                                   double x, double y, double z,
                                   double xSpeed, double ySpeed, double zSpeed) {

        VoidSongParticle particle = new VoidSongParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
        return particle;
    }
}
