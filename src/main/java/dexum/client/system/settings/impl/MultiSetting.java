package dexum.client.system.settings.impl;

import dexum.client.system.settings.Setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiSetting extends Setting {
    private final Map<String, Boolean> options = new HashMap<>();
    // порядок опций сохраняется для GUI
    private final List<String> allOptions = new ArrayList<>();

    public MultiSetting(String name, String... defaultOptions) {
        super(name);
        for (String opt : defaultOptions) {
            options.put(opt, false);
            allOptions.add(opt);
        }
    }

    /** Только те опции у которых isEnabled == true */
    public List<String> getEnabled() {
        List<String> result = new ArrayList<>();
        for (String opt : allOptions) {
            if (options.getOrDefault(opt, false)) result.add(opt);
        }
        return result;
    }

    /** Все опции (для GUI) */
    public List<String> getAllOptions() {
        return allOptions;
    }

    public boolean isEnabled(String option) {
        return options.getOrDefault(option, false);
    }

    public void toggle(String option) {
        if (options.containsKey(option)) {
            options.put(option, !options.get(option));
        }
    }

    public void enable(String option) {
        if (options.containsKey(option)) options.put(option, true);
    }

    public void disable(String option) {
        if (options.containsKey(option)) options.put(option, false);
    }
}
