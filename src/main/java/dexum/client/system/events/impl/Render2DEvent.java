package dexum.client.system.events.impl;

import dexum.client.system.events.Event;
import lombok.Getter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@Getter
public class Render2DEvent implements Event {
    private final GuiGraphicsExtractor context;
    private final DeltaTracker tickCounter;

    public Render2DEvent(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        this.context = context;
        this.tickCounter = tickCounter;
    }
}
