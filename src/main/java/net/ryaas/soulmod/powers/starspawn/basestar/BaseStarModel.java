package net.ryaas.soulmod.powers.starspawn.basestar;

import net.minecraft.resources.ResourceLocation;
import net.ryaas.soulmod.SoulMod;
import software.bernie.geckolib.model.GeoModel;

public class BaseStarModel extends GeoModel<BaseStar> {

    @Override
    public ResourceLocation getModelResource(BaseStar object) {
        // Example: "assets/soulmod/geo/base_star.geo.json"
        return new ResourceLocation(SoulMod.MODID, "geo/base_star.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BaseStar object) {
        // This can match getTextureLocation in your renderer
        return new ResourceLocation(SoulMod.MODID, "textures/entity/star.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BaseStar animatable) {
        // Example: "assets/soulmod/animations/base_star.animation.json"
        return new ResourceLocation(SoulMod.MODID, "animations/base_star.animation.json");
    }
}