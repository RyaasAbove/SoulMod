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

        // Create 5 normal slots at the bottom of our panel, centered horizontally
        int slotCount = 5;
        int slotSize = 24;
        int padding = 5;
        int totalSlotsWidth = (slotCount * slotSize) + ((slotCount - 1) * padding);

        // Vertical position for the normal slots
        int slotsY = topPos + (imageHeight - slotSize - 10);

        // Figure out where to start so the group is centered
        int startX = leftPos + (imageWidth - totalSlotsWidth) / 2;

        for (int i = 0; i < slotCount; i++) {
            int slotX = startX + i * (slotSize + padding);
            slots.add(new AbilitySlot(slotX, slotsY, slotSize, slotSize, i));
        }

        // Add the 6th slot slightly further apart for the movement-change ability
        int movementSlotPadding = 20; // Additional padding for the 6th slot
        int movementSlotX = startX + slotCount * (slotSize + padding) + movementSlotPadding;
        slots.add(new AbilitySlot(movementSlotX, slotsY, slotSize, slotSize, slotCount));

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
        int perRow = 5; //change to extend row

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
        if (button == 0) {
            // If we're already dragging something, don't start a new drag
            if (draggingIcon != null) {
                return super.mouseClicked(mouseX, mouseY, button);
            }

            // 1) Check if we clicked on a slot that already has an ability
            for (AbilitySlot slot : slots) {
                if (slot.isMouseOver((int) mouseX, (int) mouseY)) {
                    if (slot.abilityId != null && !slot.abilityId.isEmpty()) {
                        // Begin dragging from this slot
                        draggingFromSlot = slot;

                        // Create an icon to visually drag
                        draggingIcon = new AbilityIcon(
                                slot.abilityId,
                                slot.x, slot.y,
                                slot.width, slot.height,
                                AbilityRegistry.getAbility(slot.abilityId).getIconPath()
                        );
                        // Remove it from the slot visually
                        slot.setAbilityId(null);

                        // Store offsets so we can render it at correct position
                        draggingOffsetX = (int) mouseX - slot.x;
                        draggingOffsetY = (int) mouseY - slot.y;

                        return true;
                    }
                }
            }

            // 2) Otherwise, check if we clicked on an ability icon to drag it
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
            boolean droppedOnSlot = false;

            // Check if we dropped on a slot
            for (AbilitySlot slot : slots) {
                if (slot.isMouseOver((int) mouseX, (int) mouseY)) {
                    String abilityId = draggingIcon.abilityId;
                    // Equip
                    NetworkHandler.INSTANCE.sendToServer(
                            new C2SEquipAbilityPacket(slot.slotIndex, abilityId)
                    );
                    slot.setAbilityId(abilityId);

                    droppedOnSlot = true;
                    break;
                }
            }

            // If we did NOT drop on any slot
            if (!droppedOnSlot) {
                // Were we dragging from a slot originally?
                if (draggingFromSlot != null) {
                    // Then we want to “unequip,” i.e. set that slot to empty on server
                    NetworkHandler.INSTANCE.sendToServer(
                            new C2SEquipAbilityPacket(draggingFromSlot.slotIndex, "")
                    );
                    // draggingFromSlot is already visually empty. Nothing else to do.
                }
                // If we were dragging from the “available abilities” list, do nothing
                // (the user basically just cancelled the drag).
            }

            // Clear drag states
            draggingIcon = null;
            draggingFromSlot = null;

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
            slot.render(graphics, mouseX, mouseY, partialTicks, draggingIcon);
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

        public void render(
                GuiGraphics graphics,
                int mouseX,
                int mouseY,
                float partialTicks,
                AbilityIcon currentlyDragging
        ) {
            // Base slot color
            int slotColor = 0x44FFFFFF; // translucent white

            boolean hovered = isMouseOver(mouseX, mouseY);

            // If we are currently dragging an icon, highlight the slot on hover
            if (currentlyDragging != null && hovered) {
                // e.g. bright yellow highlight
                slotColor = 0x88FFFF00;
            }

            // Draw the slot background (or highlight if hovered)
            graphics.fillGradient(x, y, x + width, y + height, slotColor, slotColor);

            // If there's an ability equipped, draw its ID or an icon (up to you)
            if (abilityId != null && !abilityId.isEmpty()) {
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


