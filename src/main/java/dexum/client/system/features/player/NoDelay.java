package dexum.client.system.features.player;

import dexum.client.interfaces.IClientPlayerInteractionManager;
import dexum.client.interfaces.ILivingEntity;
import dexum.client.interfaces.IMinecraftClient;
import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.UpdateEvent;
import dexum.client.system.settings.impl.MultiSetting;

@NewFunction(name = "NoDelay", desc = "Убирает задержку у выбранных элементов", category = Category.PLAYER)
public class NoDelay extends Function {
    final MultiSetting delay = new MultiSetting("Убрать задержку", "Прыжка", "Ломания", "ПКМ");

    public NoDelay() {
        addSettings(delay);
    }

    @EventHandler
    public void onTick(UpdateEvent e) {
        if (nullCheck.player()) return;

        if (delay.isEnabled("Ломания")) ((IClientPlayerInteractionManager) mc.gameMode).setBlockBreakingCooldown(0);
        if (delay.isEnabled("Прыжка"))  ((ILivingEntity) mc.player).setJumpingCooldown(0);
        if (delay.isEnabled("ПКМ"))     ((IMinecraftClient) mc).setItemUseCooldown(0);
    }
}
