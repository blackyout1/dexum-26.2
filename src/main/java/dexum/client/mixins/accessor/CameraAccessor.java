package dexum.client.mixins.accessor;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Invoker(value = "setRotation", remap = false)
    void dexum$setRotation(float yaw, float pitch);
}
