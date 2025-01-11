package net.ryaas.soulmod.assisting.visuals.comettrail;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;

public class RedCometTrailParticle extends TextureSheetParticle {

    protected RedCometTrailParticle(ClientLevel world,
                                    double x, double y, double z,
                                    double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);

        // Movement
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        // How many ticks this particle will live
        this.lifetime = 20; // 1 second at 20 TPS (adjust as needed)

        // Initial size of the particle
        this.quadSize = 0.5F;

        // If you want it partially translucent
        this.alpha = 0.9F;
    }

    @Override
    public void tick() {
        super.tick();
        // Example fade-out effect: alpha decreases over lifetime
        this.alpha = 0.9F * ((float)this.lifetime - (float)this.age) / (float)this.lifetime;
    }

    @Override
    public ParticleRenderType getRenderType() {
        // Tells MC to render using a standard "texture sheet" approach
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}