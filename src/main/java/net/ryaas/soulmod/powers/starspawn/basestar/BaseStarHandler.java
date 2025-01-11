package net.ryaas.soulmod.powers.starspawn.basestar;

import net.minecraft.server.level.ServerPlayer;
import net.ryaas.soulmod.entities.ModEntities;

public class BaseStarHandler {
    public static void spawnStarForPlayer(ServerPlayer player) {
        // 1) Make sure we are on the server (player is a ServerPlayer)
        if (player.level().isClientSide) {
            return; // Must spawn entities on server
        }

        // 2) Create the BaseStar instance
        BaseStar star = ModEntities.BASE_STAR.get().create(player.level());
        if (star == null) {
            System.out.println("[DEBUG] Failed to create BaseStar entity!");
            return;
        }

        // 3) Set the owner UUID so the star knows who spawned it
        star.setOwnerUUID(player.getUUID());

        // 4) Position the star near or above the player
        double x = player.getX();
        double y = player.getY() + 1.5; // a bit above the player's head
        double z = player.getZ();
        star.setPos(x, y, z);

        // 5) Optionally set charging = true immediately if you want it to start charging
        star.setCharging(true);

        // 6) Finally, spawn (add) the entity in the level
        player.level().addFreshEntity(star);

        System.out.println("[DEBUG] Spawned BaseStar for player " + player.getName().getString());
    }
}
