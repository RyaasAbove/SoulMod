package net.ryaas.soulmod.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ryaas.soulmod.powers.AbilityCapability;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientPlayerChargeParticles {
    // Store swirl data by Player's UUID
    private static final Map<UUID, SwirlData> swirlingPlayers = new ConcurrentHashMap<>();

    /**
     * Called when we receive S2CUpdateChargingPacket with charging=true
     */
    public static void startSwirl(Player player, String abilityId) {
        swirlingPlayers.put(player.getUUID(), new SwirlData(abilityId));
        System.out.println("[DEBUG] Started swirl for player " + player.getName().getString());
    }

    /**
     * Called when we receive S2CUpdateChargingPacket with charging=false
     */
    public static void stopSwirl(Player player) {
        swirlingPlayers.remove(player.getUUID());
        System.out.println("[DEBUG] Stopped swirl for player " + player.getName().getString());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            // For each swirling player, increment angle & spawn particles
            for (Map.Entry<UUID, SwirlData> entry : swirlingPlayers.entrySet()) {
                UUID playerUuid = entry.getKey();
                SwirlData data = entry.getValue();

                // Increment angle
                data.angle += 5f; // degrees per tick
                if (data.angle >= 360f) data.angle -= 360f;

                // Find the actual player in the world
                Player p = mc.level.getPlayerByUUID(playerUuid);
                if (p != null) {
                    spawnSwirlAroundPlayer(p, data);
                } else {
                    System.out.println("[DEBUG] Could not find player in swirl map with UUID=" + playerUuid);
                }
            }
        }
    }

    private static void spawnSwirlAroundPlayer(Player p, SwirlData data) {
        // Swirl parameters
        double radius = 1.0;
        int swirlPoints = 8;
        float angleDeg = data.angle;

        for (int i = 0; i < swirlPoints; i++) {
            double offsetAngle = Math.toRadians(angleDeg + (i * (360.0 / swirlPoints)));
            double px = p.getX() + radius * Math.cos(offsetAngle);
            double py = p.getY() + 1.5; // around head
            double pz = p.getZ() + radius * Math.sin(offsetAngle);

            // Slight random offset for natural effect
            px += (p.getRandom().nextDouble() - 0.5) * 0.2;
            py += (p.getRandom().nextDouble() - 0.5) * 0.2;
            pz += (p.getRandom().nextDouble() - 0.5) * 0.2;

            // Spawn Glow particles
            p.level().addParticle(ParticleTypes.GLOW, px, py, pz, 0, 0, 0);
        }
    }

    private static class SwirlData {
        float angle = 0f;
        String abilityId;

        SwirlData(String abilityId) {
            this.abilityId = abilityId;
        }
    }

}