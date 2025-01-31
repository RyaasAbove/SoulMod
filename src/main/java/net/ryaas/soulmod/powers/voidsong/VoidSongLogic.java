package net.ryaas.soulmod.powers.voidsong;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.ryaas.soulmod.entities.ModEntities;
import net.ryaas.soulmod.powers.AbilityCapability;

public class VoidSongLogic {

    public static void startCharging(ServerPlayer player) {
        // Mark the player's capability as charging
        player.getCapability(AbilityCapability.PLAYER_ABILITIES_CAPABILITY).ifPresent(cap -> {
            cap.setChargingAbility("voidsong");
            cap.setChargeTicks(0);
            cap.setCharging(true);
        });

        Level level = player.level();
        if (!level.isClientSide) {
            // Create the projectile
            VoidSong vs = new VoidSong(ModEntities.VOIDSONG.get(), level);

            // Set the projectile's owner => ensures ownerUUID is set
            vs.setOwner(player);

            // Mark it as charging => it will run handleCharging() in its tick
            vs.setCharging(true);

            // Optionally, spawn it right at player's eye or slightly in front
            // But the handleCharging() will reposition it each tick anyway.
            Vec3 eyePos = player.getEyePosition();
            vs.setPos(eyePos.x, eyePos.y, eyePos.z);

            // Add to the world
            level.addFreshEntity(vs);
        }

        System.out.println("[VoidSong] Start charging => spawned entity for " + player.getName().getString());
    }

    public static void release(ServerPlayer player) {
        player.getCapability(AbilityCapability.PLAYER_ABILITIES_CAPABILITY).ifPresent(cap -> {
            int finalCharge = cap.getChargeTicks();

            cap.setChargingAbility("");
            cap.setChargeTicks(0);
            cap.setCharging(false);

            // Find the existing VoidSong entity that was charging
            VoidSong vs = findVoidSongByOwner(player);
            if (vs != null) {
                // Stop charging
                vs.setCharging(false);

                // Allow normal physics
                vs.noPhysics = false;
                vs.setNoGravity(false);

                // Store final charge
                vs.setFinalChargeValue(finalCharge);

                // Give some velocity forward
                double velocity = 0.5 + finalCharge * 0.1;
                vs.setDeltaMovement(player.getLookAngle().scale(velocity));
            }

            System.out.println("[VoidSong] Released => final charge=" + finalCharge);
        });
    }

    private static VoidSong findVoidSongByOwner(ServerPlayer player) {
        return player.level().getEntitiesOfClass(VoidSong.class,
                player.getBoundingBox().inflate(32),
                vs -> vs.isCharging() && vs.getOwner() == player
        ).stream().findFirst().orElse(null);
    }
}


