package net.ryaas.soulmod.assisting;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.util.thread.StrictQueue;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
    public static final String KEY_CATEGORY = "key.category.soulmod.soul";

    public static final String KEY_OPEN_MENU = "key.soulmod.open_menu";
    public static final String KEY_USE_ABILITY = "key.soulmod.use_ability";
    public static final String KEY_USE_SET_ABILITY = "key.soulmod.use_set_ability";
    public static final String KEY_OPEN_RAD = "key.soulmod.open_rad_menu";

    public static final KeyMapping MENU = new KeyMapping(KEY_OPEN_MENU, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, KEY_CATEGORY);
    public static final KeyMapping USE_ABILITY = new KeyMapping(KEY_USE_ABILITY, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY);

    public static final KeyMapping USE_SET_ABILITY= new KeyMapping(KEY_USE_SET_ABILITY, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F, KEY_CATEGORY);

    public static final KeyMapping OPEN_RAD = new KeyMapping(KEY_OPEN_RAD, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F, KEY_CATEGORY);
}
