package dexum.client.system.events.impl;

import dexum.client.system.events.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
public class CameraPositionEvent implements Event {
    private Vec3 pos;
    private float yaw;
    private float pitch;
    private boolean cancelled = false;

    public CameraPositionEvent(Vec3 pos, float yaw, float pitch) {
        this.pos   = pos;
        this.yaw   = yaw;
        this.pitch = pitch;
    }
}
