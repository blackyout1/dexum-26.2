package dexum.client.system.events.impl;

import dexum.client.system.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.damagesource.DamageSource;

@Getter
@AllArgsConstructor
public class DamageEvent implements Event {
    private final DamageSource source;
    private final float amount;
}
