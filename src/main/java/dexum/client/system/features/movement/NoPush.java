package dexum.client.system.features.movement;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.settings.impl.BooleanSetting;

@NewFunction(name = "NoPush", desc = "Отключает отталкивание от сущностей и воды", category = Category.MOVEMENT)
public class NoPush extends Function {
    public static NoPush Instance;
    public static BooleanSetting players = new BooleanSetting("Сущности", true);
    public static BooleanSetting water = new BooleanSetting("Вода", true);

    public NoPush() {
        Instance = this;
        addSettings(players, water);
    }
}
