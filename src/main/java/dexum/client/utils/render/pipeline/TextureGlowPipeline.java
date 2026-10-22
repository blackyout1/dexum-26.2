package dexum.client.utils.render.pipeline;

import com.mojang.blaze3d.textures.GpuTextureView;
import org.joml.Matrix4f;

/** Stubbed for 26.2 — GPU pipeline API changed, restore later. */
public class TextureGlowPipeline {
    public static void draw(Matrix4f matrix, float x, float y, float size,
                            GpuTextureView textureView, int color,
                            float radius, float glowSize, float glowIntensity, float z) {}
    public static void init() {}
    public static void shutdown() {}
}
