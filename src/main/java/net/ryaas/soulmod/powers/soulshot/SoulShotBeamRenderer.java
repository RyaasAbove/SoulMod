package net.ryaas.soulmod.powers.soulshot;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SoulShotBeamRenderer extends GeoEntityRenderer<SoulShot> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("soulmod", "textures/entity/soul_shot_projectile.png");

    public SoulShotBeamRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
        super(context, new SoulShotModel());
    }

    @Override
    public ResourceLocation getTextureLocation(SoulShot entity) {
        // if you want to override the texture from the model
        return TEXTURE;
    }

    @Override
    public void render(SoulShot entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Renders the GeoModel
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}