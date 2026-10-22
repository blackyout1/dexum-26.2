package dexum.client.utils.render.msdf;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class MsdfManager {

    private static final Map<String, MsdfFont> fonts = new HashMap<>();
    private static MsdfFont defaultFont;
    private static boolean initialized = false;

    public static MsdfFont MONTSERRAT;
    public static MsdfFont ICONS;
    public static MsdfFont BOLD;
    public static MsdfFont ICONS2;

    public static void init() {
        if (initialized)
            return;

        System.out.println("[MSDF] Initializing MsdfManager...");
        Msdf.MONTSERRAT = MONTSERRAT = register("montserrat", "montserrat.png", "montserrat.json");
        Msdf.BOLD = BOLD = register("bold", "bold.png", "bold.json");
        Msdf.ICONS = ICONS = register("icons", "icons.png", "icons.json");
        Msdf.ICONS2 = ICONS2 = register("icons2", "icons2.png", "icons2.json");

        initialized = true;
    }

    public static MsdfFont register(String name, String atlasPath, String jsonPath) {
        MsdfFont font = new MsdfFont(name);
        font.load(Identifier.fromNamespaceAndPath("dexum", atlasPath), Identifier.fromNamespaceAndPath("dexum", jsonPath));
        fonts.put(name, font);
        if (defaultFont == null)
            defaultFont = font;
        return font;
    }

    public static MsdfFont get(String name) {
        return fonts.get(name);
    }

    public static MsdfFont getDefault() {
        return defaultFont;
    }

    public static void setDefault(String name) {
        MsdfFont font = fonts.get(name);
        if (font != null)
            defaultFont = font;
    }

    public static void shutdown() {
        fonts.values().forEach(MsdfFont::shutdown);
        fonts.clear();
        defaultFont = null;
        initialized = false;
    }
}

