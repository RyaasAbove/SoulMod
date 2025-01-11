package net.ryaas.soulmod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class AnimatedImageButton extends ImageButton {
    private final ResourceLocation spritesheet;
    private final int frameWidth;
    private final int frameHeight;
    private final int framesPerState;
    private final long frameDuration; // in milliseconds
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private final boolean animateOnHover;

    // Initial texture coordinates for the normal state
    private final int initialU;
    private final int initialV;

    /**
     * Constructs an AnimatedImageButton.
     *
     * @param x                The X position of the button.
     * @param y                The Y position of the button.
     * @param width            The width of the button.
     * @param height           The height of the button.
     * @param u                The U texture coordinate for the normal state.
     * @param v                The V texture coordinate for the normal state.
     * @param spritesheet      The ResourceLocation of the button's spritesheet.
     * @param textureWidth     The width of the entire spritesheet.
     * @param textureHeight    The height of the entire spritesheet.
     * @param onPress          The action performed when the button is pressed.
     * @param tooltip          The tooltip Component displayed on hover.
     * @param frameWidth       The width of each animation frame.
     * @param frameHeight      The height of each animation frame.
     * @param framesPerState   The number of animation frames per state.
     * @param frameDuration    The duration each frame is displayed (in ms).
     * @param animateOnHover   If true, animation plays only when hovered.
     */
    public AnimatedImageButton(int x, int y, int width, int height,
                               int u, int v,
                               ResourceLocation spritesheet,
                               int textureWidth, int textureHeight,
                               ImageButton.OnPress onPress,
                               Component tooltip,
                               int frameWidth, int frameHeight,
                               int framesPerState,
                               long frameDuration,
                               boolean animateOnHover) {
        super(x, y, width, height, u, v, height, spritesheet, textureWidth, textureHeight, onPress, tooltip);
        this.spritesheet = spritesheet;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.framesPerState = framesPerState;
        this.frameDuration = frameDuration;
        this.animateOnHover = animateOnHover;
        this.initialU = u;
        this.initialV = v;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean isHovered = this.isHovered();
        boolean isPressed = isHovered && isMouseButtonDown();

        // Determine the current state row
        int stateRow = 0; // Normal
        if (isPressed) {
            stateRow = 2; // Pressed
        } else if (isHovered) {
            stateRow = 1; // Hovered
        }

        int stateV = initialV + stateRow * frameHeight;

        // Determine if animation should occur
        boolean shouldAnimate = animateOnHover && isHovered;

        if (shouldAnimate) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFrameTime > frameDuration) {
                currentFrame = (currentFrame + 1) % framesPerState;
                lastFrameTime = currentTime;
            }
        } else {
            currentFrame = 0; // Reset to first frame when not animating
        }

        // Calculate U coordinate based on current frame
        int renderU = initialU + currentFrame * frameWidth;

        // Render the appropriate frame
        RenderSystem.setShaderTexture(0, this.spritesheet);
        graphics.blit(this.spritesheet, this.getX(), this.getY(), renderU, stateV, this.width, this.height, this.textureWidth, this.textureHeight);

        // Render the tooltip if hovered

    }
    private boolean isMouseButtonDown() {
        long windowHandle = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
    }
}
