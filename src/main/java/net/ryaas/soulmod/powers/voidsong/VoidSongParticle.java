package net.ryaas.soulmod.powers.voidsong;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class VoidSongParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private final int totalFrames; // e.g. 12
    private int frameIndex;

    public VoidSongParticle(ClientLevel level, double x, double y, double z,
                           double vx, double vy, double vz,
                           SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);
        this.spriteSet = spriteSet;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        // If you know you have 12 frames in the spriteSet:
     // this.totalFrames = spriteSet.sprites.size(); // hopefully 12
        //CHANGE THIS IF YOU ADD MORE FRAMES
        this.totalFrames = 12;
        this.frameIndex = 0;

        // If you want it to last 'forever' or for a certain time
        this.lifetime = 60; // e.g. 3 seconds (20 ticks/sec) - or set longer if needed

        // Set initial sprite
        setSpriteFromIndex(0);
    }

    @Override
    public void tick() {
        super.tick();
        // each tick => increment frame
        frameIndex++;
        if (frameIndex >= totalFrames) {
            frameIndex = 0; // loop back
        }
        setSpriteFromIndex(frameIndex);
    }

    private void setSpriteFromIndex(int index) {
        setSprite(this.spriteSet.get(index, totalFrames));
    }

    @Override
    public ParticleRenderType getRenderType() {
        // Usually PARTICLE_SHEET_TRANSLUCENT if it's a translucent effect
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}

