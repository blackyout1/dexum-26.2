package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;

@NewFunction(name = "NoFov", desc = "Отключает эффекты для фов", category = Category.RENDER)
public class NoFov extends Function {
    private double prevFov;

    @Override
    public void onEnable() {
        if (mc.options == null) return;
        prevFov = mc.options.fovEffectScale().get();
        mc.options.fovEffectScale().set(0.0);
    }

    @Override
    public void onDisable() {
        if (mc.options == null) return;
        mc.options.fovEffectScale().set(prevFov);
    }
}
