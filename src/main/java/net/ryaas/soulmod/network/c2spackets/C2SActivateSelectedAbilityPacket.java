package net.ryaas.soulmod.network.c2spackets;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.ryaas.soulmod.powers.AbilityCapability;

import java.util.function.Supplier;

public class C2SActivateSelectedAbilityPacket {

    public C2SActivateSelectedAbilityPacket() {
        // no fields needed
    }

    public static void encode(C2SActivateSelectedAbilityPacket msg, FriendlyByteBuf buf) {
        // no data to write
    }

    public static C2SActivateSelectedAbilityPacket decode(FriendlyByteBuf buf) {
        return new C2SActivateSelectedAbilityPacket();
    }

    public static void handle(C2SActivateSelectedAbilityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection() != NetworkDirection.PLAY_TO_SERVER) return;
            ServerPlayer serverPlayer = ctx.get().getSender();
            if (serverPlayer == null) return;

            // Look up the player's "active" ability from the capability
            serverPlayer.getCapability(AbilityCapability.PLAYER_ABILITIES_CAPABILITY).ifPresent(cap -> {
                String abilityId = cap.getActiveAbility();
                if (abilityId == null || abilityId.isEmpty()) {
                    System.out.println("Server: No active ability selected!");
                    return;
                }

                System.out.println("Server: Activating selected ability -> " + abilityId);
                activateAbility(serverPlayer, abilityId);
            });
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Decide what to do based on the active abilityId.
     */
    private static void activateAbility(ServerPlayer player, String abilityId) {
        // Example: "fireball" -> shootFireball, "starspawn" -> spawn or do something else
        if (abilityId.equals("fireball")) {
            shootFireball(player);
        }
        else if (abilityId.equals("starspawn")) {
            // spawn or trigger the star
        }
        // else if (...) more abilities
    }

    private static void shootFireball(ServerPlayer player){
        Level world = player.level();
        if (world.isClientSide()) return; // Only spawn entities on the server

        // Player's look direction
        Vec3 look = player.getLookAngle();
        double x = player.getX() + look.x;
        double y = player.getEyeY() + 0.5;
        double z = player.getZ() + look.z;

        // Create a small fireball entity
        SmallFireball fireball = new SmallFireball(EntityType.SMALL_FIREBALL, world);
        fireball.setPos(x, y, z);
        fireball.setOwner(player);

        // Shoot the fireball forward
        float velocity = 1.5F;
        float inaccuracy = 0.0F;
        fireball.shoot(look.x, look.y, look.z, velocity, inaccuracy);

        world.addFreshEntity(fireball);

        // Debug message and a flame particle
        System.out.println("Server: Fired a fireball!");
        world.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
    }
}