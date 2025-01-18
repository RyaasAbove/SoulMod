package net.ryaas.soulmod.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ryaas.soulmod.SoulMod;
import net.ryaas.soulmod.player.soulshot.ArmCannon;
import net.ryaas.soulmod.powers.rg.RedExplosion;
import net.ryaas.soulmod.powers.rg.RedGiant;
import net.ryaas.soulmod.powers.soulshot.SoulShot;
import net.ryaas.soulmod.powers.starspawn.basestar.BaseStar;
import net.ryaas.soulmod.powers.starspawn.basestar.BaseStarburn;


public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SoulMod.MODID);

    public static final RegistryObject<EntityType<BaseStar>> BASE_STAR = ENTITIES.register(
            "base_star",
            () -> EntityType.Builder
                    .<BaseStar>of(BaseStar::new, MobCategory.MISC)
                    .setTrackingRange(64)              // how far away the client will track updates
                    .setUpdateInterval(1)             // how many ticks between entity sync packets
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(0.5F, 0.5F)                // bounding box size
                    .build("base_star")
    );

    public static final RegistryObject<EntityType<BaseStarburn>> BASE_STARBURN = ENTITIES.register(
            "base_starburn",
            () -> EntityType.Builder
                    .<BaseStarburn>of(BaseStarburn::new, MobCategory.MISC)
                    .setTrackingRange(64)              // how far away the client will track updates
                    .setUpdateInterval(1)             // how many ticks between entity sync packets
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(5.5F, 5.5F)                // bounding box size
                    .build("base_star")
    );

    public static final RegistryObject<EntityType<RedGiant>> RED_GIANT = ENTITIES.register(
            "red_giant",
            () -> EntityType.Builder
                    .<RedGiant>of(RedGiant::new, MobCategory.MISC)
                    .setTrackingRange(128)              // how far away the client will track updates
                    .setUpdateInterval(1)             // how many ticks between entity sync packets
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(2.5F, 2.5F)                // bounding box size
                    .build("red_giant")
    );

    public static final RegistryObject<EntityType<RedExplosion>> RED_EXPLOSION = ENTITIES.register(
            "red_explosion",
            () -> EntityType.Builder
                    .<RedExplosion>of(RedExplosion::new, MobCategory.MISC)
                    .setTrackingRange(64)              // how far away the client will track updates
                    .setUpdateInterval(1)             // how many ticks between entity sync packets
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(5.5F, 5.5F)                // bounding box size
                    .build("red_explosion")
    );

    public static final RegistryObject<EntityType<SoulShot>> SOUL_SHOT = ENTITIES.register(
            "soul_shot_beam",
            () -> EntityType.Builder
                    .<SoulShot>of(SoulShot::new, MobCategory.MISC)
                    .setTrackingRange(64)              // how far away the client will track updates
                    .setUpdateInterval(1)             // how many ticks between entity sync packets
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(0.25F, 0.25F)                // bounding box size
                    .build("soul_shot")
    );

    public static final RegistryObject<EntityType<ArmCannon>> ARM_CANNON = ENTITIES.register(
            "arm_cannon",
            () -> EntityType.Builder
                    .<ArmCannon>of(ArmCannon::new, MobCategory.MISC)
                    .setTrackingRange(64)              // how far away the client will track updates
                    .setUpdateInterval(1)             // how many ticks between entity sync packets
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(0.25F, 0.25F)                // bounding box size
                    .build("arm_cannon")
    );

}


