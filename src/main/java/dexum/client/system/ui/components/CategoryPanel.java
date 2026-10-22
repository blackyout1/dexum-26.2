package dexum.client.system.ui.components;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.FunctionManager;
import dexum.client.system.ui.RenderHelper;
import dexum.client.utils.render.scissor.Scissor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {
    private final Category category;
    @Getter @Setter private int x, y;
    @Getter private final int width;
    private final int maxHeight;
    @Getter private boolean expanded = true;

    private final List<ModuleButton> moduleButtons = new ArrayList<>();
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private float animHeight = -1f;
    private static final float ANIM_SPEED = 12f;

    private String searchQuery = "";

    private static final int HEADER_COLOR       = 0xFF1a1a1a;
    private static final int HEADER_HOVER_COLOR = 0xFF252525;
    private static final int PANEL_COLOR        = 0xFF0f0f0f;
    private static final int ACCENT_COLOR       = 0xFF5555ff;

    public CategoryPanel(Category category, int x, int y, int width, int maxHeight) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.maxHeight = maxHeight;
        initModules();
    }

    private void initModules() {
        List<Function> functions = FunctionManager.getFunctionsByCategory(category);
        for (Function function : functions) {
            moduleButtons.add(new ModuleButton(function, 0, 0, width));
        }
        updateMaxScroll();
    }

    private void updateMaxScroll() {
        int total = getVisibleContentHeight();
        maxScroll = Math.max(0, total - (maxHeight - 20));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int headerHeight = 18;

        if (animHeight < 0f) {
            animHeight = expanded ? Math.min(getVisibleContentHeight(), maxHeight - headerHeight) : 0f;
        }

        updateMaxScroll();
        float targetHeight = expanded
                ? Math.min(getVisibleContentHeight(), maxHeight - headerHeight)
                : 0f;

        float diff = targetHeight - animHeight;
        if (Math.abs(diff) < 0.5f) {
            animHeight = targetHeight;
        } else {
            animHeight += diff * Math.min(1f, ANIM_SPEED * delta);
        }

        int contentHeight = Math.round(animHeight);

        int headerColor = isHeaderHovered(mouseX, mouseY) ? HEADER_HOVER_COLOR : HEADER_COLOR;
        if (contentHeight > 0) {
            RenderHelper.fillRoundRectTop(context, x, y, width, headerHeight, 4, headerColor);
        } else {
            RenderHelper.fillRoundRect(context, x, y, width, headerHeight, 4, headerColor);
        }
        context.fill(x, y, x + width, y + 2, ACCENT_COLOR);

        String categoryText = category.getName();
        Minecraft mc = Minecraft.getInstance();
        int textWidth = mc.font.width(categoryText);
        context.text(mc.font, categoryText,
                x + (width - textWidth) / 2, y + 6, 0xFFFFFFFF);

        if (contentHeight > 0) {
            RenderHelper.fillRoundRectBottom(context, x, y + headerHeight, width, contentHeight, 4, PANEL_COLOR);

            Scissor.enableScissor(x, y + headerHeight, x + width, y + headerHeight + contentHeight);

            int currentY = y + headerHeight - scrollOffset;
            for (ModuleButton button : moduleButtons) {
                if (!button.isVisible()) continue;
                int bh = button.getTotalHeight();
                if (currentY + bh > y + headerHeight && currentY < y + headerHeight + contentHeight) {
                    button.render(context, x, currentY, mouseX, mouseY, delta);
                }
                currentY += bh;
            }

            Scissor.disableScissor();

            if (maxScroll > 0) {
                drawScrollbar(context, contentHeight);
            }
        }
    }

    private void drawScrollbar(GuiGraphicsExtractor context, int contentHeight) {
        int totalContent = getVisibleContentHeight();
        if (totalContent == 0) return;
        int thumbHeight = Math.max(12, contentHeight * contentHeight / totalContent);
        int scrollRange = contentHeight - thumbHeight;
        int thumbY = y + 18 + (maxScroll > 0 ? (int) ((float) scrollOffset / maxScroll * scrollRange) : 0);
        context.fill(x + width - 3, thumbY, x + width - 1, thumbY + thumbHeight, 0x885555ff);
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query.toLowerCase().trim();
        applyFilter();
        updateMaxScroll();
        scrollOffset = 0;
        if (!this.searchQuery.isEmpty()) {
            boolean hasVisible = moduleButtons.stream().anyMatch(ModuleButton::isVisible);
            if (hasVisible) expanded = true;
        }
    }

    private void applyFilter() {
        for (ModuleButton button : moduleButtons) {
            if (searchQuery.isEmpty()) {
                button.setVisible(true);
            } else {
                button.setVisible(button.getFunctionName().toLowerCase().contains(searchQuery));
            }
        }
    }

    public boolean hasVisibleModules() {
        return moduleButtons.stream().anyMatch(ModuleButton::isVisible);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHeaderHovered((int) mouseX, (int) mouseY)) {
            if (button == 0) { expanded = !expanded; return true; }
            return false;
        }
        if (expanded && isInBounds((int) mouseX, (int) mouseY)) {
            int currentY = y + 18 - scrollOffset;
            for (ModuleButton moduleButton : moduleButtons) {
                if (!moduleButton.isVisible()) continue;
                if (moduleButton.mouseClicked(mouseX, mouseY, button, x, currentY)) {
                    updateMaxScroll();
                    return true;
                }
                currentY += moduleButton.getTotalHeight();
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (expanded) {
            int currentY = y + 18 - scrollOffset;
            for (ModuleButton moduleButton : moduleButtons) {
                if (!moduleButton.isVisible()) continue;
                if (moduleButton.mouseReleased(mouseX, mouseY, button, x, currentY)) return true;
                currentY += moduleButton.getTotalHeight();
            }
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (expanded) {
            int currentY = y + 18 - scrollOffset;
            for (ModuleButton moduleButton : moduleButtons) {
                if (!moduleButton.isVisible()) continue;
                if (moduleButton.mouseDragged(mouseX, mouseY, button, deltaX, deltaY, x, currentY)) return true;
                currentY += moduleButton.getTotalHeight();
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (expanded && isInBounds((int) mouseX, (int) mouseY)) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (amount * 10)));
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (expanded) {
            for (ModuleButton button : moduleButtons) {
                if (!button.isVisible()) continue;
                if (button.keyPressed(keyCode, scanCode, modifiers)) return true;
            }
        }
        return false;
    }

    public boolean isHeaderHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 18;
    }

    public String getCategoryName() { return category.getName(); }

    private boolean isInBounds(int mouseX, int mouseY) {
        int contentHeight = Math.round(animHeight < 0 ? 0 : animHeight);
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y + 18 && mouseY <= y + 18 + contentHeight;
    }

    private int getVisibleContentHeight() {
        int total = 0;
        for (ModuleButton button : moduleButtons) {
            if (button.isVisible()) total += button.getTotalHeight();
        }
        return total;
    }
}
