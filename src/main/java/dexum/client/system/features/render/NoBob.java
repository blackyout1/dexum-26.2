package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;

@NewFunction(name = "NoBob", desc = "Убирает покачивание камеры", category = Category.RENDER)
public class NoBob extends Function {
    @Override
    public void onEnable() {
        mc.options.bobView().set(false);
    }

    @Override
    public void onDisable() {
        mc.options.bobView().set(true);
    }
}
