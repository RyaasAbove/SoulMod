package net.ryaas.soulmod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.ryaas.soulmod.SoulMod;
import net.ryaas.soulmod.network.NetworkHandler;
import net.ryaas.soulmod.network.c2spackets.C2SEquipAbilityPacket;
import net.ryaas.soulmod.powers.Ability;
import net.ryaas.soulmod.powers.AbilityCapability;
import net.ryaas.soulmod.powers.AbilityRegistry;

import java.util.ArrayList;
import java.util.List;

public class CharSheet extends Screen {

    private static final Component TITLE = Component.literal("Soul Path"); // Window title

    // GUI dimensions
    private final int imageWidth;
    private final int imageHeight;
    private int leftPos;   // screen X of the GUI
    private int topPos;    // screen Y of the GUI

    // Resource for the background
    private static final ResourceLocation BACKGROUND_TEXTURE =
            new ResourceLocation(SoulMod.MODID, "textures/gui/screens/background.png");

    // We'll keep 5 "slots" for equipping abilities
    private final List<AbilitySlot> slots = new ArrayList<>();
    private final List<AbilityIcon> availableAbilities = new ArrayList<>();

    // Drag-and-drop state
    private AbilityIcon draggingIcon = null;
    private int draggingOffsetX = 0;
    private int draggingOffsetY = 0;

    private AbilitySlot draggingFromSlot = null;

    public CharSheet() {
        super(TITLE);
        this.imageWidth = 300;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();

        // Position our GUI in the middle of the screen
        this.leftPos = (this.width - imageWidth) / 2;
        this.topPos = (this.height - imageHeight) / 2;

        // Build the "available abilities" icons on the left side
        populateAvailableAbilities();

        // Create 5 "equipped" slots at the bottom of our panel, centered horizontally
        int slotCount = 5;
        int slotSize = 24;
        int padding = 5;
        // total width for all slots + internal padding (4 gaps if 5 slots)
        int totalSlotsWidth = (slotCount * slotSize) + ((slotCount - 1) * padding);

        // You can adjust how "far" from the bottom you want them.
        // For instance, (imageHeight - slotSize - 10) places them near the bottom edge:
        int slotsY = topPos + (imageHeight - slotSize - 10);

        // Figure out where to start so the group is centered
        int startX = leftPos + (imageWidth - totalSlotsWidth) / 2;

        for (int i = 0; i < slotCount; i++) {
            int slotX = startX + i * (slotSize + padding);
            slots.add(new AbilitySlot(slotX, slotsY, slotSize, slotSize, i));
        }

        // Load whichever abilities the server says we currently have equipped
        loadEquippedAbilities();
    }

