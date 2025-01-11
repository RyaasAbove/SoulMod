package net.ryaas.soulmod.screen.radialmenu;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.ryaas.soulmod.network.NetworkHandler;
import net.ryaas.soulmod.network.c2spackets.C2SSetActiveAbilityPacket;
import net.ryaas.soulmod.powers.AbilityCapability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbilityRadialScreen extends Screen {
    // The list of currently "equipped" abilities we read from the capability
    private final List<String> equippedAbilities = new ArrayList<>();

    // Example: number of slots we expect. If you store it in cap, read that dynamically.
    private static final int MAX_SLOTS = 5;

    public AbilityRadialScreen() {
        super(Component.literal("Select Ability"));
    }

    @Override
    protected void init() {
        super.init();
        // We'll fetch the 5 slot-based abilities from the client capability
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(AbilityCapability.PLAYER_ABILITIES_CAPABILITY).ifPresent(cap -> {
                // read up to cap.getSlotCount() slots
                int count = cap.getSlotCount();
                for (int i = 0; i < count; i++) {
                    String ability = cap.getAbilityInSlot(i);
                    if (ability != null && !ability.isEmpty()) {
                        equippedAbilities.add(ability);
                    }
                }
            });
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // A tinted background
        this.renderBackground(guiGraphics);

        // radial logic
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int radius = 80;

        // The angle slice each ability gets
        // e.g. 360 / equippedAbilities.size()
        if (!equippedAbilities.isEmpty()) {
            float sliceAngle = 360f / equippedAbilities.size();

            // Draw each slice or label
            for (int i = 0; i < equippedAbilities.size(); i++) {
                String ability = equippedAbilities.get(i);

                // angle in degrees
                float angleDeg = (i + 0.5f) * sliceAngle;
                double angleRad = Math.toRadians(angleDeg);

                // label position
                float labelX = centerX + (float)Math.cos(angleRad) * radius;
                float labelY = centerY + (float)Math.sin(angleRad) * radius;

                // center the text
                int textWidth  = font.width(ability);
                int textHeight = font.lineHeight;
                int drawX = (int)(labelX - textWidth/2f);
                int drawY = (int)(labelY - textHeight/2f);

                // draw the ability name
                guiGraphics.drawString(this.font, ability, drawX, drawY, 0xFFFFFF, false);
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !equippedAbilities.isEmpty()) {
            // figure out which slice we clicked
            int centerX = width / 2;
            int centerY = height / 2;
            double dx = mouseX - centerX;
            double dy = mouseY - centerY;

            // angle in degrees
            double angleDeg = Math.toDegrees(Math.atan2(dy, dx));
            if (angleDeg < 0) angleDeg += 360;

            float sliceAngle = 360f / equippedAbilities.size();
            int selectedIndex = (int)(angleDeg / sliceAngle);

            if (selectedIndex >= 0 && selectedIndex < equippedAbilities.size()) {
                String selectedAbility = equippedAbilities.get(selectedIndex);

                // Send packet to server to set this as the active ability
                NetworkHandler.INSTANCE.sendToServer(new C2SSetActiveAbilityPacket(selectedAbility));
            }

            // close the radial after selection
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        super.onClose();
        // Return to game
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}