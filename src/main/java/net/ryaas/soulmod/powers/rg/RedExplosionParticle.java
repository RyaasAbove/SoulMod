package net.ryaas.soulmod.powers.rg;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class RedExplosionParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    // Constructor with SpriteSet
    public RedExplosionParticle(ClientLevel level, double x, double y, double z,
                                double vx, double vy, double vz,
                                SpriteSet spriteSet, float width, float height) {
        super(level, x, y, z);
        this.spriteSet = spriteSet;

        // Set particle properties
        this.lifetime = 20;           // Adjust as needed
        this.gravity = 0.0F;         // No gravity
        this.xd = vx;                // Initial velocity
        this.yd = vy;
        this.zd = vz;

        // Set size
        this.setSize(width, height);

        // Pick initial sprite
        this.pickSprite(this.spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0; // Maximum brightness
    }




}