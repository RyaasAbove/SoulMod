package net.ryaas.soulmod.assisting;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ryaas.soulmod.network.NetworkHandler;
import net.ryaas.soulmod.network.c2spackets.C2SAbilityKeyStatePacket;
import net.ryaas.soulmod.screen.radialmenu.AbilityRadialScreen;

import static net.ryaas.soulmod.assisting.KeyBinding.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientKeyHandler {
    // Let's track press/release for the ability key (R)
    private static boolean wasPressed = false;
    private static int chargeTicks = 0;
    private static final int MAX_CHARGE_TICKS = 200;

    // We'll register these in a separate "ModKeybinds" class or inline

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // Check if the G key is pressed down
        if (OPEN_RAD.consumeClick()) {
            // Open radial screen
            Minecraft.getInstance().setScreen(new AbilityRadialScreen());
        }
    }

    @SubscribeEvent
    public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            boolean isPressedNow = USE_ABILITY.isDown();
            if (!wasPressed && isPressedNow) {
                // Just pressed
                NetworkHandler.INSTANCE.sendToServer(new C2SAbilityKeyStatePacket(true));
            } else if (wasPressed && !isPressedNow) {
                // Just released
                NetworkHandler.INSTANCE.sendToServer(new C2SAbilityKeyStatePacket(false));
            }

            wasPressed = isPressedNow;

            if (isPressedNow) {
                // Increment charge ticks while key is held, up to the maximum
                if (chargeTicks < MAX_CHARGE_TICKS) {
                    chargeTicks++;
                }
            }
        }
    }
}