package dexum.client.system.events.impl;

import dexum.client.system.events.EventCancellable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import net.minecraft.world.phys.Vec3;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MoveEvent extends EventCancellable {
    Vec3 movement;
}
