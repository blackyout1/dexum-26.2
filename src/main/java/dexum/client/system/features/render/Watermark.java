package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventBus;
import dexum.client.system.hud.HudElementManager;
import dexum.client.system.hud.ModuleList;
import dexum.client.system.hud.WatermarkHud;

@NewFunction(name = "Watermark", desc = "HUD ватермарка в стиле Rich", category = Category.RENDER)
public class Watermark extends Function {

    private WatermarkHud watermarkHud;
    private ModuleList moduleList;
    private boolean hudInitialized = false;

    @Override 
    public void onEnable() {
        // Initialize HUD system
        if (!hudInitialized) {
            HudElementManager.initialize();
            watermarkHud = new WatermarkHud();
            moduleList = new ModuleList();
            HudElementManager.register(watermarkHud);
            HudElementManager.register(moduleList);
            hudInitialized = true;
        }
    }

    @Override 
    public void onDisable() {
        // Keep HUD elements registered for other features to use
    }

    // Position getters/setters for ConfigManager compatibility
    public float getPosX() {
        return watermarkHud != null ? watermarkHud.getX() : 10.0f;
    }

    public float getPosY() {
        return watermarkHud != null ? watermarkHud.getY() : 10.0f;
    }

    public void setPosX(float x) {
        if (watermarkHud != null) watermarkHud.setX(x);
    }

    public void setPosY(float y) {
        if (watermarkHud != null) watermarkHud.setY(y);
    }
}
