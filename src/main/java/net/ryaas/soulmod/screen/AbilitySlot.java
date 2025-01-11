package net.ryaas.soulmod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.ryaas.soulmod.SoulMod;
import net.ryaas.soulmod.powers.Ability;

class AbilitySlot {
    final int x, y, width, height;
    final int slotIndex;
    String abilityId;

    public AbilitySlot(int x, int y, int width, int height, int slotIndex) {
        this.x = x; this.y = y; this.width = width; this.height = height; this.slotIndex = slotIndex;
    }

    public void setAbilityId(String abilityId) {
        this.abilityId = abilityId;
    }

    public boolean isMouseOver(int mx, int my) {
        return mx >= x && mx < x+width && my >= y && my < y+height;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Draw a simple slot background
        graphics.fill(x, y, x+width, y+height, 0xFF666666);

        // If there's an ability equipped here, draw its icon
        if (abilityId != null && !abilityId.isEmpty()) {
            var ability = net.ryaas.soulmod.powers.AbilityRegistry.getAbility(abilityId);
            if (ability != null) {
                int iconSize = 20;
                int drawX = x + (width - iconSize)/2;
                int drawY = y + (height - iconSize)/2;
                RenderSystem.setShaderTexture(0, new ResourceLocation(SoulMod.MODID, "textures/gui/abilities/" + ability.getIconPath()));
                graphics.blit(new ResourceLocation(SoulMod.MODID, "textures/gui/abilities/" + ability.getIconPath()), drawX, drawY, 0, 0, iconSize, iconSize, iconSize, iconSize);
            }
        }
    }
}