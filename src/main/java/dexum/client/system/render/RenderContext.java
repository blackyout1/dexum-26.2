package dexum.client.system.render;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;

@Getter
public class RenderContext {
    private final PoseStack matrices;
    private final float tickDelta;

    public RenderContext(PoseStack matrices, float tickDelta) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }
}
