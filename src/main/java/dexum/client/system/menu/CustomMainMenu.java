package dexum.client.system.menu;

import dexum.client.utils.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.awt.*;

public class CustomMainMenu extends Screen {

    private static final String[] BUTTON_LABELS = {"Singleplayer", "Multiplayer", "Options", "Quit Game"};
    private static final float BTN_WIDTH  = 180;
    private static final float BTN_HEIGHT = 40;
    private static final float BTN_GAP    = 8;
    private static final float BTN_RADIUS = 8;

    private float modelRotation  = 0f;
    private float rotationVelocity = 0.5f;
    private boolean isDragging   = false;
    private double lastMouseX    = 0;

    private int hoveredButton = -1;
    private final float[] buttonAnimations = new float[BUTTON_LABELS.length];

    public CustomMainMenu() {
        super(Component.literal(""));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        dexum.client.utils.render.RenderUtil.setContext(context);
        RenderUtil.rect(0, 0, this.width, this.height, 0, new Color(13, 13, 21, 255));
        drawGradientOverlay();
        updateModelRotation(delta);
        renderButtons(context, mouseX, mouseY, delta);
        renderLogo();
        renderPlayerName(context);
        String version = "Dexum v1.0";
        context.text(this.font, version, 4, this.height - 10, 0x66FFFFFF);
    }

    private void drawGradientOverlay() {
        RenderUtil.rect(0, 0, this.width / 2f, this.height, 0,
                new Color(108, 92, 231, 30), new Color(108, 92, 231, 0),
                new Color(108, 92, 231, 0), new Color(108, 92, 231, 30));
    }

    private void renderButtons(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        float leftMargin = 60;
        float startY = this.height / 2f - (BUTTON_LABELS.length * (BTN_HEIGHT + BTN_GAP) - BTN_GAP) / 2f;

        hoveredButton = -1;
        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            float by = startY + i * (BTN_HEIGHT + BTN_GAP);
            if (mouseX >= leftMargin && mouseX <= leftMargin + BTN_WIDTH
                    && mouseY >= by && mouseY <= by + BTN_HEIGHT) {
                hoveredButton = i;
                break;
            }
        }

        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            float by = startY + i * (BTN_HEIGHT + BTN_GAP);
            boolean hovered = hoveredButton == i;

            buttonAnimations[i] = hovered
                    ? Math.min(1.0f, buttonAnimations[i] + delta * 5f)
                    : Math.max(0.0f, buttonAnimations[i] - delta * 5f);
            float anim = buttonAnimations[i];

            RenderUtil.shadow(leftMargin, by, BTN_WIDTH, BTN_HEIGHT, BTN_RADIUS, new Color(0, 0, 0, (int)(80 + anim * 50)));
            RenderUtil.blur(leftMargin - 2, by - 2, BTN_WIDTH + 4, BTN_HEIGHT + 4, BTN_RADIUS, 8f);
            RenderUtil.rect(leftMargin, by, BTN_WIDTH, BTN_HEIGHT, BTN_RADIUS, new Color(26, 26, 46, (int)(180 + anim * 75)));
            if (anim > 0.1f)
                RenderUtil.glow(leftMargin, by, BTN_WIDTH, BTN_HEIGHT, 2f, BTN_RADIUS, 10f * anim, anim * 0.7f, 0.6f, new Color(108, 92, 231, 200));
            RenderUtil.outline(leftMargin, by, BTN_WIDTH, BTN_HEIGHT, BTN_RADIUS, 1.5f, new Color(108, 92, 231, (int)(120 + anim * 135)));

            String label = BUTTON_LABELS[i];
            int textWidth = this.font.width(label);
            context.text(this.font, label, (int)(leftMargin + (BTN_WIDTH - textWidth) / 2f), (int)(by + (BTN_HEIGHT - 8) / 2f), 0xFFFFFFFF);
        }
    }

    private void updateModelRotation(float delta) {
        if (!isDragging) {
            modelRotation += rotationVelocity;
            if (Math.abs(rotationVelocity) > 0.5f) rotationVelocity *= 0.98f;
            else rotationVelocity = 0.5f;
        }
        while (modelRotation > 360) modelRotation -= 360;
        while (modelRotation < 0)   modelRotation += 360;
    }

    private void renderLogo() {
        float logoSize = 200;
        float logoX = this.width - logoSize - 180;
        float logoY = this.height / 2f - logoSize / 2f;
        RenderUtil.textureGlow(logoX, logoY, logoSize,
                Identifier.fromNamespaceAndPath("dexum", "textures/logo.png"),
                20f, 10f, 0.8f, Color.WHITE);
    }

    private void renderPlayerName(GuiGraphicsExtractor context) {
        if (this.minecraft == null) return;
        String name = this.minecraft.getUser().getName();
        float nameX = this.width - 180;
        float nameY = this.height / 2f + 90;
        int textWidth = this.font.width(name);
        float cx = nameX - textWidth / 2f;
        context.text(this.font, name, (int)cx + 1, (int)nameY + 1, 0x66000000);
        context.text(this.font, name, (int)cx,     (int)nameY,     0xFF6C5CE7);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == 0) {
            float leftMargin = 60;
            float startY = this.height / 2f - (BUTTON_LABELS.length * (BTN_HEIGHT + BTN_GAP) - BTN_GAP) / 2f;
            for (int i = 0; i < BUTTON_LABELS.length; i++) {
                float by = startY + i * (BTN_HEIGHT + BTN_GAP);
                if (mouseX >= leftMargin && mouseX <= leftMargin + BTN_WIDTH
                        && mouseY >= by && mouseY <= by + BTN_HEIGHT) {
                    onButtonClick(i);
                    return true;
                }
            }
            float modelX = this.width - 180, modelY = this.height / 2f;
            if (Math.abs(mouseX - modelX) < 100 && Math.abs(mouseY - modelY) < 100) {
                isDragging = true;
                lastMouseX = mouseX;
                return true;
            }
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && isDragging) isDragging = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        double mouseX = event.x();
        if (isDragging) {
            rotationVelocity = (float)((mouseX - lastMouseX) * 0.5f);
            modelRotation += rotationVelocity;
            lastMouseX = mouseX;
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    private void onButtonClick(int index) {
        if (this.minecraft == null) return;
        switch (index) {
            case 0 -> this.minecraft.setScreenAndShow(new SelectWorldScreen(this));
            case 1 -> this.minecraft.setScreenAndShow(new JoinMultiplayerScreen(this));
            case 2 -> this.minecraft.setScreenAndShow(new OptionsScreen(this, this.minecraft.options, false));
            case 3 -> this.minecraft.stop();
        }
    }

    @Override public boolean isPauseScreen() { return false; }
    public void renderBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {}
}
