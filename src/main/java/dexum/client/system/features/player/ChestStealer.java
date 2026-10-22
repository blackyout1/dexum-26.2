package dexum.client.system.features.player;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.UpdateEvent;
import dexum.client.system.settings.impl.BooleanSetting;
import dexum.client.system.settings.impl.NumberSetting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@NewFunction(name = "ChestStealer", desc = "Рандомизированный лут из сундука с человеческой задержкой", category = Category.PLAYER)
public class ChestStealer extends Function {

    private final NumberSetting minDelay  = new NumberSetting("Мин. задержка (мс)", 80, 20, 500, 10);
    private final NumberSetting maxDelay  = new NumberSetting("Макс. задержка (мс)", 250, 50, 600, 10);
    private final BooleanSetting randomOrder = new BooleanSetting("Случайный порядок", true);
    private final BooleanSetting autoClose   = new BooleanSetting("Авто-закрытие", true);

    private final Random random = new Random();
    private long lastClickTime = 0L;
    private long nextDelay = 0L;

    public ChestStealer() {
        addSettings(minDelay, maxDelay, randomOrder, autoClose);
    }

    @Override
    public void onEnable() {
        lastClickTime = 0L;
        nextDelay = 0L;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (nullCheck.all()) return;
        if (!(mc.gui.screen() instanceof AbstractContainerScreen<?> screen)) return;

        long now = System.currentTimeMillis();
        if (now - lastClickTime < nextDelay) return;

        AbstractContainerMenu handler = screen.getMenu();

        List<Slot> containerSlots = new ArrayList<>();
        for (Slot slot : handler.slots) {
            if (slot.container != mc.player.getInventory() && slot.hasItem()) {
                containerSlots.add(slot);
            }
        }

        if (containerSlots.isEmpty()) {
            if (autoClose.isEnabled()) {
                mc.player.closeContainer();
            }
            return;
        }

        if (randomOrder.isEnabled()) {
            Collections.shuffle(containerSlots, random);
        }

        Slot target = containerSlots.get(0);

        if (mc.player.getInventory().getFreeSlot() == -1) {
            if (autoClose.isEnabled()) {
                mc.player.closeContainer();
            }
            return;
        }

        mc.gameMode.handleContainerInput(
                handler.containerId,
                target.index,
                0,
                net.minecraft.world.inventory.ContainerInput.QUICK_MOVE,
                mc.player
        );

        int minMs = (int) minDelay.getValue();
        int maxMs = (int) maxDelay.getValue();
        if (maxMs < minMs) maxMs = minMs;
        nextDelay = minMs + random.nextInt(maxMs - minMs + 1);
        lastClickTime = now;
    }
}
