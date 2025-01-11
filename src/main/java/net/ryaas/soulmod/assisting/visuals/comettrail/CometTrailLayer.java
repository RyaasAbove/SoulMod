package net.ryaas.soulmod.assisting.visuals.comettrail;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;


public class CometTrailLayer<T extends Entity & GeoAnimatable> extends GeoRenderLayer<T> {

    public CometTrailLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            T animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        // 1) Invoke default layer rendering (if any)
        super.render(poseStack, animatable, bakedModel, renderType,
                bufferSource, buffer, partialTick, packedLight, packedOverlay);

        // 2) Use CometTrailManager to draw a continuous "ribbon" behind this entity
        //    as long as it has a position history stored.

    }
}