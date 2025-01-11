package net.ryaas.soulmod.powers.starspawn.basestar;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.ryaas.soulmod.SoulMod;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BaseStarRenderer extends GeoEntityRenderer<BaseStar> {

    public BaseStarRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BaseStarModel());
        this.shadowRadius = 0.25f; // Adjust shadow size if desired
    }

    @Override
    public ResourceLocation getTextureLocation(BaseStar instance) {
        // Replace "soulmod" with your actual mod ID,
        // and "textures/entity/star.png" with the correct path to your texture file
        return new ResourceLocation(SoulMod.MODID, "textures/entity/star_placeholder_e.png");
    }
}