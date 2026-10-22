package dexum.client.system.ui;

import dexum.client.system.api.Category;
import dexum.client.system.config.ConfigManager;
import dexum.client.system.ui.components.CategoryPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private final List<CategoryPanel> panels = new ArrayList<>();

    private CategoryPanel draggingPanel = null;
    private int dragOffsetX = 0, dragOffsetY = 0;
    private static final int DRAG_THRESHOLD = 4;
    private CategoryPanel pendingDragPanel = null;
    private int pendingDragOffsetX = 0, pendingDragOffsetY = 0;
    private int pendingDragStartX = 0, pendingDragStartY = 0;
    private boolean dragConfirmed = false;

    private final StringBuilder searchQuery = new StringBuilder();

    private float islandWidth  = 28f;
    private float islandHeight = 8f;
    private static final float ISLAND_IDLE_W   = 28f;
    private static final float ISLAND_IDLE_H   = 8f;
    private static final float ISLAND_ACTIVE_W = 160f;
    private static final float ISLAND_ACTIVE_H = 22f;
    private static final float ISLAND_SPEED    = 14f;
    private static final int   ISLAND_Y        = 6;
    
    private float guiOpenAnimation = 0f;
    private static final float GUI_OPEN_SPEED = 10f;

    public ClickGuiScreen() {
        super(Component.literal("Dexum GUI"));
    }

    @Override
    protected void init() {
        super.init();
        searchQuery.setLength(0);
        islandWidth  = ISLAND_IDLE_W;
        islandHeight = ISLAND_IDLE_H;
        initPanels();
        guiOpenAnimation = 0f;
    }

    private void initPanels() {
        panels.clear();
        int x = 20, y = 20, spacing = 10;
        for (Category category : Category.values()) {
            if (category == Category.CONFIG) continue;
            PanelPositionManager.PanelPos saved = PanelPositionManager.get(category.getName());
            int panelX = saved != null ? saved.x() : x;
            int panelY = saved != null ? saved.y() : y;
            panelX = Math.max(0, Math.min(panelX, width  - 120));
            panelY = Math.max(0, Math.min(panelY, height - 18));
            CategoryPanel panel = new CategoryPanel(category, panelX, panelY, 120, 300);
            panels.add(panel);
            if (saved == null) {
                x += 120 + spacing;
                if (panels.size() % 5 == 0) { x = 20; y += 310 + spacing; }
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // Анимация открытия GUI
        float targetAnimation = 1f;
        float diff = targetAnimation - guiOpenAnimation;
        if (Math.abs(diff) > 0.01f) {
            guiOpenAnimation += diff * Math.min(1f, GUI_OPEN_SPEED * delta);
        } else {
            guiOpenAnimation = targetAnimation;
        }
        
        dexum.client.utils.render.RenderUtil.setContext(context);
        
        // Рендер полупрозрачного фона
        int bgAlpha = (int)(guiOpenAnimation * 180);
        context.fill(0, 0, width, height, (bgAlpha << 24) | 0x0a0b0d);
        
        // Рендер панелей
        for (CategoryPanel panel : panels) {
            if (!searchQuery.isEmpty() && !panel.hasVisibleModules()) continue;
            panel.render(context, mouseX, mouseY, delta);
        }
        renderDynamicIsland(context, delta);
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    private void renderDynamicIsland(GuiGraphicsExtractor context, float delta) {
        boolean active = searchQuery.length() > 0;
        float targetW = active ? ISLAND_ACTIVE_W : ISLAND_IDLE_W;
        float targetH = active ? ISLAND_ACTIVE_H : ISLAND_IDLE_H;

        float t = Math.min(1f, ISLAND_SPEED * delta);
        islandWidth  += (targetW - islandWidth)  * t;
        islandHeight += (targetH - islandHeight) * t;

        int iw = Math.round(islandWidth);
        int ih = Math.round(islandHeight);
        int ix = (width - iw) / 2;
        int iy = ISLAND_Y;
        int r = ih / 2;

        fillRoundRect(context, ix - 1, iy + 2, iw + 2, ih + 1, r + 1, 0x44000000);
        fillRoundRect(context, ix, iy, iw, ih, r, 0xFF0d0d0d);

        if (active) {
            float lineAlpha = Math.max(0f, (islandWidth - ISLAND_IDLE_W) / (ISLAND_ACTIVE_W - ISLAND_IDLE_W));
            int lineA = (int)(lineAlpha * 255);
            if (lineA > 0) fillRoundRect(context, ix, iy, iw, 2, 1, (lineA << 24) | 0x5555ff);

            float textAlpha = Math.max(0f, (islandWidth - ISLAND_IDLE_W * 2) / (ISLAND_ACTIVE_W - ISLAND_IDLE_W * 2));
            int ta = (int)(textAlpha * 255);
            if (ta > 10) {
                String display = searchQuery + "|";
                int textColor = (ta << 24) | 0xFFFFFF;
                int tw = Minecraft.getInstance().font.width(display);
                context.text(Minecraft.getInstance().font, display,
                        ix + (iw - tw) / 2, iy + (ih - 8) / 2, textColor);
            }
        }

        if (!active) {
            float dotAlpha = Math.max(0f, 1f - (islandWidth - ISLAND_IDLE_W) / 10f);
            int da = (int)(dotAlpha * 180);
            if (da > 0) {
                int dotSize = 4;
                context.fill(ix + (iw - dotSize) / 2, iy + (ih - dotSize) / 2,
                        ix + (iw + dotSize) / 2, iy + (ih + dotSize) / 2,
                        (da << 24) | 0x5555ff);
            }
        }
    }

    private static void fillRoundRect(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int r, int color) {
        if (r <= 0) { ctx.fill(x, y, x + w, y + h, color); return; }
        r = Math.min(r, Math.min(w, h) / 2);
        ctx.fill(x + r, y, x + w - r, y + h, color);
        ctx.fill(x, y + r, x + r, y + h - r, color);
        ctx.fill(x + w - r, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            int dx = (int) Math.round(r - Math.sqrt((double) r * r - (double)(r - i) * (r - i)));
            ctx.fill(x + dx, y + i, x + r, y + i + 1, color);
            ctx.fill(x + w - r, y + i, x + w - dx, y + i + 1, color);
            ctx.fill(x + dx, y + h - i - 1, x + r, y + h - i, color);
            ctx.fill(x + w - r, y + h - i - 1, x + w - dx, y + h - i, color);
        }
    }

    // ── Input ──────────────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && searchQuery.length() > 0) {
            searchQuery.deleteCharAt(searchQuery.length() - 1);
            propagateSearch();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (searchQuery.length() > 0) {
                searchQuery.setLength(0);
                propagateSearch();
                return true;
            }
            onClose();
            return true;
        }
        for (CategoryPanel panel : panels) {
            if (panel.keyPressed(keyCode, 0, 0)) return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        int chr = event.codepoint();
        if (chr >= 32 && chr != 127) {
            searchQuery.appendCodePoint(chr);
            propagateSearch();
            return true;
        }
        return super.charTyped(event);
    }

    private void propagateSearch() {
        String q = searchQuery.toString();
        for (CategoryPanel panel : panels) panel.setSearchQuery(q);
    }

    // ── Mouse ──────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            for (CategoryPanel panel : panels) {
                if (!searchQuery.isEmpty() && !panel.hasVisibleModules()) continue;
                if (panel.isHeaderHovered((int) mouseX, (int) mouseY)) {
                    pendingDragPanel   = panel;
                    pendingDragOffsetX = (int)(mouseX - panel.getX());
                    pendingDragOffsetY = (int)(mouseY - panel.getY());
                    pendingDragStartX  = (int) mouseX;
                    pendingDragStartY  = (int) mouseY;
                    dragConfirmed = false;
                    return true;
                }
            }
        }
        for (CategoryPanel panel : panels) {
            if (!searchQuery.isEmpty() && !panel.hasVisibleModules()) continue;
            if (panel.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (pendingDragPanel != null) {
                if (!dragConfirmed) pendingDragPanel.mouseClicked(mouseX, mouseY, button);
                draggingPanel = null; pendingDragPanel = null; dragConfirmed = false;
            } else if (draggingPanel != null) {
                draggingPanel = null;
            }
        }
        for (CategoryPanel panel : panels) panel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (pendingDragPanel != null && !dragConfirmed) {
                int dx = (int) mouseX - pendingDragStartX;
                int dy = (int) mouseY - pendingDragStartY;
                if (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD) {
                    dragConfirmed  = true;
                    draggingPanel  = pendingDragPanel;
                    dragOffsetX    = pendingDragOffsetX;
                    dragOffsetY    = pendingDragOffsetY;
                    pendingDragPanel = null;
                } else return true;
            }
            if (draggingPanel != null) {
                int newX = Math.max(0, Math.min((int) mouseX - dragOffsetX, width - draggingPanel.getWidth()));
                int newY = Math.max(0, (int) mouseY - dragOffsetY);
                draggingPanel.setX(newX);
                draggingPanel.setY(newY);
                return true;
            }
        }
        for (CategoryPanel panel : panels) {
            if (panel.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CategoryPanel panel : panels) {
            if (!searchQuery.isEmpty() && !panel.hasVisibleModules()) continue;
            if (panel.mouseScrolled(mouseX, mouseY, verticalAmount)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────

    @Override
    public void onClose() {
        for (CategoryPanel panel : panels) {
            PanelPositionManager.set(panel.getCategoryName(), panel.getX(), panel.getY());
        }
        ConfigManager.shutdown();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    public void renderBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {}
}
