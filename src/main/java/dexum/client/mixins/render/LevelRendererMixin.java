package dexum.client.mixins.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dexum.client.system.events.EventBus;
import dexum.client.system.events.impl.Render3DEvent;
import dexum.client.utils.render.RenderMatrices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At("RETURN"), require = 0)
    private void onRenderLevel(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null || mc.gameRenderer == null) return;
        Matrix4f modelView = new Matrix4f().rotate(new org.joml.Quaternionf(mc.gameRenderer.mainCamera().rotation()).conjugate());
        RenderMatrices.setLastWorldSpaceMatrix(modelView);
        RenderMatrices.setLastModMat(modelView);
        RenderMatrices.setLastProjMat(new Matrix4f());
        float tickDelta = 1.0f;
        PoseStack stack = new PoseStack();
        stack.last().pose().set(modelView);
        EventBus.post(new Render3DEvent(stack, tickDelta));
    }
}
