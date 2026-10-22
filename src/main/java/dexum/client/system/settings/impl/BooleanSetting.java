package dexum.client.system.settings.impl;

import dexum.client.system.settings.Setting;
import lombok.Getter;
import lombok.Setter;

public class BooleanSetting extends Setting {
    @Getter @Setter private boolean enabled;

    public BooleanSetting(String name, boolean enabled) {
        super(name);
        this.enabled = enabled; 
    }
}
