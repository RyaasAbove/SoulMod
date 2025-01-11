package net.ryaas.soulmod.powers.rg;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.IForgeVertexConsumer;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.ryaas.soulmod.SoulMod;
import net.ryaas.soulmod.assisting.visuals.comettrail.CometTrailLayer;

import net.ryaas.soulmod.powers.starspawn.basestar.BaseStar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import java.util.List;

public class RenderRG extends GeoEntityRenderer<RedGiant> {
    @Override
    protected void applyRotations(
            RedGiant animatable,
            PoseStack poseStack,
            float ageInTicks,
            float rotationYaw,
            float partialTick
    ) {
        // Option A: Call super first, so you retain any vanilla-based rotations
        // such as facing 180 - rotationYaw, shaking when damaged, etc.
        // super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick);

        // Option B: Skip calling super if you want to override all rotation logic
        // (including the usual 180° flip).

        // Now apply your custom rotation logic:
        float yaw   = animatable.getYRot();  // server-side yaw
        float pitch = animatable.getXRot();  // server-side pitch

        // If your model's "front" faces -Z, you might do negative yaw
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        // For pitch, you might do positive or negative depending on orientation
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
    }

    public RenderRG(EntityRendererProvider.Context context) {
        super(context, new RGModel());

        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));



    }

    @Override
    public void render(RedGiant entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);


    }

    @Override
    public ResourceLocation getTextureLocation(RedGiant instance) {
        // Replace "soulmod" with your actual mod ID,
        // and "textures/entity/star.png" with the correct path to your texture file
        return new ResourceLocation(SoulMod.MODID, "textures/entity/redstar.png");
    }


}
