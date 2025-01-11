package net.ryaas.soulmod.powers.starspawn.basestar;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BaseStarburnParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    // totalFrames = 6, so we loop 0..5
    private static final int TOTAL_FRAMES = 6;

    public BaseStarburnParticle(ClientLevel level, double x, double y, double z,
                                double vx, double vy, double vz,
                                SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);
        this.spriteSet = spriteSet;

        // Example settings:
        this.lifetime = 40;           // 2 seconds at 20 ticks/sec
        this.gravity = 0.0F;         // no gravity
        this.xd = vx;                // initial velocity
        this.yd = vy;
        this.zd = vz;

        // if you want bigger or smaller rendering:
        this.setSize(0.5F, 0.5F);

        // pick initial frame
        setSprite(this.spriteSet.get(0, TOTAL_FRAMES));
    }

    @Override
    public void tick() {
        super.tick();

        // If the particle exceeded its lifetime, remove it
        if (this.age >= this.lifetime) {
            this.remove();
            return;
        }

        // Loop frames: age % TOTAL_FRAMES => 0..5
        int frameIndex = (this.age % TOTAL_FRAMES);
        setSprite(this.spriteSet.get(frameIndex, TOTAL_FRAMES));
    }

    @Override
    public ParticleRenderType getRenderType() {
        // Typically "PARTICLE_SHEET_TRANSLUCENT" for an additive or alpha-blended effect
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
        // By default, TextureSheetParticle's super.render(...) draws the sprite with correct UVs
        super.render(vertexConsumer, camera, partialTicks);
    }
}
