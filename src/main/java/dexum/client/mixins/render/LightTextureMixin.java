package dexum.client.mixins.render;

import dexum.client.system.features.render.FullBright;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public class LightTextureMixin {

    // В 26.2 метод обновления света теперь называется render и принимает LightmapRenderState
    @Inject(method = "render(Lnet/minecraft/client/renderer/state/LightmapRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void onRender(LightmapRenderState renderState, CallbackInfo ci) {
        if (FullBright.active) {
            // Логика для FullBright: можно отменить ванильный рендер
            // или изменить renderState. Пока просто отменяем, чтобы не тратить ресурсы.
            // Сама яркость будет достигаться через другие механизмы (например, гамма-коррекцию).
            ci.cancel();
        }
    }
}