package net.ryaas.soulmod.powers;

import net.minecraft.server.level.ServerPlayer;
import net.ryaas.soulmod.entities.ModEntities;
import net.ryaas.soulmod.powers.rg.RedGiant;
import net.ryaas.soulmod.powers.starspawn.basestar.BaseStar;

import java.util.UUID;

public class AbilityLogic {

    /**
     * Called when the key is pressed for a chargeable ability.
     * Example: starspawn -> find or create a star entity that floats near player's hand.
     */
    public static void onPressChargeable(ServerPlayer player, String abilityId) {
        // If you have other chargeable abilities, you can do more checks here
        if ("starspawn".equals(abilityId)) {
            // find or spawn a BaseStar
            BaseStar existingStar = findStarByOwner(player);
            if (existingStar == null) {
                spawnChargingStar(player);
            } else {
                existingStar.setCharging(true);
            }
        }
        else if ("rg".equals(abilityId)) {
            // find or spawn a RedGiant
            RedGiant existingStar = findRedStarByOwner(player);
            if (existingStar == null) {
                spawnRedChargingStar(player);
            } else {
                existingStar.setCharging(true);
            }
        }
        // else if ("someOtherChargeable".equals(abilityId)) { ... }
    }

    /**
     * Called when the key is released for a chargeable ability.
     * Example: starspawn -> stop charging, "throw" the star.
     */
    public static void onReleaseChargeable(ServerPlayer player, String abilityId) {
        if ("starspawn".equals(abilityId)) {
            BaseStar star = findStarByOwner(player);
            if (star != null) {
                shootStar(player, star);
            }
        }
        else if("rg".equals(abilityId)){
            RedGiant redstar = findRedStarByOwner(player);
            if(redstar != null){
                redstar.releaseCharge();
                shootRedStar(player, redstar);
            }
        }
        // else if ("someOtherChargeable".equals(abilityId)) { ... }
    }

    /**
     * Called when we press the key for a non-chargeable (instant) ability.
     */
    public static void onInstantAbility(ServerPlayer player, String abilityId) {
        // e.g. if ("fireball".equals(abilityId)) { spawnFireball(...); }
        System.out.println("[Server] onInstantAbility -> " + abilityId);
    }

    /* =========================================================
       Star-specific logic for starspawn
     ========================================================= */

    private static void spawnChargingStar(ServerPlayer player) {
        if (player.level().isClientSide()) return;

        BaseStar star = ModEntities.BASE_STAR.get().create(player.level());
        if (star == null) {
            System.out.println("[DEBUG] Failed to create BaseStar entity!");
            return;
        }

        star.setOwnerUUID(player.getUUID());
        star.setCharging(true); // ensures float logic in tick()

        // Position it above player's head with increased offset
        star.setPos(player.getX(), player.getEyeY() + 3.0, player.getZ());

        player.level().addFreshEntity(star);
        System.out.println("[DEBUG] Spawned a new charging BaseStar for " + player.getName().getString());
    }

    private static void spawnRedChargingStar(ServerPlayer player) {
        if (player.level().isClientSide()) return;

        RedGiant redstar = ModEntities.RED_GIANT.get().create(player.level());
        if (redstar == null) {
            System.out.println("[DEBUG] Failed to create RedGiant entity!");
            return;
        }

        redstar.setOwnerUUID(player.getUUID());
        redstar.setCharging(true); // ensures float logic in tick()

        // Position it above player's head with increased offset
        redstar.setPos(player.getX(), player.getEyeY() + 3.0, player.getZ());

        player.level().addFreshEntity(redstar);
        System.out.println("[DEBUG] Spawned a new charging RedGiant for " + player.getName().getString());
    }

    private static void shootStar(ServerPlayer player, BaseStar star) {
        // Stop charging => star will stop floating in the player's hand
        star.setCharging(false);

        // Convert star's charge level into velocity
        int chargeLevel = star.getChargeLevel();
        star.setFinalCharge(chargeLevel); // optional

        // Switch to normal physics if you want it to fly
        star.noPhysics = false;
        star.setNoGravity(false);

        // 0-100 => scale velocity from ~1.0 to 4.0
        double basePower = 1.0;
        double maxExtra = 3.0;
        double scale = Math.min(basePower + (chargeLevel / 100.0 * maxExtra), basePower + maxExtra);

        // Launch in player's look direction with upward offset
        double upOffset = 0.6; // Increased for a more pronounced arc
        var lookDir = player.getLookAngle();

        // Create a velocity vector with a significant upward component
        var velocity = lookDir.add(0, upOffset, 0).normalize().scale(scale);

        star.setDeltaMovement(velocity);
        System.out.println("[DEBUG] Shooting BaseStar with velocity: " + velocity);
    }

    private static void shootRedStar(ServerPlayer player, RedGiant redstar) {
        // Stop charging -> star will no longer float above player's head
        redstar.setCharging(false);

        // Make it a projectile again
        redstar.noPhysics = false;
        redstar.setNoGravity(false);

        // Retrieve the charge level
        int chargeLevel = redstar.getChargeLevel();  // 0..100
        redstar.setFinalCharge(chargeLevel); // optional

        // Velocity calculation aligned with player's look direction
        double baseSpeed = 1.5;       // Increased minimal speed
        double maxExtra  = 4.5;       // Increased extra speed at full charge
        double scale = Math.min(baseSpeed + (chargeLevel / 100.0 * maxExtra), baseSpeed + maxExtra);

        // Get the player's look direction without additional offset
        var lookDir = player.getLookAngle();

        // Create a velocity vector scaled by the calculated speed
        var velocity = lookDir.normalize().scale(scale);

        // Apply the calculated velocity to the RedGiant
        redstar.setDeltaMovement(velocity);

        // Debug statement to verify velocity
        System.out.println("[DEBUG] Shooting RedGiant with velocity: " + velocity);
    }

    /**
     * Finds a BaseStar near the player (64-block radius).
     * If you allow multiple stars, you may want a more robust approach.
     */
    private static BaseStar findStarByOwner(ServerPlayer player) {
        UUID ownerId = player.getUUID();
        return player.level().getEntitiesOfClass(
                BaseStar.class,
                player.getBoundingBox().inflate(64),
                s -> s.getOwnerUUID() != null && s.getOwnerUUID().equals(ownerId)
        ).stream().findFirst().orElse(null);
    }

    private static RedGiant findRedStarByOwner(ServerPlayer player) {
        UUID ownerId = player.getUUID();
        return player.level().getEntitiesOfClass(
                RedGiant.class,
                player.getBoundingBox().inflate(64),
                s -> s.getOwnerUUID() != null && s.getOwnerUUID().equals(ownerId)
        ).stream().findFirst().orElse(null);
    }
}