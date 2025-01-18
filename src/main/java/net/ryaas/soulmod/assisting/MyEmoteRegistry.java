package net.ryaas.soulmod.assisting;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MyEmoteRegistry {
    public static KeyframeAnimation CHARGINGSS_EMOTE = null;

    /**
     * Called at mod setup to load the "charging" emote from our JSON resource.
     */
    public static void loadEmotes() {
        // Try loading "charging.json" from assets/<your_modid>/emotes/charging.json
        try (InputStream is = MyEmoteRegistry.class.getResourceAsStream("/assets/soulmod/player_animation/chargingss.json")) {
            if (is != null) {
                List<KeyframeAnimation> animations =
                        // (quarkName=null, format="json" for Emotecraft/GeckoLib)
                        ServerEmoteAPI.deserializeEmote(is, null, "json");

                if (!animations.isEmpty()) {
                    CHARGINGSS_EMOTE = animations.get(0);
                    System.out.println("[DEBUG] Loaded CHARGING_EMOTE successfully!");
                } else {
                    System.out.println("[WARN] No animations found in chargingss.json!");
                }
            } else {
                System.out.println("[ERROR] Could not find charging.json resource!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

