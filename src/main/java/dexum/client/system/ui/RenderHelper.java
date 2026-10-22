package dexum.client.system.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class RenderHelper {

    public static void fillRoundRect(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int r, int color) {
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

    public static void outlineRoundRect(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int r, float thickness, int color) {
        if (r <= 0) {
            ctx.fill(x, y, x + w, y + h, color);
            return;
        }
        r = Math.min(r, Math.min(w, h) / 2);
        
        // Горизонтальные линии
        ctx.fill(x + r, y, x + w - r, y + (int)thickness, color);
        ctx.fill(x + r, y + h - (int)thickness, x + w - r, y + h, color);
        
        // Вертикальные линии
        ctx.fill(x, y + r, x + (int)thickness, y + h - r, color);
        ctx.fill(x + w - (int)thickness, y + r, x + w, y + h - r, color);
        
        // Углы
        for (int i = 0; i < r; i++) {
            int dx = (int) Math.round(r - Math.sqrt((double) r * r - (double)(r - i) * (r - i)));
            
            // Верхний левый
            ctx.fill(x + r - dx, y + i, x + r - dx + (int)thickness, y + i + 1, color);
            ctx.fill(x + i, y + r - dx, x + i + 1, y + r - dx + (int)thickness, color);
            
            // Верхний правый
            ctx.fill(x + w - r + dx - (int)thickness, y + i, x + w - r + dx, y + i + 1, color);
            ctx.fill(x + w - r + i, y + r - dx, x + w - r + i + 1, y + r - dx + (int)thickness, color);
            
            // Нижний левый
            ctx.fill(x + r - dx, y + h - r + i, x + r - dx + (int)thickness, y + h - r + i + 1, color);
            ctx.fill(x + i, y + h - r + dx - (int)thickness, x + i + 1, y + h - r + dx, color);
            
            // Нижний правый
            ctx.fill(x + w - r + dx - (int)thickness, y + h - r + i, x + w - r + dx, y + h - r + i + 1, color);
            ctx.fill(x + w - r + i, y + h - r + dx - (int)thickness, x + w - r + i + 1, y + h - r + dx, color);
        }
    }

    public static void fillRoundRectTop(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int r, int color) {
        if (r <= 0) { ctx.fill(x, y, x + w, y + h, color); return; }
        r = Math.min(r, Math.min(w, h) / 2);
        ctx.fill(x + r, y, x + w - r, y + h, color);
        ctx.fill(x, y + r, x + r, y + h, color);
        ctx.fill(x + w - r, y + r, x + w, y + h, color);
        for (int i = 0; i < r; i++) {
            int dx = (int) Math.round(r - Math.sqrt((double) r * r - (double)(r - i) * (r - i)));
            ctx.fill(x + dx, y + i, x + r, y + i + 1, color);
            ctx.fill(x + w - r, y + i, x + w - dx, y + i + 1, color);
        }
    }

    public static void fillRoundRectBottom(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int r, int color) {
        if (r <= 0) { ctx.fill(x, y, x + w, y + h, color); return; }
        r = Math.min(r, Math.min(w, h) / 2);
        ctx.fill(x + r, y, x + w - r, y + h, color);
        ctx.fill(x, y, x + r, y + h - r, color);
        ctx.fill(x + w - r, y, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            int dx = (int) Math.round(r - Math.sqrt((double) r * r - (double)(r - i) * (r - i)));
            ctx.fill(x + dx, y + h - i - 1, x + r, y + h - i, color);
            ctx.fill(x + w - r, y + h - i - 1, x + w - dx, y + h - i, color);
        }
    }
}
