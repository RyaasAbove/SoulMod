package net.ryaas.soulmod;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.ryaas.soulmod.assisting.visuals.ModParticleTypes;
import net.ryaas.soulmod.assisting.ModSounds;
import net.ryaas.soulmod.assisting.visuals.comettrail.RedCometTrailProvider;
import net.ryaas.soulmod.entities.ModEntities;
import net.ryaas.soulmod.network.NetworkHandler;
import net.ryaas.soulmod.powers.AbilityRegistry;
import net.ryaas.soulmod.powers.rg.*;
import net.ryaas.soulmod.powers.starspawn.basestar.BaseStarRenderer;
import net.ryaas.soulmod.powers.starspawn.basestar.BaseStarburnRenderer;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SoulMod.MODID)
public class SoulMod {
    public static final String MODID = "soulmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SoulMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register event listeners on the mod event bus
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);


        // Register ourselves on the Forge event bus for non-mod-lifecycle events
        MinecraftForge.EVENT_BUS.register(this);
        ModSounds.SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModEntities.ENTITIES.register(modEventBus);

        ModParticleTypes.PARTICLE_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());

        // If you have a config file:
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Use enqueueWork to ensure this runs at the correct time after registries are done
        event.enqueueWork(() -> {
            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

            // Register abilities
            AbilityRegistry.registerAbilities();
            // Register network packets
            NetworkHandler.register();
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Add items to creative tabs if needed
        // event.register(...);
    }

    // This event is fired on Forge event bus, and we already registered MinecraftForge.EVENT_BUS
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Perform server-start logic if needed
        LOGGER.info("Server starting with SoulMod loaded!");
    }



    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticleTypes.RED_EXPLOSION.get(),
                    new RedExplosionParticleRegistration<>());
            event.registerSpriteSet(ModParticleTypes.RED_ORB.get(),
                    RedCometTrailProvider::new);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP of SoulMod");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            // Any client-specific setup like rendering, keybinds, etc.
            event.enqueueWork(() -> {
                EntityRenderers.register(ModEntities.BASE_STAR.get(), BaseStarRenderer::new);
                EntityRenderers.register(ModEntities.BASE_STARBURN.get(), BaseStarburnRenderer::new);
                EntityRenderers.register(ModEntities.RED_GIANT.get(), RenderRG::new);
                EntityRenderers.register(ModEntities.RED_EXPLOSION.get(), RedExplosionRenderer::new);




            });
        }
    }

}
