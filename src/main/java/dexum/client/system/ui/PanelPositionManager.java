package dexum.client.system.ui;

import java.util.HashMap;
import java.util.Map;

/**
 * Хранит позиции панелей ClickGUI между открытиями экрана.
 * Сохранение/загрузка делегируется ConfigManager.
 */
public class PanelPositionManager {

    public record PanelPos(int x, int y) {}

    private static final Map<String, PanelPos> positions = new HashMap<>();

    public static void set(String categoryName, int x, int y) {
        positions.put(categoryName, new PanelPos(x, y));
    }

    public static PanelPos get(String categoryName) {
        return positions.get(categoryName);
    }

    public static Map<String, PanelPos> getAll() {
        return positions;
    }

    public static void setAll(Map<String, PanelPos> data) {
        positions.clear();
        positions.putAll(data);
    }
}
