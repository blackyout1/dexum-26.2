package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;

@NewFunction(name = "FullBright", desc = "Убирает темноту — гамма на максимум", category = Category.RENDER)
public class FullBright extends Function {

    private double savedGamma = 1.0;

    @Override
    public void onEnable() {
        savedGamma = mc.options.gamma().get();
        mc.options.gamma().set(16.0);
    }

    @Override
    public void onDisable() {
        mc.options.gamma().set(savedGamma);
    }
}
