package net.ryaas.soulmod.assisting;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class MyPlayerAnimationLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public MyPlayerAnimationLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        // We don't actually render anything here, just ensure the layer is created
        if (!AnimatorMaps.animationData.containsKey(player)) {
            // Create a new layer for this player
            ModifierLayer<IAnimation> layer = new ModifierLayer<>();
            AnimatorMaps.animationData.put(player, layer);
            System.out.println("[DEBUG] Created ModifierLayer for player " + player.getName().getString());
        }

        // no actual rendering done by this layer
    }
}