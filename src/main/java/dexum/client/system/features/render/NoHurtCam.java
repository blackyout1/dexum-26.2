package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import lombok.Getter;

@NewFunction(name = "NoHurtCam", desc = "Убирает тряску экрана при получении урона", category = Category.RENDER)
public class NoHurtCam extends Function {

    @Getter
    private static NoHurtCam instance;

    public NoHurtCam() {
        instance = this;
    }
}
