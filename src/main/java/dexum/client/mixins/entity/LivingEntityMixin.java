package dexum.client.mixins.entity;

import dexum.client.interfaces.ILivingEntity;
import dexum.client.system.events.EventBus;
import dexum.client.system.events.impl.JumpEvent;
import dexum.client.system.events.impl.PushEvent;
import dexum.client.system.rotation.ServerAngleStore;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ILivingEntity {

    private static final Field JUMP_TICKS = resolveField();
    private static Field resolveField() {
        for (String name : new String[]{"jumpTicks", "jumpCooldown", "ticksUntilNextJump"}) {
            try {
                Field f = LivingEntity.class.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }

    @Override
    public void setJumpingCooldown(int val) {
        if (JUMP_TICKS == null) return;
        try { JUMP_TICKS.setInt(this, val); } catch (IllegalAccessException ignored) {}
    }

    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true, require = 0)
    public void onIsPushable(CallbackInfoReturnable<Boolean> cir) {
        PushEvent event = new PushEvent(PushEvent.Type.COLLISION);
        EventBus.post(event);
        if (event.isCancelled()) cir.setReturnValue(false);
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true, require = 0)
    private void onJump(CallbackInfo ci) {
        if ((Object) this instanceof LocalPlayer player) {
            JumpEvent event = new JumpEvent(player);
            EventBus.post(event);
            if (event.isCancelled()) ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "NEW", target = "(DDD)Lnet/minecraft/world/phys/Vec3;"), require = 0)
    private Vec3 hookJumpMoveCorrection(Vec3 original) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || (Object) this != client.player) return original;
        if (!ServerAngleStore.INSTANCE.isActive()) return original;

        float yaw = ServerAngleStore.INSTANCE.getServerYaw() * 0.017453292F;
        return new Vec3(-Mth.sin(yaw) * 0.2F, 0.0, Mth.cos(yaw) * 0.2F);
    }
}