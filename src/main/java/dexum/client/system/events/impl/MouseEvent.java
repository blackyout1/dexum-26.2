package dexum.client.system.events.impl;

import dexum.client.system.events.Event;
import lombok.Getter;

@Getter
public class MouseEvent implements Event {
    private final double x;
    private final double y;
    private final int    button; // -1 если это движение мыши
    private final int    action; // 0=release, 1=press, -1=move

    public MouseEvent(double x, double y, int button, int action) {
        this.x      = x;
        this.y      = y;
        this.button = button;
        this.action = action;
    }

    public boolean isPress()   { return action == 1; }
    public boolean isRelease() { return action == 0; }
    public boolean isMove()    { return action == -1; }
}
