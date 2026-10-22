package dexum.client.mixins.render;

import dexum.client.system.api.FunctionManager;
import dexum.client.system.features.render.Nametags;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    /**
     * Подавляем shouldShowName для всех entity когда Nametags включён
     */
    @Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true, require = 0)
    private void onShouldShowName(Entity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        Nametags mod = FunctionManager.getFunction(Nametags.class);
        if (mod != null && mod.isEnabled()) {
            if (entity instanceof Player) {
                cir.setReturnValue(false);
                return;
            }
            if (entity.hasCustomName()) {
                cir.setReturnValue(false);
            }
        }
    }

    /**
     * Дополнительная защита — отменяем сам рендер неймтегов
     */
    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true, require = 0)
    private void onRenderNameTag(Entity entity, CallbackInfo ci) {
        Nametags mod = FunctionManager.getFunction(Nametags.class);
        if (mod != null && mod.isEnabled()) {
            if (entity instanceof Player) {
                ci.cancel();
                return;
            }
            if (entity.hasCustomName()) {
                ci.cancel();
            }
        }
    }
}
