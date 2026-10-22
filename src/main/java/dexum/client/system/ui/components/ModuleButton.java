package dexum.client.system.ui.components;

import dexum.client.system.api.Function;
import dexum.client.system.settings.Setting;
import dexum.client.system.ui.RenderHelper;
import dexum.client.system.ui.components.settings.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton {
    private final Function function;
    @Getter private boolean expanded = false;
    @Getter @Setter private boolean visible = true;
    private final int width;
    private final int moduleHeight = 16;

    private boolean binding = false;
    private float enabledAnim = 0f;
    private float expandAnim = 0f;
    private float animSettingsHeight = -1f;

    private static final float EXPAND_SPEED = 14f;
    private static final float ENABLE_SPEED = 10f;

    private final List<SettingComponent> settingComponents = new ArrayList<>();

    private static final int BG_OFF          = 0xFF0d0d0d;
    private static final int BG_OFF_HOVER    = 0xFF151515;
    private static final int BG_ON           = 0xFF1a2533;
    private static final int BG_ON_HOVER     = 0xFF1f2d3d;
    private static final int BINDING_COLOR   = 0xFF332200;
    private static final int TEXT_ON         = 0xFFFFFFFF;
    private static final int TEXT_OFF        = 0xFF888888;
    private static final int BIND_COLOR      = 0xFFaaaaaa;
    private static final int SETTINGS_BG     = 0xFF0a0a0a;

    public ModuleButton(Function function, int x, int y, int width) {
        this.function = function;
        this.width = width;
        this.enabledAnim = function.isEnabled() ? 1f : 0f;
        initSettings();
    }

    private void initSettings() {
        settingComponents.clear();
        for (Setting setting : function.getSettings()) {
            SettingComponent c = SettingComponentFactory.create(setting, width);
            if (c != null) settingComponents.add(c);
        }
    }

    public String getFunctionName() { return function.getName(); }

    private int getSettingsFullHeight() {
        int h = 0;
        for (SettingComponent c : settingComponents) {
            if (c.isVisible()) h += c.getHeight() + 1;
        }
        return Math.max(0, h - 1);
    }

    public void render(GuiGraphicsExtractor context, int x, int y, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);
        boolean enabled = function.isEnabled();
        Minecraft mc = Minecraft.getInstance();

        float enableTarget = enabled ? 1f : 0f;
        float enableDiff = enableTarget - enabledAnim;
        enabledAnim = Math.abs(enableDiff) < 0.01f ? enableTarget : enabledAnim + enableDiff * Math.min(1f, ENABLE_SPEED * delta);

        float expandTarget = expanded ? 1f : 0f;
        float expandDiff = expandTarget - expandAnim;
        expandAnim = Math.abs(expandDiff) < 0.005f ? expandTarget : expandAnim + expandDiff * Math.min(1f, EXPAND_SPEED * delta);

        int fullH = getSettingsFullHeight();
        if (animSettingsHeight < 0f) animSettingsHeight = expanded ? fullH : 0f;
        float targetSettingsH = expanded ? fullH : 0f;
        float hDiff = targetSettingsH - animSettingsHeight;
        animSettingsHeight = Math.abs(hDiff) < 0.5f ? targetSettingsH : animSettingsHeight + hDiff * Math.min(1f, EXPAND_SPEED * delta);

        int visH = Math.round(animSettingsHeight);

        int bgColor;
        if (binding) {
            bgColor = BINDING_COLOR;
        } else {
            bgColor = lerpColor(hovered ? BG_OFF_HOVER : BG_OFF, hovered ? BG_ON_HOVER : BG_ON, enabledAnim);
        }

        if (visH > 0) {
            RenderHelper.fillRoundRectTop(context, x + 3, y, width - 6, moduleHeight, 5, bgColor);
        } else {
            RenderHelper.fillRoundRect(context, x + 3, y, width - 6, moduleHeight, 5, bgColor);
        }

        if (enabledAnim > 0.01f) {
            int indicatorAlpha = (int)(enabledAnim * 255);
            RenderHelper.fillRoundRect(context, x + 5, y + 3, 2, moduleHeight - 6, 1, (indicatorAlpha << 24) | 0x4a9eff);
        }

        int textColor = lerpColor(TEXT_OFF, TEXT_ON, enabledAnim);
        int nameMaxWidth = width - 11 - 18;
        String nameText = mc.font.width(function.getName()) > nameMaxWidth
                ? mc.font.plainSubstrByWidth(function.getName(), nameMaxWidth - mc.font.width("…")) + "…"
                : function.getName();
        context.text(mc.font, nameText, x + 11, y + 5, textColor);

        if (binding) {
            context.text(mc.font, "...", x + width - 16, y + 5, 0xFF4a9eff);
        } else {
            int keybind = function.getKeybind();
            if (keybind != 0 && keybind != GLFW.GLFW_KEY_UNKNOWN) {
                String keyName = getKeyName(keybind);
                int kw = mc.font.width(keyName);
                context.text(mc.font, keyName, x + width - kw - 6, y + 5, BIND_COLOR);
            } else if (!settingComponents.isEmpty()) {
                int arrowAlphaOpen   = (int)(expandAnim * 255);
                int arrowAlphaClosed = (int)((1f - expandAnim) * 255);
                if (arrowAlphaOpen > 0)
                    context.text(mc.font, "v", x + width - 14, y + 5, (arrowAlphaOpen << 24) | (textColor & 0x00FFFFFF));
                if (arrowAlphaClosed > 0)
                    context.text(mc.font, ">", x + width - 14, y + 5, (arrowAlphaClosed << 24) | (textColor & 0x00FFFFFF));
            }
        }

        if (visH > 0 && !settingComponents.isEmpty()) {
            RenderHelper.fillRoundRectBottom(context, x + 3, y + moduleHeight, width - 6, visH, 5, SETTINGS_BG);
            context.fill(x + 5, y + moduleHeight, x + 7, y + moduleHeight + visH, 0x224a9eff);

            int settingY = y + moduleHeight;
            for (SettingComponent component : settingComponents) {
                if (!component.isVisible()) continue;
                int ch = component.getHeight();
                if (settingY < y + moduleHeight + visH) {
                    component.render(context, x + 10, settingY, mouseX, mouseY, delta);

                    float pos = fullH > 0 ? (float)(settingY - (y + moduleHeight)) / fullH : 1f;
                    float alpha = fullH > 0
                            ? Math.max(0f, Math.min(1f, (expandAnim - pos) / (1f / Math.max(1, settingComponents.size()))))
                            : expandAnim;
                    int maskAlpha = (int)((1f - alpha) * 220);
                    if (maskAlpha > 0)
                        context.fill(x + 5, settingY, x + width - 5, settingY + ch, maskAlpha << 24);
                }
                settingY += ch + 1;
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int panelX, int panelY) {
        boolean hovered = isHovered((int) mouseX, (int) mouseY, panelX, panelY);
        if (hovered) {
            if (button == 0) { if (binding) { binding = false; return true; } function.toggle(); return true; }
            if (button == 1 && !settingComponents.isEmpty()) { expanded = !expanded; return true; }
            if (button == 2) { binding = true; return true; }
        }
        if (animSettingsHeight > 1f && !binding) {
            int settingY = panelY + moduleHeight;
            for (SettingComponent component : settingComponents) {
                if (!component.isVisible()) continue;
                if (settingY >= panelY + moduleHeight + Math.round(animSettingsHeight)) break;
                if (component.mouseClicked(mouseX, mouseY, button, panelX + 10, settingY)) return true;
                settingY += component.getHeight() + 1;
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button, int panelX, int panelY) {
        if (animSettingsHeight > 1f) {
            int settingY = panelY + moduleHeight;
            for (SettingComponent component : settingComponents) {
                if (!component.isVisible()) continue;
                if (settingY >= panelY + moduleHeight + Math.round(animSettingsHeight)) break;
                if (component.mouseReleased(mouseX, mouseY, button, panelX + 10, settingY)) return true;
                settingY += component.getHeight() + 1;
            }
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, int panelX, int panelY) {
        if (animSettingsHeight > 1f) {
            int settingY = panelY + moduleHeight;
            for (SettingComponent component : settingComponents) {
                if (!component.isVisible()) continue;
                if (settingY >= panelY + moduleHeight + Math.round(animSettingsHeight)) break;
                if (component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY, panelX + 10, settingY)) return true;
                settingY += component.getHeight() + 1;
            }
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) binding = false;
            else if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) { function.setKeybind(GLFW.GLFW_KEY_UNKNOWN); binding = false; }
            else { function.setKeybind(keyCode); binding = false; }
            return true;
        }
        if (expanded) {
            for (SettingComponent c : settingComponents) {
                if (c.isVisible() && c.keyPressed(keyCode, scanCode, modifiers)) return true;
            }
        }
        return false;
    }

    public int getTotalHeight() {
        int visH = animSettingsHeight < 0f ? (expanded ? getSettingsFullHeight() : 0) : Math.round(animSettingsHeight);
        return moduleHeight + visH;
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x + 3 && mouseX <= x + width - 3 && mouseY >= y && mouseY <= y + moduleHeight;
    }

    static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >> 24) & 0xFF, ra = (a >> 16) & 0xFF, ga = (a >> 8) & 0xFF, ba2 = a & 0xFF;
        int ab = (b >> 24) & 0xFF, rb = (b >> 16) & 0xFF, gb = (b >> 8) & 0xFF, bb2 = b & 0xFF;
        return ((aa + (int)((ab - aa) * t)) << 24) | ((ra + (int)((rb - ra) * t)) << 16)
             | ((ga + (int)((gb - ga) * t)) << 8) | (ba2 + (int)((bb2 - ba2) * t));
    }

    public static String getKeyName(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> "SPC"; case GLFW.GLFW_KEY_ENTER -> "ENT";
            case GLFW.GLFW_KEY_TAB -> "TAB"; case GLFW.GLFW_KEY_BACKSPACE -> "BS";
            case GLFW.GLFW_KEY_DELETE -> "DEL"; case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSH";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSH"; case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCT";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCT"; case GLFW.GLFW_KEY_LEFT_ALT -> "LAL";
            case GLFW.GLFW_KEY_F1 -> "F1"; case GLFW.GLFW_KEY_F2 -> "F2";
            case GLFW.GLFW_KEY_F3 -> "F3"; case GLFW.GLFW_KEY_F4 -> "F4";
            case GLFW.GLFW_KEY_F5 -> "F5"; case GLFW.GLFW_KEY_F6 -> "F6";
            case GLFW.GLFW_KEY_F7 -> "F7"; case GLFW.GLFW_KEY_F8 -> "F8";
            case GLFW.GLFW_KEY_F9 -> "F9"; case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11"; case GLFW.GLFW_KEY_F12 -> "F12";
            default -> { String n = GLFW.glfwGetKeyName(keyCode, 0); yield (n != null && !n.isBlank()) ? n.toUpperCase() : "?"; }
        };
    }
}
