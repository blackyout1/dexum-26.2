package dexum.client.utils.render.pipeline;

import com.mojang.blaze3d.textures.GpuTextureView;
import org.joml.Matrix4f;

/**
 * BlurPipeline — stubbed out for 26.2 compilation.
 * The GPU/pipeline APIs changed significantly; restore functionality later.
 */
public class BlurPipeline {

    public static void init() {}

    public static void draw(Matrix4f matrix, float x, float y, float width, float height, float radius,
                            float strength, float z) {
        // Stubbed — blur pipeline not yet ported to 26.2
    }

    public static GpuTextureView getBlurTextureView() {
        return null;
    }

    public static void shutdown() {}
}
