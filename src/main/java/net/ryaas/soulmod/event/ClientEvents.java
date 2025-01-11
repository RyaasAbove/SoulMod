package net.ryaas.soulmod.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.ryaas.soulmod.SoulMod;
import net.ryaas.soulmod.assisting.KeyBinding;
import net.ryaas.soulmod.client.ClientHooks;
import net.ryaas.soulmod.entities.ModEntities;
import net.ryaas.soulmod.network.NetworkHandler;
import net.ryaas.soulmod.network.c2spackets.C2SActivateSelectedAbilityPacket;

import org.lwjgl.glfw.GLFW;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = SoulMod.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents{

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event){
            if(KeyBinding.MENU.consumeClick()){
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openCharMenu());
            }

            if(KeyBinding.USE_ABILITY.consumeClick()){
                NetworkHandler.INSTANCE.sendToServer(new C2SActivateSelectedAbilityPacket());
            }
        }
    }


    @Mod.EventBusSubscriber(modid = SoulMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents{
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event){

            event.register(KeyBinding.MENU);
            event.register(KeyBinding.USE_ABILITY);
            event.register(KeyBinding.OPEN_RAD);
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {

        }
    }

}
