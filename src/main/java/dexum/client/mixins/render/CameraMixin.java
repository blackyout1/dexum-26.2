package dexum.client.mixins.render;

import dexum.client.system.events.EventBus;
import dexum.client.system.events.impl.CameraPositionEvent;
import dexum.client.system.events.impl.RotationEvent;
import dexum.client.system.features.render.FreeCam;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private Vec3 position;
    @Shadow private float xRot;
    @Shadow private float yRot;
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * Инжект в TAIL метода alignWithEntity (в 26.2 он заменил setup).
     * Здесь мы напрямую перезаписываем поля position/yRot/xRot,
     * не полагаясь на публичные геттеры, имя которых может меняться.
     */
    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void dexum$afterAlignWithEntity(float tickDelta, CallbackInfo ci) {
        FreeCam fc = FreeCam.INSTANCE;
        if (fc != null && fc.isActive()) {
            // FreeCam активен – принудительно ставим позицию и углы камеры
            this.position = fc.getCameraPosition();
            this.yRot = minecraft.player.getYRot();   // камера следует за игроком
            this.xRot = minecraft.player.getXRot();
            return;
        }

        // Для других модулей (например, Aura) пробрасываем событие
        CameraPositionEvent event = new CameraPositionEvent(this.position, this.yRot, this.xRot);
        EventBus.post(event);
        if (event.isCancelled()) {
            this.position = event.getPos();
            this.yRot = event.getYaw();
            this.xRot = event.getPitch();
        }
    }

    /**
     * RotationEvent для Aura/FreeLook.
     * Если FreeCam активен, не перебиваем rotation (она уже установлена выше).
     */
    @Redirect(method = "alignWithEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void dexum$onSetRotation(Camera instance, float yaw, float pitch) {
        FreeCam fc = FreeCam.INSTANCE;
        if (fc != null && fc.isActive()) {
            // FreeCam уже выставил нужные углы в @Inject, здесь ничего не делаем
            return;
        }
        RotationEvent event = new RotationEvent(yaw, pitch);
        EventBus.post(event);
        ((CameraMixin) (Object) instance).setRotation(event.getYaw(), event.getPitch());
    }
}