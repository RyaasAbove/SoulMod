package net.ryaas.soulmod.assisting;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.IExtensibleEnum;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;


public class PlayerAnimationManager {
    public static int animIndex = 0;

    // Optionally, you could store a global map of players -> layers, if needed.
    // e.g. public static final Map<Player, ModifierLayer<IAnimation>> animationLayers = new HashMap<>();

    public PlayerAnimationManager() {}

    /**
     * Example: Only do something if client-side.
     */
    public static void playAnimation(Level level, AnimationsList animation) {
        if (level.isClientSide) {
            playAnimation(animation);
        }
    }

    /**
     * Overloaded method: Send a packet or do local logic.
     * Just a placeholder.
     */
    public static void playAnimation(AnimationsList animation) {
        System.out.println("[DEBUG] playAnimation called for: " + animation.name());
        // e.g., send a packet or do something else
    }

    /**
     * Actually apply or stop the animation on a given layer.
     * This would be called from the client after receiving SyncPlayerAnimationPacket.
     */
    public static void applyAnimationToLayer(@Nullable KeyframeAnimation anim,
                                             ModifierLayer<IAnimation> layer,
                                             boolean isOtherLayer) {
        boolean isFirstPersonModLoaded = ModList.get().isLoaded("firstpersonmod");

        if (anim == null) {
            // Stop / clear
            layer.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(3, Ease.OUTCIRC),
                    null
            );
        } else {
            int fadeTime = isOtherLayer ? 20 : 3;
            KeyframeAnimationPlayer animPlayer = new KeyframeAnimationPlayer(anim)
                    .setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
                    .setFirstPersonConfiguration(
                            new FirstPersonConfiguration()
                                    .setShowRightArm(!isFirstPersonModLoaded)
                                    .setShowRightItem(!isFirstPersonModLoaded)
                                    .setShowLeftArm(!isFirstPersonModLoaded)
                                    .setShowLeftItem(!isFirstPersonModLoaded)
                    );
            layer.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(fadeTime, Ease.OUTCIRC),
                    animPlayer,
                    false
            );
        }
    }

    /**
     * Get the enum constant from an integer ID.
     */
    public static AnimationsList getAnimFromId(int id) {
        for (AnimationsList anim : AnimationsList.values()) {
            if (anim.getId() == id) {
                return anim;
            }
        }
        throw new IllegalStateException("Animation ID is invalid: " + id);
    }

    // ============================
    //       NESTED ENUM
    // ============================
    public static enum AnimationsList implements IExtensibleEnum {
        CLEAR(null),
        CHARGINGSS("chargingss");
        // Add more as needed: CHARGING("charging"), SHOOTING("shooting"), ...

        private final String path;
        private final int id;

        private AnimationsList(String file) {
            this.path = file;
            this.id = PlayerAnimationManager.animIndex++;
        }

        public static AnimationsList create(String name, String file) {
            throw new IllegalStateException("Enum not extended");
        }

        @Nullable
        public KeyframeAnimation getAnimation() {
            if (this.path == null) {
                return null;
            }
            // e.g. "yourmod:aim"
            ResourceLocation animLoc = new ResourceLocation("soulmod", this.path);
            return PlayerAnimationRegistry.getAnimation(animLoc);
        }

        @Nullable
        public String getPath() {
            return this.path;
        }

        public int getId() {
            return this.id;
        }
    }
}