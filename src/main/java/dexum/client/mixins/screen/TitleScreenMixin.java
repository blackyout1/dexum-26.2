package dexum.client.mixins.screen;

import dexum.client.system.menu.CustomMainMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true, require = 0)
    private void replaceWithCustomMenu(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client != null) {
            client.gui.setScreen(new CustomMainMenu());
            ci.cancel();
        }
    }
}
