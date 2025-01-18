package net.ryaas.soulmod.network.s2cpackets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.ryaas.soulmod.assisting.AnimatorMaps;
import net.ryaas.soulmod.assisting.PlayerAnimationManager;

import java.util.function.Supplier;

public class S2CSyncPlayerAnimationPacket {
    private final int playerId;
    private final int animId;

    public S2CSyncPlayerAnimationPacket(int playerId, int animId) {
        this.playerId = playerId;
        this.animId = animId;
    }

    public static S2CSyncPlayerAnimationPacket decode(FriendlyByteBuf buf) {
        int playerId = buf.readInt();
        int animId = buf.readInt();
        return new S2CSyncPlayerAnimationPacket(playerId, animId);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerId);
        buf.writeInt(animId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Entity entity = mc.level.getEntity(this.playerId);
            if (entity instanceof AbstractClientPlayer player) {
                // 1) Find the enum from animId
                var animListEntry = PlayerAnimationManager.getAnimFromId(this.animId);

                // 2) Fetch the actual KeyframeAnimation
                var keyAnim = animListEntry.getAnimation();

                // 3) Retrieve the layer from CSAnimator
                var layer = AnimatorMaps.animationData.get(player);
                if (layer == null) {
                    System.out.println("[DEBUG] No ModifierLayer found for " + player.getName().getString());
                    return;
                }

                // 4) Apply the animation
                PlayerAnimationManager.applyAnimationToLayer(keyAnim, layer, false);

                System.out.println("[DEBUG] Client applying anim "
                        + animListEntry.name()
                        + " to player " + player.getName().getString());
            }
        });
        return true;
    }
}


