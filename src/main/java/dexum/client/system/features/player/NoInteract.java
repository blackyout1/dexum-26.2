package dexum.client.system.features.player;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.settings.impl.BooleanSetting;

@NewFunction(name = "NoInteract", desc = "Блокирует случайное взаимодействие с блоками и сущностями", category = Category.PLAYER)
public class NoInteract extends Function {

    public static NoInteract Instance;

    public final BooleanSetting onlyDuringAura = new BooleanSetting("Только при Aura", true);
    public final BooleanSetting blocks          = new BooleanSetting("Блоки", true);
    public final BooleanSetting entities        = new BooleanSetting("Сущности", true);

    public NoInteract() {
        Instance = this;
        addSettings(onlyDuringAura, blocks, entities);
    }
}
