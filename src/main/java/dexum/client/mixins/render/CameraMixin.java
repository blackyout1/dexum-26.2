package dexum.client.mixins.render;

import dexum.client.system.events.EventBus;
import dexum.client.system.events.impl.CameraPositionEvent;
import dexum.client.system.events.impl.RotationEvent;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private float xRot;
    @Shadow private float yRot;
    @Shadow private Vec3 position;
    @Shadow protected abstract void setPosition(double x, double y, double z);
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "setup", at = @At("RETURN"), require = 0)
    private void onUpdate(BlockGetter area, Entity focusedEntity, boolean thirdPerson,
                          boolean inverseView, float tickDelta, CallbackInfo ci) {
        CameraPositionEvent event = new CameraPositionEvent(this.position, this.yRot, this.xRot);
        EventBus.post(event);
        if (event.isCancelled()) {
            Vec3 p = event.getPos();
            setPosition(p.x, p.y, p.z);
            this.yRot  = event.getYaw();
            this.xRot  = event.getPitch();
        }
    }

    @Redirect(method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"),
            require = 0)
    private void onSetRotation(Camera instance, float yaw, float pitch,
                                BlockGetter area, Entity focusedEntity, boolean thirdPerson,
                                boolean inverseView, float tickDelta) {
        RotationEvent event = new RotationEvent(yaw, pitch);
        EventBus.post(event);
        float newYaw   = event.getYaw();
        float newPitch = event.getPitch();
        if (thirdPerson && inverseView) { newYaw += 180f; newPitch = -newPitch; }
        setRotation(newYaw, newPitch);
    }
}
