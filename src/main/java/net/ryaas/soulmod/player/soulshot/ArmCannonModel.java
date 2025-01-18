package net.ryaas.soulmod.player.soulshot;

import net.minecraft.resources.ResourceLocation;
import net.ryaas.soulmod.SoulMod;
import net.ryaas.soulmod.powers.soulshot.SoulShot;
import software.bernie.geckolib.model.GeoModel;

public class ArmCannonModel extends GeoModel<ArmCannon> {

    @Override
    public ResourceLocation getModelResource(ArmCannon cannon) {
        return new ResourceLocation("soulmod","geo/armcannon.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ArmCannon cannon) {
        return new ResourceLocation("soulmod","texures/entity/armcannon.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ArmCannon cannon) {
        return new ResourceLocation(SoulMod.MODID, "animations/armcannon.animation.json");
    }

}