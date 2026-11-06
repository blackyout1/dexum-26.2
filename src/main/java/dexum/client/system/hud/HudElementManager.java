package dexum.client.system.hud;

import dexum.client.system.events.EventBus;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.Render2DEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Central manager for all HUD elements.
 * Handles rendering, event dispatch, and element registry.
 */
public class HudElementManager {
    private static final List<AbstractHudElement> elements = new ArrayList<>();
    private static boolean editModeActive = false;
    private static boolean registered = false;

    /**
     * Register the manager with the event bus (call once on startup).
     */
    public static void initialize() {
        if (!registered) {
            EventBus.register(new Manager());
            registered = true;
        }
    }

    /**
     * Register a HUD element.
     */
    public static void register(AbstractHudElement element) {
        if (!elements.contains(element)) {
            elements.add(element);
        }
    }

    /**
     * Unregister a HUD element.
     */
    public static void unregister(AbstractHudElement element) {
        elements.remove(element);
    }

    /**
     * Get all registered HUD elements.
     */
    public static List<AbstractHudElement> getElements() {
        return new ArrayList<>(elements);
    }

    /**
     * Check if edit mode is active.
     */
    public static boolean isEditModeActive() {
        return editModeActive;
    }

    /**
     * Set edit mode active state.
     */
    public static void setEditModeActive(boolean active) {
        editModeActive = active;
        if (active) {
            for (AbstractHudElement element : elements) {
                element.startAnimation();
            }
        }
    }

    /**
     * Internal event handler class.
     */
    private static class Manager {
        @EventHandler
        public void onRender2D(Render2DEvent event) {
            GuiGraphicsExtractor context = event.getContext();
            if (context == null) return;

            // Update all elements
            for (AbstractHudElement element : elements) {
                if (!element.isEnabled()) continue;
                element.tick();
            }

            // Handle dragging for all elements
            long win = net.minecraft.client.Minecraft.getInstance().getWindow().handle();
            int sw = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int sh = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight();

            double[] rx = new double[1], ry = new double[1];
            GLFW.glfwGetCursorPos(win, rx, ry);
            double mouseX = (rx[0] * sw / net.minecraft.client.Minecraft.getInstance().getWindow().getScreenWidth());
            double mouseY = (ry[0] * sh / net.minecraft.client.Minecraft.getInstance().getWindow().getScreenHeight());

            for (AbstractHudElement element : elements) {
                if (!element.isEnabled()) continue;
                element.handleDragInEditMode(mouseX, mouseY, editModeActive);
            }

            // Render all elements
            for (AbstractHudElement element : elements) {
                if (!element.isEnabled() || !element.isVisible()) continue;

                float alpha = element.getAnimationValue();
                if (alpha <= 0.01f) continue;

                element.drawElement(context, alpha);
            }

            // Draw debug overlay in edit mode
            if (editModeActive) {
                drawEditModeOverlay(context);
            }
        }

        private void drawEditModeOverlay(GuiGraphicsExtractor context) {
            // Could draw grab handles or borders around HUD elements in edit mode
            // This is optional - can be left empty for now
        }
    }
}
