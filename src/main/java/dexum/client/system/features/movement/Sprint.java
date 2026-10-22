package dexum.client.system.features.movement;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.UpdateEvent;

@NewFunction(name = "Sprint", desc = "Автоматически удерживает состояние бега", category = Category.MOVEMENT)
public class Sprint extends Function {

    @EventHandler
    public void update(UpdateEvent event) {
        if (nullCheck.player()) return;

        if (mc.player.isSprinting()) {
            if (!canSprint()) {
                mc.options.keySprint.setDown(false);
                mc.player.setSprinting(false);
            }
        } else if (canSprint()) {
            mc.options.keySprint.setDown(true);
        }
    }

    private boolean canSprint() {
        return !mc.player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)
                && mc.player.input.hasForwardImpulse()
                && mc.player.getFoodData().getFoodLevel() > 6;
    }
}
