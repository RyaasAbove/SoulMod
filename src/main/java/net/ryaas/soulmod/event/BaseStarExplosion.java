package net.ryaas.soulmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.ryaas.soulmod.assisting.visuals.ModParticleTypes;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BaseStarExplosion extends Explosion {
    private final Level myLevel;

    public BaseStarExplosion(Level pLevel, @Nullable Entity pSource, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, List<BlockPos> pPositions, Level myLevel) {
        super(pLevel, pSource, pToBlowX, pToBlowY, pToBlowZ, pRadius, pPositions);
        this.myLevel = myLevel;
    }
    @Override
    public void finalizeExplosion(boolean spawnParticles) {
        // 1) Let Explosion handle damage to blocks/entities
        //    This breaks blocks and does entity knockback/damage
        super.finalizeExplosion(false);
        // pass false so we definitely skip default explosion smoke/sound visuals

        // 2) Now spawn your own custom particles
        spawnBaseStarExplosionParticles();
    }

    private void spawnBaseStarExplosionParticles() {
        double cx = this.getPosition().x;
        double cy = this.getPosition().y;
        double cz = this.getPosition().z;

        // The server instructs clients to spawn 20 of your custom particle
        if (myLevel instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ModParticleTypes.BASE_STAR_SMOKE.get(),
                    cx, cy, cz,
                    20,   // count
                    0.2, 0.2, 0.2,  // xOffset, yOffset, zOffset
                    0.01  // speed
            );
        }
    }
}

