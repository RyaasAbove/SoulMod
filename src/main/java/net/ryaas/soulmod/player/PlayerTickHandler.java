package net.ryaas.soulmod.player;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ryaas.soulmod.powers.AbilityCapability;
import net.ryaas.soulmod.powers.PlayerAbilities;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerTickHandler {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Only run logic on the server side & the END phase
        if (event.side.isServer() && event.phase == TickEvent.Phase.END) {
            Player player = event.player;

            // Grab the abilities capability
            player.getCapability(AbilityCapability.PLAYER_ABILITIES_CAPABILITY).ifPresent(cap -> {
                // If charging, increment
                if (cap.isCharging() && cap.getChargeTicks() < 100) {
                    cap.incrementChargeTicks();
                }
            });
        }
    }
}