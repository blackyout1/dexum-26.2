package dexum.client.mixins.input;

import dexum.client.system.events.EventBus;
import dexum.client.system.events.impl.KeyEvent;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardInputMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), require = 0)
    private void onKey(long window, int scancode, net.minecraft.client.input.KeyEvent keyEvent, CallbackInfo ci) {
        EventBus.post(new KeyEvent(1, window, keyEvent.key(), scancode, keyEvent.modifiers()));
    }
}
