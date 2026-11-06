package dexum.client.system.config;

import com.google.gson.*;
import dexum.client.system.api.Function;
import dexum.client.system.api.FunctionManager;
import dexum.client.system.settings.Setting;
import dexum.client.system.settings.impl.*;
import dexum.client.system.ui.PanelPositionManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Менеджер конфигов.
 * <p>
 * Запись атомарная (временный файл + {@code Files.move} с {@code ATOMIC_MOVE}),
 * чтобы краш посреди сохранения не оставлял битый конфиг.
 * <p>
 * Загрузка использует {@link Function#setEnabled(boolean)} (идемпотентный, без реентрантности)
 * вместо старого {@code toggle()}.
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    private static final File configsDir = new File(Minecraft.getInstance().gameDirectory, "dexum/configs");

    @Getter
    private static final List<Config> configs = new ArrayList<>();

    @Getter
    private static Config currentConfig;

    public static void init() {
        if (!configsDir.exists()) {
            configsDir.mkdirs();
        }
        loadConfigList();

        Config autosave = new Config("autosave");
        if (autosave.exists()) {
            autosave.load();
        }
        currentConfig = autosave;
    }

    public static void loadConfigList() {
        configs.clear();
        File[] files = configsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName().replace(".json", "");
                configs.add(new Config(name));
            }
        }
    }

    /**
     * Сохраняет конфиг атомарно: сначала пишем {@code <name>.json.tmp}, потом
     * {@code Files.move(..., ATOMIC_MOVE)}.
     */
    public static boolean saveConfig(Config config) {
        File target = config.getFile();
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        File tmp = new File(parent, target.getName() + ".tmp");

        try {
            JsonObject root = new JsonObject();
            JsonArray modulesArray = new JsonArray();

            for (Function function : FunctionManager.getFunctions()) {
                JsonObject moduleObj = new JsonObject();
                moduleObj.addProperty("name", function.getName());
                moduleObj.addProperty("enabled", function.isEnabled());
                moduleObj.addProperty("keybind", function.getKeybind());

                if (!function.getSettings().isEmpty()) {
                    JsonObject settingsObj = new JsonObject();
                    for (Setting setting : function.getSettings()) {
                        saveSetting(settingsObj, setting);
                    }
                    moduleObj.add("settings", settingsObj);
                }

                modulesArray.add(moduleObj);
            }

            root.add("modules", modulesArray);

            JsonObject panelsObj = new JsonObject();
            for (Map.Entry<String, PanelPositionManager.PanelPos> entry : PanelPositionManager.getAll().entrySet()) {
                JsonObject posObj = new JsonObject();
                posObj.addProperty("x", entry.getValue().x());
                posObj.addProperty("y", entry.getValue().y());
                panelsObj.add(entry.getKey(), posObj);
            }
            root.add("panels", panelsObj);

            JsonObject hudObj = new JsonObject();
            FunctionManager.getFunctions().stream()
                    .filter(f -> f instanceof dexum.client.system.features.render.Watermark)
                    .map(f -> (dexum.client.system.features.render.Watermark) f)
                    .findFirst().ifPresent(wm -> {
                        JsonObject wmPos = new JsonObject();
                        wmPos.addProperty("x", wm.getPosX());
                        wmPos.addProperty("y", wm.getPosY());
                        hudObj.add("watermark", wmPos);
                    });
            root.add("hud", hudObj);

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }

            try {
                Files.move(tmp.toPath(), target.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            return true;
        } catch (Exception e) {
            try { Files.deleteIfExists(tmp.toPath()); } catch (Exception ignored) {}
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loadConfig(Config config) {
        try {
            if (!config.exists()) return false;

            String content = new String(Files.readAllBytes(config.getFile().toPath()), StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            JsonArray modulesArray = root.getAsJsonArray("modules");

            for (JsonElement element : modulesArray) {
                JsonObject moduleObj = element.getAsJsonObject();
                String name = moduleObj.get("name").getAsString();

                Function function = FunctionManager.getFunctions().stream()
                        .filter(f -> f.getName().equals(name))
                        .findFirst()
                        .orElse(null);

                if (function == null) continue;

                // setEnabled идемпотентен — можно вызывать даже если состояние не меняется
                boolean enabled = moduleObj.get("enabled").getAsBoolean();
                function.setEnabled(enabled);

                int keybind = moduleObj.get("keybind").getAsInt();
                function.setKeybind(keybind);

                if (moduleObj.has("settings")) {
                    JsonObject settingsObj = moduleObj.getAsJsonObject("settings");
                    for (Setting setting : function.getSettings()) {
                        loadSetting(settingsObj, setting);
                    }
                }
            }

            if (root.has("panels")) {
                JsonObject panelsObj = root.getAsJsonObject("panels");
                Map<String, PanelPositionManager.PanelPos> positions = new java.util.HashMap<>();
                for (Map.Entry<String, JsonElement> entry : panelsObj.entrySet()) {
                    JsonObject posObj = entry.getValue().getAsJsonObject();
                    positions.put(entry.getKey(), new PanelPositionManager.PanelPos(
                            posObj.get("x").getAsInt(), posObj.get("y").getAsInt()));
                }
                PanelPositionManager.setAll(positions);
            }

            if (root.has("hud")) {
                JsonObject hudObj = root.getAsJsonObject("hud");
                if (hudObj.has("watermark")) {
                    JsonObject wmPos = hudObj.getAsJsonObject("watermark");
                    FunctionManager.getFunctions().stream()
                            .filter(f -> f instanceof dexum.client.system.features.render.Watermark)
                            .map(f -> (dexum.client.system.features.render.Watermark) f)
                            .findFirst().ifPresent(wm -> {
                                wm.setPosX(wmPos.get("x").getAsFloat());
                                wm.setPosY(wmPos.get("y").getAsFloat());
                            });
                }
            }

            currentConfig = config;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void saveSetting(JsonObject obj, Setting setting) {
        String name = setting.getName();
        if (setting instanceof BooleanSetting s) {
            obj.addProperty(name, s.isEnabled());
        } else if (setting instanceof NumberSetting s) {
            obj.addProperty(name, s.getValue());
        } else if (setting instanceof ModeSetting s) {
            obj.addProperty(name, s.getActiveMode());
        } else if (setting instanceof MultiSetting s) {
            JsonObject multiObj = new JsonObject();
            for (String option : s.getAllOptions()) {
                multiObj.addProperty(option, s.isEnabled(option));
            }
            obj.add(name, multiObj);
        } else if (setting instanceof ColorSetting s) {
            obj.addProperty(name, s.getColor().getRGB());
        } else if (setting instanceof KeybindSetting s) {
            obj.addProperty(name, s.getKeybind());
        }
    }

    private static void loadSetting(JsonObject obj, Setting setting) {
        String name = setting.getName();
        if (!obj.has(name)) return;

        try {
            if (setting instanceof BooleanSetting s) {
                s.setEnabled(obj.get(name).getAsBoolean());
            } else if (setting instanceof NumberSetting s) {
                s.setValue(obj.get(name).getAsDouble());
            } else if (setting instanceof ModeSetting s) {
                s.setMode(obj.get(name).getAsString());
            } else if (setting instanceof MultiSetting s) {
                JsonObject multiObj = obj.getAsJsonObject(name);
                for (String option : s.getAllOptions()) {
                    if (multiObj.has(option)) {
                        boolean enabled = multiObj.get(option).getAsBoolean();
                        boolean current = s.isEnabled(option);
                        if (enabled != current) s.toggle(option);
                    }
                }
            } else if (setting instanceof ColorSetting s) {
                int argb = obj.get(name).getAsInt();
                s.setColor(new java.awt.Color(argb, true));
            } else if (setting instanceof KeybindSetting s) {
                s.setKeybind(obj.get(name).getAsInt());
            }
        } catch (Exception ignored) {
            // Битый конфиг — пропускаем настройку
        }
    }

    public static void shutdown() {
        Config autosave = new Config("autosave");
        autosave.save();
    }
}