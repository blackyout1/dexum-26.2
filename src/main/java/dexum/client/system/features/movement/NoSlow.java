package dexum.client.system.features.movement;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.NoSlowEvent;
import dexum.client.system.events.impl.UpdateEvent;
import dexum.client.system.settings.impl.ModeSetting;
import lombok.Getter;

@NewFunction(name = "NoSlow", desc = "Убирает замедления от использования предметов", category = Category.MOVEMENT)
public class NoSlow extends Function {

    @Getter private static NoSlow instance;
    public static final ModeSetting mode = new ModeSetting("Режим", "Vanilla", "Grim");

    private int ticks = 0;
    private boolean cancel = false;

    public NoSlow() {
        addSettings(mode);
        instance = this;
    }

    @EventHandler
    public void onNoSlow(NoSlowEvent event) {
        if (nullCheck.all()) return;

        switch (mode.getActiveMode()) {
            case "Vanilla" -> event.setCancelled(true);
            case "Grim" -> {
                if (cancel) event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (nullCheck.all()) return;

        if (!mc.player.isUsingItem()) {
            ticks = 0;
            cancel = false;
            return;
        }

        ticks++;
        cancel = (ticks % 2 == 0);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ticks = 0;
        cancel = false;
    }
}
