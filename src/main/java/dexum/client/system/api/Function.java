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
    @Getter private boolean enabled;
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

    /**
     * Пользовательский переключатель. Синоним {@link #setEnabled(boolean)} с инверсией.
     */
    public void toggle() {
        setEnabled(!enabled);
    }

    /**
     * Идемпотентная установка состояния с корректной обработкой реентрантности.
     * <p>
     * Если {@code onEnable()} / {@code onDisable()} внутри вызывает {@code setEnabled()} ещё раз —
     * внутренний вызов побеждает: после возврата из хука мы сверяем флаг {@code enabled}
     * и регистрируемся в EventBus только если он всё ещё {@code true}.
     * <p>
     * Исключение внутри {@code onEnable()} откатывает флаг и не регистрирует модуль.
     */
    public void setEnabled(boolean value) {
        if (value) {
            if (this.enabled) return;
            this.enabled = true;
            try {
                onEnable();
            } catch (Throwable t) {
                this.enabled = false;
                EventBus.unregister(this);
                throw t;
            }
            if (this.enabled) {
                EventBus.register(this);
            }
        } else {
            if (!this.enabled) return;
            this.enabled = false;
            EventBus.unregister(this);
            onDisable();
        }
    }

    public void onEnable() {}
    public void onDisable() {}
}