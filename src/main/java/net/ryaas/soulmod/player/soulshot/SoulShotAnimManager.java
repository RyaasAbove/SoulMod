package net.ryaas.soulmod.player.soulshot;


import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ryaas.soulmod.powers.AbilityCapability;

import static net.ryaas.soulmod.SoulMod.MODID;
import static net.ryaas.soulmod.player.PlayerAnimator.SOULSHOT_LAYER;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SoulShotAnimManager {

    private static final ResourceLocation CHARGING_SOULSHOT = new ResourceLocation(MODID, "charging_soulshot");
    private static final ResourceLocation FIRING_SOULSHOT = new ResourceLocation(MODID, "firing_soulshot");

    private static boolean lastFrameCharging = false; // track old state

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().level == null) return;
        var player = Minecraft.getInstance().player;
        AbstractClientPlayer cPlayer = (AbstractClientPlayer) player;

        player.getCapability(AbilityCapability.PLAYER_ABILITIES_CAPABILITY).ifPresent(cap -> {
            boolean isCharging = cap.isSoulShotCharging();

            // 1) Get the layer we registered
            ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData(cPlayer)
                    .get(SOULSHOT_LAYER);

            if (layer == null) return;

            // If we just started charging this frame
            if (isCharging && !lastFrameCharging) {
                // Set the "charging_soulshot" loop animation
                var animData = PlayerAnimationRegistry.getAnimation(CHARGING_SOULSHOT);
                if (animData != null) {
                    layer.setAnimation(new KeyframeAnimationPlayer(animData));
                }
            }

            // If we just ended charging
            if (!isCharging && lastFrameCharging) {
                // Remove the charging anim
                layer.setAnimation(null);

                // Immediately play the one-shot "firing_soulshot" animation
                var animData = PlayerAnimationRegistry.getAnimation(FIRING_SOULSHOT);
                if (animData != null) {
                    layer.setAnimation(new KeyframeAnimationPlayer(animData));
                }
            }

            lastFrameCharging = isCharging;
        });
    }
}