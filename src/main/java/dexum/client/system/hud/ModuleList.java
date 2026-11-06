package dexum.client.system.hud;

import dexum.client.system.api.Function;
import dexum.client.system.api.FunctionManager;
import dexum.client.utils.animation.AnimationHandler;
import dexum.client.utils.animation.Direction;
import dexum.client.utils.render.RenderUtil;
import dexum.client.utils.render.msdf.Msdf;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.*;
import java.util.*;

/**
 * Module list HUD element displaying all enabled modules.
 * Shows active modules in an arraylist-style layout with glassmorphism effects.
 */
public class ModuleList extends AbstractHudElement {
    private static final float PADDING = 4.0f;
    private static final float ROW_HEIGHT = 10.0f;
    private static final float HEADER_HEIGHT = 10.0f;
    private static final float GAP = 4.0f;
    private static final float ANIMATION_SPEED = 6.0f;
    
    private float animatedWidth = 80.0f;
    private float animatedHeight = 20.0f;
    private final Map<String, AnimationHandler> rowAnimations = new LinkedHashMap<>();
    private final java.util.List<String> currentModules = new ArrayList<>();

    public ModuleList() {
        super("Module List", 10, 10, 80, 20);
        startAnimation();
    }

    @Override
    public void tick() {
        // Update current list of enabled modules
        currentModules.clear();
        java.util.List<Function> functions = FunctionManager.getFunctions();
        for (Function func : functions) {
            if (func.isEnabled()) {
                currentModules.add(func.getName());
            }
        }

        // Update animations for each module
        for (String name : currentModules) {
            if (!rowAnimations.containsKey(name)) {
                rowAnimations.put(name, new AnimationHandler(0.0f, 0.15f));
                rowAnimations.get(name).start(Direction.FORWARDS);
            }
        }

        // Animate out removed modules
        java.util.List<String> toRemove = new ArrayList<>();
        for (String name : rowAnimations.keySet()) {
            if (!currentModules.contains(name)) {
                rowAnimations.get(name).start(Direction.BACKWARDS);
                if (rowAnimations.get(name).isFinished(Direction.BACKWARDS)) {
                    toRemove.add(name);
                }
            }
        }
        for (String name : toRemove) {
            rowAnimations.remove(name);
        }
    }

    @Override
    public void drawElement(GuiGraphicsExtractor context, float alpha) {
        if (alpha <= 0.01f) return;

        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastUpdateTime) / 1000.0f, 0.1f);
        lastUpdateTime = currentTime;

        float scale = 1.0f;
        float radius = 5.0f * scale;

        // Calculate dimensions
        float targetWidth = 55.0f * scale;
        float currentOffset = HEADER_HEIGHT + GAP;

        // Check if we have any visible modules
        boolean hasModules = !currentModules.isEmpty();

        // Calculate width based on module names
        for (String name : rowAnimations.keySet()) {
            AnimationHandler anim = rowAnimations.get(name);
            float animValue = anim.getOutput() != null ? anim.getOutput().floatValue() : 0.0f;
            if (animValue > 0.01f) {
                float nameWidth = Msdf.width(Msdf.BOLD, name, 6.0f * scale);
                targetWidth = Math.max(targetWidth, nameWidth + 12.0f * scale);
                currentOffset += (ROW_HEIGHT + GAP);
            }
        }

        float targetHeight = Math.max(20.0f, currentOffset - GAP);

        // Smooth animation
        animatedWidth = lerp(animatedWidth, targetWidth, deltaTime, ANIMATION_SPEED);
        animatedHeight = lerp(animatedHeight, targetHeight, deltaTime, ANIMATION_SPEED);

        width = animatedWidth;
        height = animatedHeight;

        float x = this.x;
        float y = this.y;

        // Draw background with glassmorphism
        int headerColor = new Color(20, 20, 25, (int)(100 * alpha)).getRGB();
        int steps = 6;
        for (int i = 1; i <= steps; i++) {
            float alphaFactor = (float) Math.pow(1f - (i / (float)steps), 1.5f);
            int shadowAlpha = (int)(60 * alphaFactor * alpha);
            float offset = i * 0.75f * scale;
            RenderUtil.outline(x - offset, y - offset, width + offset*2, HEADER_HEIGHT + offset*2, 
                    radius + offset, 0.75f * scale, new Color(0, 0, 0, shadowAlpha));
        }
        
        RenderUtil.glassmorphism(x, y, width, HEADER_HEIGHT, radius, 10.0f * scale, headerColor);

        // Draw header text
        int whiteColor = new Color(255, 255, 255, (int)(255 * alpha)).getRGB();
        float fontSize = 5.0f * scale;
        float textHeight = Msdf.height(Msdf.BOLD, fontSize);
        float headerTextY = y + HEADER_HEIGHT / 2.0f - textHeight / 2.0f;
        
        Msdf.text(context, Msdf.BOLD, "Modules", x + 6.0f * scale, headerTextY, fontSize, whiteColor, false);

        // Draw modules
        float drawOffsetY = y + HEADER_HEIGHT + GAP;
        for (String name : rowAnimations.keySet()) {
            AnimationHandler anim = rowAnimations.get(name);
            float animValue = anim.getOutput() != null ? anim.getOutput().floatValue() : 0.0f;

            if (animValue > 0.01f) {
                int rowBg = new Color(255, 255, 255, (int)(15 * alpha * animValue)).getRGB();
                drawRow(context, name, x, drawOffsetY, animValue, alpha, scale, radius);
                drawOffsetY += ROW_HEIGHT + GAP;
            }
        }
    }

    private void drawRow(GuiGraphicsExtractor context, String name, float x, float y, 
                         float animValue, float alpha, float scale, float radius) {
        int textColor = new Color(255, 255, 255, (int)(255 * alpha * animValue)).getRGB();
        float fontSize = 6.0f * scale;
        float textHeight = Msdf.height(Msdf.BOLD, fontSize);
        float textY = y + ROW_HEIGHT / 2.0f - textHeight / 2.0f;

        int rowBg = new Color(255, 255, 255, (int)(15 * alpha * animValue)).getRGB();
        RenderUtil.glassmorphism(x, y, width, ROW_HEIGHT, radius, 5.0f * scale, rowBg);

        Msdf.text(context, Msdf.BOLD, name, x + 6.0f * scale, textY, fontSize, textColor, false);
    }

    private float lerp(float current, float target, float deltaTime, float speed) {
        float factor = (float) (1.0 - Math.pow(0.001, deltaTime * speed));
        return current + (target - current) * factor;
    }

    @Override
    public boolean isVisible() {
        return !currentModules.isEmpty() || !rowAnimations.isEmpty();
    }
}
