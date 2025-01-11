package net.ryaas.soulmod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.ryaas.soulmod.SoulMod;

class AbilityIcon {
    final String abilityId;
    final int x, y, width, height;
    final String iconPath;

    public AbilityIcon(String abilityId, int x, int y, int width, int height, String iconPath) {
        this.abilityId = abilityId;
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.iconPath = iconPath;
    }

    public boolean isMouseOver(int mx, int my) {
        return mx >= x && mx < x+width && my >= y && my < y+height;
    }

    public void render(GuiGraphics graphics) {
        renderAt(graphics, x, y);
    }

    public void renderAt(GuiGraphics graphics, int drawX, int drawY) {
        RenderSystem.setShaderTexture(0, new ResourceLocation(SoulMod.MODID, "textures/gui/abilities/" + iconPath));
        graphics.blit(new ResourceLocation(SoulMod.MODID, "textures/gui/abilities/" + iconPath), drawX, drawY, 0, 0, width, height, width, height);
    }
}