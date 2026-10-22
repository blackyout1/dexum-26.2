package dexum.client.system.settings.impl;

import dexum.client.system.settings.Setting;
import lombok.Getter;
import lombok.Setter;

public class KeybindSetting extends Setting {
    @Getter @Setter int keybind;

    public KeybindSetting(String name, int keybind) {
        super(name);
        this.keybind = keybind;
    }
}
