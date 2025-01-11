package net.ryaas.soulmod.powers.rg;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RedExplosionRenderer extends GeoEntityRenderer<RedExplosion> {
    public RedExplosionRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RedExplosionModel());
        this.shadowRadius = 0.0F; // No shadow for explosion

    }



}
