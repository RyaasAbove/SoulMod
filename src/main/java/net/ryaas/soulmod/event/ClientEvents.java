package net.ryaas.soulmod.event;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.ryaas.soulmod.SoulMod;
import net.ryaas.soulmod.assisting.KeyBinding;
import net.ryaas.soulmod.assisting.MyPlayerAnimationLayer;
import net.ryaas.soulmod.client.ClientHooks;
import net.ryaas.soulmod.entities.ModEntities;
import net.ryaas.soulmod.network.NetworkHandler;
//import net.ryaas.soulmod.network.c2spackets.C2SActivateSelectedAbilityPacket;


import net.ryaas.soulmod.powers.AbilityCapability;
import net.ryaas.soulmod.powers.AbilityRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "soulmod", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiOverlayEvent.Pre event) {
        // Call the HUD rendering method
        renderEquippedAbilitiesHUD(event.getGuiGraphics());
    }

    private static final ResourceLocation HUD_BACKGROUND =
            new ResourceLocation("soulmod", "textures/gui/screens/abilityhud.png");

    private static void renderEquippedAbilitiesHUD(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Get equipped abilities
        List<String> equippedAbilities = new ArrayList<>();
        mc.player.getCapability(AbilityCapability.PLAYER_ABILITIES_CAPABILITY).ifPresent(cap -> {
            for (int i = 0; i < cap.getSlotCount(); i++) {
                String abilityId = cap.getAbilityInSlot(i);
                if (abilityId != null && !abilityId.isEmpty()) {
                    equippedAbilities.add(abilityId);
                }
            }
        });

        // Fetch the current window dimensions
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Original dimensions of the image
        int originalWidth = 26;
        int originalHeight = 132;

        // Scale factor (75%)
        float scaleFactor = 0.75f;
        int scaledWidth = (int) (originalWidth * scaleFactor);
        int scaledHeight = (int) (originalHeight * scaleFactor);

        // Position in bottom-right corner
        int hudX = screenWidth - scaledWidth;
        int hudY = screenHeight - scaledHeight - 10;

        // Render the custom background with scaling
        graphics.pose().pushPose();
        graphics.pose().translate(hudX, hudY, 0);
        graphics.pose().scale(scaleFactor, scaleFactor, 1.0f);

// Draw background at local (0,0) in this scaled coordinate space
        RenderSystem.setShaderTexture(0, HUD_BACKGROUND);
        graphics.blit(HUD_BACKGROUND, 0, 0, 0, 0,
                originalWidth, originalHeight,
                originalWidth, originalHeight);

// Now, draw icons in local coordinates as well
        int iconSize = 16;
        int[] slotOffsets = {3, 24, 46, 68, 90, 100};

        for (int i = 0; i < 6; i++) {
            String abilityId = (i < equippedAbilities.size()) ? equippedAbilities.get(i) : null;

            // For each slot, position icons at local coords:
            //  - Move 1 pixel to the RIGHT => +1
            //  - If it's the top slot (i == 0), move 1 pixel UP => iconY -= 1
            int iconX = (16 - iconSize) / 2 + 2;
            int iconY = slotOffsets[i] + (16 - iconSize) / 2;

            if (i == 0) {
                iconY -= 1;  // top icon goes 1px higher
            }

            if (abilityId != null) {
                var ability = AbilityRegistry.getAbility(abilityId);
                if (ability != null) {
                    ResourceLocation iconTexture = new ResourceLocation("soulmod", ability.getIconPath());
                    RenderSystem.setShaderTexture(0, iconTexture);
                    graphics.blit(iconTexture, iconX, iconY, 0, 0,
                            iconSize, iconSize,
                            iconSize, iconSize);
                }
            }
        }

// Finally pop the pose
        graphics.pose().popPose();
    }

    @Mod.EventBusSubscriber(modid = SoulMod.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (KeyBinding.MENU.consumeClick()) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openCharMenu());
            }

            if (KeyBinding.USE_ABILITY.consumeClick()) {
//               NetworkHandler.INSTANCE.sendToServer(new C2SActivateSelectedAbilityPacket());
            }
        }
    }


    @Mod.EventBusSubscriber(modid = SoulMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {

            event.register(KeyBinding.MENU);
            event.register(KeyBinding.USE_ABILITY);
            event.register(KeyBinding.OPEN_RAD);
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {

        }

        @SubscribeEvent
        public static void onRegisterLayers(EntityRenderersEvent.AddLayers event) {
            // For "default" player skin
            PlayerRenderer defaultRenderer = event.getSkin("default");
            if (defaultRenderer != null) {
                defaultRenderer.addLayer(new MyPlayerAnimationLayer(defaultRenderer));
            }

            // For "slim" (Alex) skin
            PlayerRenderer slimRenderer = event.getSkin("slim");
            if (slimRenderer != null) {
                slimRenderer.addLayer(new MyPlayerAnimationLayer(slimRenderer));
            }
        }

    }
}
