package net.ryaas.soulmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SoulModCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ABILITIES_BREAK_BLOCKS;

    static{
        BUILDER.push("Configs for SoulMod");

    ABILITIES_BREAK_BLOCKS = BUILDER.comment("Do abilities break blocks?")
                    .define("Ability Status", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
