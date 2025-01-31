package net.ryaas.soulmod.powers.voidsong;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class VoidSongProjectileRenderer extends EntityRenderer<VoidSongProjectile> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("soulmod", "textures/particle/voidsong/voidsong_projectile.png");

    public VoidSongProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        // If you want your entity to show up with a specific shadow or no shadow:
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(VoidSongProjectile entity) {
        return TEXTURE;
    }

    @Override
    public void render(VoidSongProjectile entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        poseStack.pushPose();

        // 1) Translate from world coords to camera-relative coords
        var camera = this.entityRenderDispatcher.camera;
        double dx = Mth.lerp(partialTicks, entity.xOld, entity.getX()) - camera.getPosition().x;
        double dy = Mth.lerp(partialTicks, entity.yOld, entity.getY()) - camera.getPosition().y;
        double dz = Mth.lerp(partialTicks, entity.zOld, entity.getZ()) - camera.getPosition().z;
        poseStack.translate(dx, dy + 1.0F, dz);

        // 2) Billboard the sprite toward the camera
        float camYaw = camera.getYRot();
        float camPitch = camera.getXRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-camYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(camPitch));

        // -- (Remove multi-frame logic here) --
        // Instead, just pick ONE texture for now:
        ResourceLocation currentFrame = new ResourceLocation(
                "soulmod", "textures/particle/voidsong/voidsong_projectile.png"
        );

        // 3) Use entityTranslucent (or entityCutout) for the quad
        var renderType = RenderType.entityTranslucent(currentFrame);
        var vertexConsumer = buffer.getBuffer(renderType);

        // 4) Draw the quad
        // We'll make it 4×4 in size by default (halfSize=2 => -2..2)
        float halfSize = 2.0F;

        poseStack.pushPose(); // push if you want to apply additional scale
        // For example: poseStack.scale(2f, 2f, 2f);

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

        poseStack.popPose(); // pop the scale pushPose
        poseStack.popPose(); // final pop

        // The default "super.render(...)" is typically empty, but you can call it if you want bounding box debug, etc.
        // super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight
    }
}
