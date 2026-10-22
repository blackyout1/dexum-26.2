package dexum.client.mixins.render;

import dexum.client.system.features.render.AntiBlind;
import dexum.client.system.features.render.NoHurtCam;
import dexum.client.utils.render.RenderMatrices;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"), require = 0)
    private void onRenderWorldHead(DeltaTracker tickCounter, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gameRenderer == null || mc.level == null) return;
        Camera camera = mc.gameRenderer.mainCamera();
        if (camera == null) return;
        Quaternionf invRot = new Quaternionf(camera.rotation()).conjugate();
        Matrix4f view = new Matrix4f().rotate(invRot);
        Matrix4f proj = new Matrix4f();
        RenderMatrices.setLastWorldSpaceMatrix(view);
        RenderMatrices.setLastModMat(new Matrix4f());
        RenderMatrices.setLastProjMat(proj);
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true, require = 0)
    private void onTiltViewWhenHurt(net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState, PoseStack matrices, CallbackInfo ci) {
        NoHurtCam m = NoHurtCam.getInstance();
        if (m != null && m.isEnabled()) ci.cancel();
    }

    @Inject(method = "getNightVisionScale", at = @At("HEAD"), cancellable = true, require = 0)
    private static void onGetNightVisionStrength(LivingEntity entity, float tickDelta,
                                                 CallbackInfoReturnable<Float> cir) {
        AntiBlind m = AntiBlind.getInstance();
        if (m == null || !m.isEnabled()) return;
        if (entity.hasEffect(MobEffects.NIGHT_VISION)) return;
        if (entity.hasEffect(MobEffects.BLINDNESS) || entity.hasEffect(MobEffects.DARKNESS))
            cir.setReturnValue(0.0f);
    }
}
