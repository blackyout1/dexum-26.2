package dexum.client.mixins.input;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dexum.client.system.events.EventBus;
import dexum.client.system.events.impl.LookEvent;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseMixin {

    @WrapWithCondition(
            method = "turnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"),
            require = 0, allow = 1)
    private boolean onChangeLookDirection(LocalPlayer instance, double deltaX, double deltaY) {
        LookEvent event = new LookEvent(deltaX, deltaY);
        EventBus.post(event);
        if (event.isCancelled()) return false;
        instance.turn(event.getYaw(), event.getPitch());
        return false;
    }
}
