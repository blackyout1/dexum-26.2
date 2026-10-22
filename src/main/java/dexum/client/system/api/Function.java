package dexum.client.system.api;

import dexum.client.system.events.EventBus;
import dexum.client.system.settings.Setting;
import dexum.client.utils.helpers.NullCheck;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Function {
    protected final Minecraft mc = Minecraft.getInstance();
    protected final NullCheck nullCheck = new NullCheck();
    @Getter private final String name;
    @Getter private final String desc;
    @Getter private final Category category;
    @Getter @Setter private int keybind;
    @Getter @Setter private boolean enabled;
    @Getter private final List<Setting> settings = new ArrayList<>();

    public Function() {
        if (this.getClass().isAnnotationPresent(NewFunction.class)) {
            NewFunction annotation = this.getClass().getAnnotation(NewFunction.class);
            this.name = annotation.name();
            this.desc = annotation.desc();
            this.category = annotation.category();
            this.enabled = false;
        } else {
            throw new RuntimeException("Error " + this.getClass().getSimpleName());
        }
    }

    public void addSettings(Setting... setting) {
        this.settings.addAll(Arrays.asList(setting));
    }

    public void toggle() {
        this.enabled = !this.enabled;
        if (enabled) {
            onEnable();
            EventBus.register(this);
        } else {
            onDisable();
            EventBus.unregister(this);
        }
    }

    public void onEnable() {}
    public void onDisable() {}
}
