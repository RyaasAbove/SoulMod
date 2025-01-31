package net.ryaas.soulmod.powers.voidsong;

import net.minecraft.resources.ResourceLocation;
import net.ryaas.soulmod.SoulMod;
import net.ryaas.soulmod.powers.rg.RedGiant;
import software.bernie.geckolib.model.GeoModel;

public class VoidSongModel extends GeoModel<VoidSong> {
    @Override
    public ResourceLocation getModelResource(VoidSong voidSong) {
        return new ResourceLocation(SoulMod.MODID, "geo/invis.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VoidSong voidSong) {
        return new ResourceLocation(SoulMod.MODID, "textures/entity/invis.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VoidSong voidSong) {
        return new ResourceLocation(SoulMod.MODID, "animations/invis.animation.json");
    }
}
