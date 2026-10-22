package dexum.client.mixins.client;

import dexum.client.system.command.CommandManager;
import dexum.client.system.events.EventBus;
import dexum.client.system.events.impl.ChatEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true, require = 0)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        ChatEvent event = new ChatEvent(message);
        EventBus.post(event);
        if (event.isCancelled()) { ci.cancel(); return; }
        if (CommandManager.handleMessage(message)) ci.cancel();
    }
}
