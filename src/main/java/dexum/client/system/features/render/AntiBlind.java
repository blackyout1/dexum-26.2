package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import lombok.Getter;

@NewFunction(name = "AntiBlind", desc = "Убирает эффекты темноты и слепоты", category = Category.RENDER)
public class AntiBlind extends Function {

    @Getter
    private static AntiBlind instance;

    public AntiBlind() {
        instance = this;
    }
}
