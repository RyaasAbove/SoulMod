package net.ryaas.soulmod.powers.voidsong;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.ryaas.soulmod.SoulMod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VoidSongRenderer extends EntityRenderer<VoidSong> {


    public VoidSongRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0F; // no shadow
    }

    @Override
    public void render(VoidSong entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        // We do not call super.render(...) because we handle all transforms manually.

        poseStack.pushPose();

        // 1) Translate from world coords to camera-relative coords
        var camera = this.entityRenderDispatcher.camera;
        double dx = Mth.lerp(partialTicks, entity.xOld, entity.getX()) - camera.getPosition().x;
        double dy = Mth.lerp(partialTicks, entity.yOld, entity.getY()) - camera.getPosition().y;
        double dz = Mth.lerp(partialTicks, entity.zOld, entity.getZ()) - camera.getPosition().z;
        poseStack.translate(dx, dy + 1.0F, dz); // +1.0F => offset upward if desired

        // 2) Billboard the sprite toward the camera
        float camYaw = camera.getYRot();
        float camPitch = camera.getXRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-camYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(camPitch));


        // 3) Determine which prefix to use based on charge
        int charge = entity.getFinalChargeValue();
        String prefix;
        if (charge < 30) {
            prefix = "voidsong-0";      // e.g. "voidsong-01.png" .. "voidsong-012.png"
        } else if (charge < 60) {
            prefix = "voidsong1ball-"; // e.g. "voidsong1ball-1.png" .. "voidsong1ball-12.png"
        } else {
            prefix = "voidsong-";      // e.g. "voidsong-1.png" .. "voidsong-12.png"
        }

        // 4) Pick which sub-frame (1..12) to display for animation
        //    Every 2 ticks => next frame. Adjust as you like.
        int subFrame = (entity.tickCount / 2) % 12 + 1;

        // 5) Build the texture path
        //    Example: "soulmod:textures/particle/voidsong/voidsong-01.png"
        //             "soulmod:textures/particle/voidsong/voidsong1ball-8.png" etc.
        String texturePath = String.format(
                "soulmod:textures/particle/voidsong/%s%d.png",
                prefix, subFrame
        );
        ResourceLocation currentFrame = new ResourceLocation(texturePath);

        // 6) Use entityTranslucent so alpha is respected
        var renderType = RenderType.entityTranslucent(currentFrame);
        var vertexConsumer = buffer.getBuffer(renderType);

        // 7) Draw a simple quad
        float halfSize = 2.0F; // total sprite = 4 x 4

        poseStack.pushPose(); // extra push if you want scaling
        // poseStack.scale(2f, 2f, 2f); // for an example of bigger scaling

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        // bottom-left
        vertexConsumer.vertex(matrix, -halfSize, -halfSize, 0.0F)
                .color(1f, 1f, 1f, 1f)
                .uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normalMatrix, 0, 1, 0)
                .endVertex();

        // bottom-right
        vertexConsumer.vertex(matrix, halfSize, -halfSize, 0.0F)
                .color(1f, 1f, 1f, 1f)
                .uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normalMatrix, 0, 1, 0)
                .endVertex();

        // top-right
        vertexConsumer.vertex(matrix, halfSize, halfSize, 0.0F)
                .color(1f, 1f, 1f, 1f)
                .uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normalMatrix, 0, 1, 0)
                .endVertex();

        // top-left
        vertexConsumer.vertex(matrix, -halfSize, halfSize, 0.0F)
                .color(1f, 1f, 1f, 1f)
                .uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normalMatrix, 0, 1, 0)
                .endVertex();

        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(VoidSong entity) {
        // Not used, because we specify the texture in render() via RenderType
        // But must return something non-null.
        return new ResourceLocation("soulmod", "textures/particle/voidsong/voidsong-1.png");
    }
}
