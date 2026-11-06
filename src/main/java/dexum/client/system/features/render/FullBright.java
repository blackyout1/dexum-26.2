package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;

@NewFunction(name = "FullBright", desc = "Убирает темноту — гамма на максимум", category = Category.RENDER)
public class FullBright extends Function {
    public static boolean active = false;

    @Override
    public void onEnable() { active = true; }

    @Override
    public void onDisable() { active = false; }
}