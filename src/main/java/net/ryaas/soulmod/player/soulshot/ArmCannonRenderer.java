package net.ryaas.soulmod.player.soulshot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.ryaas.soulmod.SoulMod;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ArmCannonRenderer extends GeoEntityRenderer<ArmCannon> {

    public ArmCannonRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ArmCannonModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public void render(ArmCannon entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        // entityYaw is typically the entity's Y rotation in degrees,
        // but we handle final transforms in applyRotations(...)
        super.render(entity, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
    }

    /**
     * Called by the parent GeoEntityRenderer code before rendering the model.
     * We'll override the default rotation logic so we can fully control
     * yaw + pitch from the entity's getYRot() / getXRot().
     */
    @Override
    protected void applyRotations(ArmCannon cannonEntity, PoseStack poseStack,
                                  float ageInTicks, float rotationYaw, float partialTicks) {
        // If you skip super, you take full control of the rotation
        // Instead of "super.applyRotations(...)", we do custom transforms.

        // Pull the real angles from the entity:
        float yaw = cannonEntity.getYRot();  // left-right
        float pitch = cannonEntity.getXRot(); // up-down

        // Typically, vanilla living entities do:  poseStack.mulPose(Axis.YP.rotationDegrees(180 - yaw))
        // So if your model is "facing south" by default in your .geo.json,
        // you might do:
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));

        // Then apply pitch.
        // If your model is oriented so +XRot aims downward, you can invert if needed:
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
    }

    @Override
    public ResourceLocation getTextureLocation(ArmCannon entity) {
        // fallback if the model doesn't specify a texture
        return new ResourceLocation(SoulMod.MODID, "textures/entity/armcannon.png");
    }
}