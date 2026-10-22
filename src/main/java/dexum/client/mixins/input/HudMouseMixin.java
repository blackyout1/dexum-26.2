package dexum.client.mixins.input;

import dexum.client.system.events.EventBus;
import dexum.client.system.events.impl.MouseEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class HudMouseMixin {
    @Shadow private double xpos;
    @Shadow private double ypos;

    @Inject(method = "onPress", at = @At("HEAD"), require = 0)
    private void onButton(long window, int button, int action, int modifiers, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() != null) return;
        double sx = this.xpos * mc.getWindow().getGuiScaledWidth()  / mc.getWindow().getScreenWidth();
        double sy = this.ypos * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
        EventBus.post(new MouseEvent(sx, sy, button, action));
    }

    @Inject(method = "onMove", at = @At("HEAD"), require = 0)
    private void onCursor(long window, double x, double y, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() != null) return;
        double sx = x * mc.getWindow().getGuiScaledWidth()  / mc.getWindow().getScreenWidth();
        double sy = y * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
        EventBus.post(new MouseEvent(sx, sy, -1, -1));
    }
}