    /**
     * Creates an icon for each known ability in AbilityRegistry,
     * laying them out in rows near the top-left of our GUI.
     */
    private void populateAvailableAbilities() {
        int iconSize = 20;
        int padding = 5;
        int xStart = leftPos + 10;
        int yStart = topPos + 30;
        int perRow = 5;

        var ids = AbilityRegistry.getAllAbilityIds().toArray(new String[0]);
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            var ab = AbilityRegistry.getAbility(id);
            if (ab != null) {
                int row = i / perRow;
                int col = i % perRow;
                int x = xStart + col * (iconSize + padding);
                int y = yStart + row * (iconSize + padding);

                availableAbilities.add(new AbilityIcon(
                        id,              // abilityId
                        x, y,            // position
                        iconSize, iconSize,
                        ab.getIconPath() // path to texture
                ));
            }
        }
    }

    /**
     * Reads the currently equipped abilities from the player’s
     * AbilityCapability and updates our local slots to show them.
     */
    public void loadEquippedAbilities() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        player.getCapability(AbilityCapability.PLAYER_ABILITIES_CAPABILITY).ifPresent(cap -> {
            int count = cap.getSlotCount(); // likely 5
            for (int i = 0; i < Math.min(slots.size(), count); i++) {
                String abilityId = cap.getAbilityInSlot(i);
                slots.get(i).setAbilityId(abilityId);
            }
        });
    }

    /* =========================================================
       Mouse / Dragging Logic
     ========================================================= */

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Left-click is button == 0
        if (button == 0) {
            // If we're already dragging something, no new drag
            if (draggingIcon != null) {
                return super.mouseClicked(mouseX, mouseY, button);
            }

            // Check if we clicked on a slot that already has an ability
            for (AbilitySlot slot : slots) {
                if (slot.isMouseOver((int) mouseX, (int) mouseY)) {
                    if (slot.abilityId != null && !slot.abilityId.isEmpty()) {
                        // "Un-equipping" from that slot
                        NetworkHandler.INSTANCE.sendToServer(
                                new C2SEquipAbilityPacket(slot.slotIndex, "")
                        );
                        slot.setAbilityId(null); // visually remove on client
                        return true;
                    }
                }
            }

            // Otherwise, check if we clicked on an ability icon to drag it
            for (AbilityIcon icon : availableAbilities) {
                if (icon.isMouseOver((int) mouseX, (int) mouseY)) {
                    draggingIcon = icon;
                    draggingOffsetX = (int) mouseX - icon.x;
                    draggingOffsetY = (int) mouseY - icon.y;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingIcon != null && button == 0) {
            // We don't actually move the icon in real-time,
            // we just highlight it or show a "ghost" while dragging in render().
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingIcon != null) {
            // We "drop" the icon onto a slot, if hovered
            for (AbilitySlot slot : slots) {
                if (slot.isMouseOver((int) mouseX, (int) mouseY)) {
                    String abilityId = draggingIcon.abilityId;
                    NetworkHandler.INSTANCE.sendToServer(
                            new C2SEquipAbilityPacket(slot.slotIndex, abilityId)
                    );
                    slot.setAbilityId(abilityId); // visually update
                    break;
                }
            }
            draggingIcon = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /* =========================================================
       Rendering
     ========================================================= */

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Draw background
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        graphics.blit(
                BACKGROUND_TEXTURE,
                this.leftPos,
                this.topPos,
                0, 0,
                imageWidth, imageHeight,
                imageWidth, imageHeight
        );

        super.render(graphics, mouseX, mouseY, partialTicks);

        // Render the 5 slots
        for (AbilitySlot slot : slots) {
            slot.render(graphics, mouseX, mouseY, partialTicks);
        }

        // Render each ability icon (unless it's the one we're dragging)
        for (AbilityIcon icon : availableAbilities) {
            if (icon == draggingIcon) continue;
            icon.render(graphics);
        }

        // If dragging an icon, render it near the mouse
        if (draggingIcon != null) {
            int drawX = mouseX - draggingOffsetX;
            int drawY = mouseY - draggingOffsetY;
            draggingIcon.renderAt(graphics, drawX, drawY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /* =========================================================
       Inner Classes for Slots / Icons
     ========================================================= */

    /**
     * Represents a slot in the GUI where a player can equip an ability.
     */
    private static class AbilitySlot {
        public int x, y, width, height;
        public int slotIndex;
        public String abilityId; // which ability is currently in this slot (null/empty if none)

        public AbilitySlot(int x, int y, int w, int h, int index) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.slotIndex = index;
            this.abilityId = null;
        }

        public void setAbilityId(String abilityId) {
            this.abilityId = abilityId;
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX < x + width
                    && mouseY >= y && mouseY < y + height;
        }

        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            // Draw a simple slot background
            int color = 0x44FFFFFF; // translucent white
            graphics.fillGradient(x, y, x + width, y + height, color, color);

            // If there's an ability equipped, we could draw an icon or text
            if (abilityId != null && !abilityId.isEmpty()) {
                // Optionally, draw the ability's icon or name
                // For simplicity, just draw the abilityId as text
                graphics.drawString(
                        Minecraft.getInstance().font,
                        abilityId,
                        x + 2, y + 2,
                        0xFFFFFF, false
                );
            }
        }
    }

    /**
     * Represents an ability icon in the "available" pool on the left side.
     * The player can drag this icon onto a slot to equip the ability.
     */
    private static class AbilityIcon {
        public final String abilityId;
        public int x, y, width, height;
        public final ResourceLocation texture;

        public AbilityIcon(String abilityId, int x, int y, int w, int h, String iconPath) {
            this.abilityId = abilityId;
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.texture = new ResourceLocation(iconPath);
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX < x + width
                    && mouseY >= y && mouseY < y + height;
        }

        public void render(GuiGraphics graphics) {
            renderAt(graphics, x, y);
        }

        public void renderAt(GuiGraphics graphics, int drawX, int drawY) {
            RenderSystem.setShaderTexture(0, texture);
            graphics.blit(texture, drawX, drawY, 0, 0, width, height, width, height);
        }
    }
}


