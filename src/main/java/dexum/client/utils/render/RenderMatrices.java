package dexum.client.utils.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

public class RenderMatrices {
    private static Matrix4f lastProjMat = new Matrix4f();
    private static Matrix4f lastModMat = new Matrix4f();
    private static Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    public static void setLastProjMat(Matrix4f mat) { lastProjMat.set(mat); }
    public static void setLastModMat(Matrix4f mat) { lastModMat.set(mat); }
    public static void setLastWorldSpaceMatrix(Matrix4f mat) { lastWorldSpaceMatrix.set(mat); }
    public static Matrix4f getLastProjMat() { return lastProjMat; }
    public static Matrix4f getLastModMat() { return lastModMat; }
    public static Matrix4f getLastWorldSpaceMatrix() { return lastWorldSpaceMatrix; }

    public static void updateMatrices() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gameRenderer == null) return;

        Camera camera = mc.gameRenderer.mainCamera();
        if (camera == null) return;

        float fov = (float) Math.toRadians(mc.options.fov().get());
        float aspectRatio = (float) mc.getWindow().getScreenWidth() / (float) mc.getWindow().getScreenHeight();
        Matrix4f projMatrix = new Matrix4f().perspective(fov, aspectRatio, 0.05f, 1000.0f);
        setLastProjMat(projMatrix);

        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();
        viewMatrix.rotateX((float) Math.toRadians(camera.xRot()));
        viewMatrix.rotateY((float) Math.toRadians(camera.yRot() + 180.0f));
        setLastModMat(viewMatrix);
        setLastWorldSpaceMatrix(viewMatrix);
    }
}
