package net.ryaas.soulmod.player;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


import static net.ryaas.soulmod.SoulMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PlayerAnimator {

    // A unique ResourceLocation to identify the layer
    public static final ResourceLocation SOULSHOT_LAYER = new ResourceLocation(MODID, "soulshot_layer");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Register a new animation layer for each player
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                SOULSHOT_LAYER,
                42, // priority
                PlayerAnimator::createSoulShotLayer
        );
    }

    private static IAnimation createSoulShotLayer(AbstractClientPlayer player) {
        // Return a brand-new ModifierLayer
        return new ModifierLayer<>();
    }
}