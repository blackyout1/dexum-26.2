package dexum.client.system.features.player;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.UpdateEvent;
import dexum.client.system.settings.impl.NumberSetting;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;

@NewFunction(name = "AutoRespawn", desc = "Автоматически возрождается после смерти", category = Category.PLAYER)
public class AutoRespawn extends Function {

    private final NumberSetting delay = new NumberSetting("Задержка (с)", 0.5, 0.0, 5.0, 0.1);

    private long deathTime = -1L;

    public AutoRespawn() {
        addSettings(delay);
    }

    @Override
    public void onEnable() {
        deathTime = -1L;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (nullCheck.player()) return;

        if (mc.gui.screen() instanceof DeathScreen) {
            if (deathTime < 0) {
                deathTime = System.currentTimeMillis();
            }
            long delayMs = (long) (delay.getValue() * 1000);
            if (System.currentTimeMillis() - deathTime >= delayMs) {
                if (mc.player.connection != null) {
                    mc.player.connection.send(
                            new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN)
                    );
                }
                mc.gui.setScreen(null);
                deathTime = -1L;
            }
        } else {
            deathTime = -1L;
        }
    }
}
