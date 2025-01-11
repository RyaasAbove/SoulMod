package net.ryaas.soulmod.powers.rg;

import net.minecraft.resources.ResourceLocation;
import net.ryaas.soulmod.SoulMod;
import software.bernie.geckolib.model.GeoModel;

public class RGModel extends GeoModel<RedGiant> {
    @Override
    public ResourceLocation getModelResource(RedGiant redGiant) {
        return new ResourceLocation(SoulMod.MODID, "geo/redgiant.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RedGiant redGiant) {
        return new ResourceLocation(SoulMod.MODID, "textures/entity/redstar.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RedGiant redGiant) {
        return new ResourceLocation(SoulMod.MODID, "animations/rg.animation.json");
    }
}
