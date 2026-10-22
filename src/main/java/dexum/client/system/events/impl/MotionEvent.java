package dexum.client.system.events.impl;

import dexum.client.system.events.EventCancellable;
import lombok.Getter;
import lombok.Setter;

/**
 * Вызывается прямо перед отправкой пакета движения серверу.
 * Модули могут подменить yaw/pitch в пакете не трогая mc.player.
 */
@Getter
@Setter
public class MotionEvent extends EventCancellable {
    private float yaw;
    private float pitch;

    public MotionEvent(float yaw, float pitch) {
        this.yaw   = yaw;
        this.pitch = pitch;
    }
}
