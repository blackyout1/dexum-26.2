package dexum.client.system.events.impl;

import dexum.client.system.events.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Вызывается при движении мыши перед changeLookDirection.
 * yaw/pitch — дельта движения мыши (уже с учётом чувствительности).
 * Отмена → камера не двигается.
 */
@Getter
@Setter
@AllArgsConstructor
public class LookEvent extends EventCancellable {
    private double yaw;
    private double pitch;
}
