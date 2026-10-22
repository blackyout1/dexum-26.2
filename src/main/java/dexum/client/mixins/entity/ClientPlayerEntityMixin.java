package dexum.client.mixins.entity;

import com.mojang.authlib.GameProfile;
import dexum.client.system.events.EventBus;
import dexum.client.system.events.impl.MoveEvent;
import dexum.client.system.events.impl.MotionEvent;
import dexum.client.system.events.impl.NoSlowEvent;
import dexum.client.system.events.impl.PostMotionEvent;
import dexum.client.system.events.impl.PushEvent;
import dexum.client.system.events.impl.UpdateEvent;
import dexum.client.system.rotation.ServerAngleStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer {

    @Shadow public abstract boolean isUsingItem();
    @Final @Shadow protected Minecraft minecraft;

    public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void onTick(CallbackInfo ci) {
        // UpdateEvent now sent via ClientTickEvents in Dexum.java
    }

    @Redirect(method = "aiStep",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"),
            require = 0)
    private boolean applyMovementSpeedFactors(LocalPlayer instance) {
        NoSlowEvent event = new NoSlowEvent();
        EventBus.post(event);
        if (event.isCancelled()) return false;
        return instance.isUsingItem();
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true, require = 0)
    public void onPushOutOfBlocks(double x, double z, CallbackInfo ci) {
        PushEvent event = new PushEvent(PushEvent.Type.BLOCK);
        EventBus.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "move",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"),
            cancellable = true, require = 0)
    public void onMove(MoverType movementType, Vec3 movement, CallbackInfo ci) {
        MoveEvent event = new MoveEvent(movement);
        EventBus.post(event);
        super.move(movementType, event.getMovement());
        ci.cancel();
    }

    private float dexum$savedYaw;
    private float dexum$savedPitch;

    @Inject(method = "sendPosition", at = @At("HEAD"), require = 0)
    private void onSendMovementPacketsHead(CallbackInfo ci) {
        if (minecraft.player == null) return;
        dexum$savedYaw   = minecraft.player.getYRot();
        dexum$savedPitch = minecraft.player.getXRot();
        ServerAngleStore store = ServerAngleStore.INSTANCE;
        store.tick();
        if (store.isActive()) {
            minecraft.player.setYRot(store.getServerYaw());
            minecraft.player.setXRot(store.getServerPitch());
        }
        float yaw   = minecraft.player.getYRot();
        float pitch = minecraft.player.getXRot();
        MotionEvent motionEvent = new MotionEvent(yaw, pitch);
        EventBus.post(motionEvent);
        if (!motionEvent.isCancelled()) {
            minecraft.player.setYRot(motionEvent.getYaw());
            minecraft.player.setXRot(motionEvent.getPitch());
        }
    }

    @Inject(method = "sendPosition", at = @At("RETURN"), require = 0)
    private void onSendMovementPacketsReturn(CallbackInfo ci) {
        if (minecraft.player == null) return;
        if (ServerAngleStore.INSTANCE.isActive()) {
            minecraft.player.setYRot(dexum$savedYaw);
            minecraft.player.setXRot(dexum$savedPitch);
        }
        EventBus.post(new PostMotionEvent());
    }
}
