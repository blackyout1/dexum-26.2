package dexum.client.system.features.world;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.UpdateEvent;
import lombok.Getter;

@NewFunction(name = "NoWeather", desc = "Убирает дождь и гром клиентски", category = Category.WORLD)
public class NoWeather extends Function {

    @Getter
    private static NoWeather instance;

    public NoWeather() {
        instance = this;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        // rainLevel and thunderLevel fields are not directly accessible in 26.2
        // This feature is stubbed out — requires an accessor mixin to implement
    }
}
