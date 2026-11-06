package dexum.client.system.hud;

import dexum.client.utils.animation.AnimationHandler;
import dexum.client.utils.animation.Direction;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

/**
 * Base class for all HUD elements.
 * Provides dragging, positioning, and basic animation support.
 */
@Getter
@Setter
public abstract class AbstractHudElement {
    protected final Minecraft mc = Minecraft.getInstance();

    protected String name;
    protected float x, y;
    protected float width, height;
    protected boolean enabled = true;
    protected boolean dragging = false;
    protected float dragOffX, dragOffY;
    protected boolean wasMouseDown = false;

    protected AnimationHandler scaleAnimation;
    protected long lastUpdateTime = System.currentTimeMillis();
    protected final float lastTickDelta = 1.0f;

    public AbstractHudElement(String name, float x, float y, float width, float height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scaleAnimation = new AnimationHandler(0.0f, 0.15f);
    }

    public abstract void drawElement(GuiGraphicsExtractor context, float alpha);

    /** Логический тик. По умолчанию ничего не делает — анимацию двигает {@link #getAnimationValue()}. */
    public void tick() {
    }

    public boolean isVisible() {
        return true;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public void handleDragInEditMode(double mouseX, double mouseY, boolean editModeActive) {
        if (!editModeActive) {
            dragging = false;
            return;
        }

        long win = mc.getWindow().handle();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        double[] rx = new double[1], ry = new double[1];
        GLFW.glfwGetCursorPos(win, rx, ry);
        float mx = (float)(rx[0] * sw / mc.getWindow().getScreenWidth());
        float my = (float)(ry[0] * sh / mc.getWindow().getScreenHeight());

        boolean lmb = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean over = mx >= x && mx <= x + width && my >= y && my <= y + height;

        if (lmb && !wasMouseDown && over) {
            dragging = true;
            dragOffX = mx - x;
            dragOffY = my - y;
        }

        if (!lmb) {
            dragging = false;
        }

        if (dragging) {
            x = mx - dragOffX;
            y = my - dragOffY;
        }

        wasMouseDown = lmb;

        x = Math.max(0, Math.min(sw - width, x));
        y = Math.max(0, Math.min(sh - height, y));
    }

    public void startAnimation() {
        scaleAnimation.start(Direction.FORWARDS);
    }

    public void stopAnimation() {
        scaleAnimation.start(Direction.BACKWARDS);
    }

    protected boolean isChat(Object screen) {
        return screen instanceof net.minecraft.client.gui.screens.ChatScreen;
    }

    /**
     * Возвращает текущий alpha (0..1) и двигает анимацию.
     * Каждый вызов в кадре — тик; рендер-луп дергает это как раз раз в кадр.
     */
    public float getAnimationValue() {
        if (scaleAnimation == null) return 0.0f;
        Float v = scaleAnimation.getOutput();
        return v == null ? 0.0f : v;
    }
}