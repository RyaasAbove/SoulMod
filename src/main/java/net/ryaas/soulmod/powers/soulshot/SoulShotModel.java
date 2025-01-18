package net.ryaas.soulmod.powers.soulshot;

import net.minecraft.resources.ResourceLocation;
import net.ryaas.soulmod.SoulMod;
import net.ryaas.soulmod.powers.rg.RedGiant;
import software.bernie.geckolib.model.GeoModel;

public class SoulShotModel extends GeoModel<SoulShot> {

    @Override
    public ResourceLocation getModelResource(SoulShot entity) {
        // The .geo.json file (e.g., assets/soulmod/geo/soulshot.geo.json)
        return new ResourceLocation(SoulMod.MODID, "geo/soulshot.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SoulShot entity) {
        // The texture for the model
        return new ResourceLocation(SoulMod.MODID, "textures/entity/soul_shot_projectile.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SoulShot entity) {
        // If you have an animation file, specify it, otherwise return null
        return new ResourceLocation(SoulMod.MODID, "animations/shot.animation.json");
    }
}