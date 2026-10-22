package dexum.client.system.menu;

import dexum.client.utils.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class ClickGui extends Screen {
    public ClickGui() {
        super(Component.literal("ClickGui"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        RenderUtil.rect((RenderUtil.getFixedScaledWidth() - 400) / 2f, (RenderUtil.getFixedScaledHeight() - 300) / 2f, 400, 300, 10, new Color(12, 12, 12));
        RenderUtil.outline((RenderUtil.getFixedScaledWidth() - 400) / 2f, (RenderUtil.getFixedScaledHeight() - 300) / 2f, 400, 300, 10, 1f, new Color(25, 25, 25));
        super.extractRenderState(context, mouseX, mouseY, deltaTicks);
    }

    public void renderBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {}
}
