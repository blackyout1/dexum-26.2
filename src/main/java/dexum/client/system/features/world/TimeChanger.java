package dexum.client.system.features.world;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.UpdateEvent;
import dexum.client.system.settings.impl.ModeSetting;
import dexum.client.system.settings.impl.NumberSetting;

import java.lang.reflect.Field;

@NewFunction(name = "TimeChanger", desc = "Меняет время суток клиентски", category = Category.WORLD)
public class TimeChanger extends Function {

    private final ModeSetting timeMode = new ModeSetting("Время",
            "День", "Полдень", "Закат", "Ночь", "Полночь", "Рассвет", "Кастом");
    private final NumberSetting customTime = new NumberSetting("Кастом тики", 6000, 0, 23999, 100);

    private Field timeOfDayField = null;
    private boolean fieldSearched = false;

    public TimeChanger() {
        addSettings(timeMode, customTime);
        customTime.setVisible(false);
        timeMode.runnable(() -> customTime.setVisible(timeMode.isEnabled("Кастом")));
    }

    @Override
    public void onDisable() {
        timeOfDayField = null;
        fieldSearched = false;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (nullCheck.world()) return;

        long targetTime = switch (timeMode.getActiveMode()) {
            case "День"    -> 1000L;
            case "Полдень" -> 6000L;
            case "Закат"   -> 12000L;
            case "Ночь"    -> 15000L;
            case "Полночь" -> 18000L;
            case "Рассвет" -> 23000L;
            case "Кастом"  -> (long) customTime.getValue();
            default        -> 6000L;
        };

        Object props = mc.level.getLevelData();
        if (props == null) return;

        if (!fieldSearched) {
            fieldSearched = true;
            timeOfDayField = findTimeField(props.getClass());
        }

        if (timeOfDayField != null) {
            try {
                timeOfDayField.setLong(props, targetTime);
            } catch (Exception ignored) {}
        }
    }

    private static Field findTimeField(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) return null;
        for (Field f : clazz.getDeclaredFields()) {
            String n = f.getName();
            if (f.getType() == long.class && (n.contains("time") || n.contains("Time") || n.contains("dayTime"))) {
                f.setAccessible(true);
                return f;
            }
        }
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getType() == long.class) {
                f.setAccessible(true);
                return f;
            }
        }
        return findTimeField(clazz.getSuperclass());
    }
}
