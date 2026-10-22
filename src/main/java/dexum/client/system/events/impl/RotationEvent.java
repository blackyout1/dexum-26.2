package dexum.client.system.events.impl;

import dexum.client.system.events.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Вызывается когда Camera.setRotation(yaw, pitch) применяется.
 * Модули могут подменить yaw/pitch камеры.
 */
@Getter
@Setter
@AllArgsConstructor
public class RotationEvent extends EventCancellable {
    private float yaw;
    private float pitch;
}
