package net.ryaas.soulmod.powers.starspawn.basestar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BaseStarburnRenderer extends EntityRenderer<BaseStarburn> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("soulmod", "textures/particle/base_star_explosion.png");

    private static final int TOTAL_FRAMES = 6; // 6 frames horizontally

    public BaseStarburnRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0F; // no shadow
    }

    @Override
    public ResourceLocation getTextureLocation(BaseStarburn entity) {
        return TEXTURE;
    }

    @Override
    public void render(BaseStarburn entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 1) Interpolate position
        double x = Mth.lerp(partialTicks, entity.xOld, entity.getX());
        double y = Mth.lerp(partialTicks, entity.yOld, entity.getY());
        double z = Mth.lerp(partialTicks, entity.zOld, entity.getZ());

        // Translate to that position relative to the camera
        var cam = this.entityRenderDispatcher.camera;
        var camPos = cam.getPosition();
        poseStack.translate(x - camPos.x, y - camPos.y, z - camPos.z);

        // 2) Billboard logic
        float camYaw = cam.getYRot();
        float camPitch = cam.getXRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-camYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(camPitch));

        // 3) Scale
        float scale = 2.0F;
        poseStack.scale(scale, scale, scale);

        // 4) Determine which frame to render
        // For example, cycle every 5 ticks
        int frameIndex = (entity.tickCount / 5) % TOTAL_FRAMES;

        // 5) Compute UV for a horizontal sprite sheet
        float[] uv = getFrameUVs(frameIndex);
        float u1 = uv[0], v1 = uv[1];
        float u2 = uv[2], v2 = uv[3];

        // Build the quad
        var renderType = RenderType.entityCutout(getTextureLocation(entity));
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        var pose = poseStack.last().pose();

        float r = 1f, g = 1f, b = 1f, a = 1f;

        // Tri #1
        vertexConsumer.vertex(pose, -0.5f, -0.5f, 0)
                .color(r, g, b, a)
                .uv(u1, v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        vertexConsumer.vertex(pose, 0.5f, -0.5f, 0)
                .color(r, g, b, a)
                .uv(u2, v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        vertexConsumer.vertex(pose, 0.5f, 0.5f, 0)
                .color(r, g, b, a)
                .uv(u2, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        // Tri #2
        vertexConsumer.vertex(pose, -0.5f, -0.5f, 0)
                .color(r, g, b, a)
                .uv(u1, v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        vertexConsumer.vertex(pose, 0.5f, 0.5f, 0)
                .color(r, g, b, a)
                .uv(u2, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        vertexConsumer.vertex(pose, -0.5f, 0.5f, 0)
                .color(r, g, b, a)
                .uv(u1, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * We have 6 frames horizontally.
     * Each frame is 64 wide in a total 384 width => 64/384 = 1/6 in U.
     * Height is 64 => 64/64 = 1 => full V range.
     *
     * frameIndex 0 => U range [0..1/6]
     * frameIndex 1 => [1/6..2/6], etc.
     */
    private float[] getFrameUVs(int frameIndex) {
        float frameWidth = 1f / 6f;  // each frame is 64 px out of 384 px => 1/6
        float leftU = frameIndex * frameWidth;
        float rightU = leftU + frameWidth;

        // entire height is 64 => 64/64 = 1 => V from 0..1
        float topV = 0f;    // 0
        float botV = 1f;    // 1

        // we return [u1, v1, u2, v2],
        // which we'll interpret as top-left = (u1, v1), bottom-right = (u2, v2)
        return new float[]{ leftU, topV, rightU, botV };
    }
}