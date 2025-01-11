package net.ryaas.soulmod.client;

import net.minecraft.client.Minecraft;
import net.ryaas.soulmod.screen.CharSheet;

public class ClientHooks {
    public static void openCharMenu(){
        Minecraft.getInstance().setScreen(new CharSheet());
    }
}
