package net.ryaas.soulmod.powers.rg;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RedExplosionModel extends GeoModel<RedExplosion> {
    @Override
    public ResourceLocation getModelResource(RedExplosion redExplosion) {
        return new ResourceLocation("soulmod", "geo/redexplosion.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RedExplosion redExplosion) {
        return new ResourceLocation("soulmod", "textures/entity/redexplosion.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RedExplosion redExplosion) {
        return new ResourceLocation("soulmod", "animations/redexplosion.animation.json");
    }
}
