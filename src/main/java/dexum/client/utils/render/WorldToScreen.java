package dexum.client.utils.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class WorldToScreen {

    private WorldToScreen() {}

    public static float[] project(double worldX, double worldY, double worldZ) {
        float[] out = new float[2];
        if (!project(worldX, worldY, worldZ, out)) return null;
        return out;
    }

    public static boolean project(double worldX, double worldY, double worldZ, float[] out) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gameRenderer == null) return false;

        Camera cam = mc.gameRenderer.mainCamera();
        if (cam == null) return false;

        Vec3 camPos = cam.position();

        // Относительные координаты от камеры
        float relX = (float) (worldX - camPos.x);
        float relY = (float) (worldY - camPos.y);
        float relZ = (float) (worldZ - camPos.z);

        // Получаем готовую матрицу view + rotation + projection от MC
        Matrix4f matrix = cam.getViewRotationProjectionMatrix(new Matrix4f());

        // Применяем матрицу к точке
        Vector4f clip = matrix.transform(relX, relY, relZ, 1.0f, new Vector4f());

        // w <= 0 значит точка за спиной
        if (clip.w() <= 1.0e-4f) return false;

        // NDC (normalize device coordinates)
        double ndcX = clip.x() / clip.w();
        double ndcY = clip.y() / clip.w();

        // Переводим в GUI-scaled экранные координаты
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // NDC: x=[-1,1], y=[-1,1] (y up in clip space)
        // GUI: x=[0,width], y=[0,height] (y down)
        out[0] = (float) ((ndcX * 0.5 + 0.5) * width);
        out[1] = (float) ((0.5 - ndcY * 0.5) * height);
        return true;
    }
}
