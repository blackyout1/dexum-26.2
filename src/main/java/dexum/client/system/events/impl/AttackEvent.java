package dexum.client.system.events.impl;

import dexum.client.system.events.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;

@Getter
@AllArgsConstructor
public class AttackEvent extends EventCancellable {
    private final LivingEntity target;
}
