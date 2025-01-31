package net.ryaas.soulmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SoulModClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final  ForgeConfigSpec SPEC;

    static {
        BUILDER.push("Config for SoulMod");

        //DEFINE CONFIGS HERE

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}