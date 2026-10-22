package dexum.client.mixins.entity;

import dexum.client.system.events.EventBus;
import dexum.client.system.events.impl.PushEvent;
import dexum.client.system.features.movement.NoPush;
import dexum.client.system.rotation.RotationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {

    @Redirect(method = "updateFluidInteraction",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;getFlow(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"),
            require = 0)
    private Vec3 stopWaterVelocity(FluidState instance, BlockGetter world, BlockPos pos) {
        if (NoPush.Instance != null && NoPush.Instance.isEnabled() && NoPush.water.isEnabled()) return Vec3.ZERO;
        PushEvent event = new PushEvent(PushEvent.Type.WATER);
        EventBus.post(event);
        if (event.isCancelled()) return Vec3.ZERO;
        return instance.getFlow(world, pos);
    }

    @ModifyVariable(method = "getViewVector(F)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), ordinal = 0, argsOnly = true, require = 0)
    private float modifyPitch(float pitch) {
        if ((Object) this instanceof LocalPlayer && RotationManager.INSTANCE.isActive())
            return RotationManager.INSTANCE.getServerPitch();
        return pitch;
    }

    @ModifyVariable(method = "getViewVector(F)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), ordinal = 0, argsOnly = true, index = 1, require = 0)
    private float modifyYaw(float yaw) {
        if ((Object) this instanceof LocalPlayer && RotationManager.INSTANCE.isActive())
            return RotationManager.INSTANCE.getServerYaw();
        return yaw;
    }
}
