package dexum.client.system.ui.components.settings;

import dexum.client.system.settings.Setting;
import dexum.client.system.settings.impl.*;

public class SettingComponentFactory {
    
    public static SettingComponent create(Setting setting, int width) {
        if (setting instanceof BooleanSetting) {
            return new BooleanComponent((BooleanSetting) setting, width);
        } else if (setting instanceof NumberSetting) {
            return new SliderComponent((NumberSetting) setting, width);
        } else if (setting instanceof ModeSetting) {
            return new ModeComponent((ModeSetting) setting, width);
        } else if (setting instanceof MultiSetting) {
            return new MultiComponent((MultiSetting) setting, width);
        }
        return null;
    }
}
