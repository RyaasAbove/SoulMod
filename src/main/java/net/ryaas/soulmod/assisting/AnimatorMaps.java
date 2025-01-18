package net.ryaas.soulmod.assisting;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.HashMap;
import java.util.Map;

public class AnimatorMaps {
    // One layer map for "normal" animations:
    public static final Map<AbstractClientPlayer, ModifierLayer<IAnimation>> animationData = new HashMap<>();

    // Optionally a second layer map for "other" or "overlay" animations:
    public static final Map<AbstractClientPlayer, ModifierLayer<IAnimation>> otherAnimationData = new HashMap<>();
}