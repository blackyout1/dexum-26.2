package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.Render3DEvent;
import dexum.client.system.settings.impl.NumberSetting;
import dexum.client.utils.animation.Animation;
import dexum.client.utils.animation.Easing;
import net.minecraft.world.entity.LivingEntity;

@NewFunction(name = "TargetESP", desc = "Визуализация цели (3D)", category = Category.RENDER)
public class TargetESP extends Function {

    private final NumberSetting modeSetting = new NumberSetting("Режим", 1, 0, 2, 1);

    private final Animation anim     = new Animation(400L,  Easing.CUBIC_OUT);
    private final Animation metaAnim = new Animation(1300L, Easing.CIRC_OUT);

    private LivingEntity lastTarget = null;

    public TargetESP() {
        addSettings(modeSetting);
    }

    @Override
    public void onEnable() {
        anim.setValue(0f);
        metaAnim.setValue(0f);
        lastTarget = null;
    }

    @EventHandler
    public void onRender3D(Render3DEvent e) {
        // 3D rendering API changed in 26.2 — stubbed out for compilation
    }
}
