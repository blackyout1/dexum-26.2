package dexum.client.system.events.impl;

import dexum.client.system.events.Event;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;

@Getter
public class Render3DEvent implements Event {

    private final PoseStack matrix;
    private final float partialTicks;

    public Render3DEvent(PoseStack matrix, float partialTicks) {
        this.matrix = matrix;
        this.partialTicks = partialTicks;
    }
}
