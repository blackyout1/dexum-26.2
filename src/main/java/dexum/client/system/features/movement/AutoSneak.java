package dexum.client.system.features.movement;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.UpdateEvent;
import dexum.client.system.settings.impl.BooleanSetting;
import net.minecraft.core.BlockPos;

@NewFunction(name = "AutoSneak", desc = "Автоматически приседает у края блоков", category = Category.MOVEMENT)
public class AutoSneak extends Function {

    private final BooleanSetting onlyMoving = new BooleanSetting("Только при движении", true);

    public AutoSneak() {
        addSettings(onlyMoving);
    }

    @Override
    public void onDisable() {
        if (!nullCheck.player()) {
            mc.options.keyShift.setDown(false);
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (nullCheck.all()) return;
        if (!mc.player.onGround()) return;

        if (onlyMoving.isEnabled()) {
            boolean moving = mc.options.keyUp.isDown()
                    || mc.options.keyDown.isDown()
                    || mc.options.keyLeft.isDown()
                    || mc.options.keyRight.isDown();
            if (!moving) {
                mc.options.keyShift.setDown(false);
                return;
            }
        }

        boolean edgeDetected = isNearEdge();
        mc.options.keyShift.setDown(edgeDetected);
    }

    private boolean isNearEdge() {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        float yaw = mc.player.getYRot();
        double yawRad = Math.toRadians(yaw);

        double dx = 0, dz = 0;
        if (mc.options.keyUp.isDown())    { dx += -Math.sin(yawRad); dz +=  Math.cos(yawRad); }
        if (mc.options.keyDown.isDown())  { dx +=  Math.sin(yawRad); dz += -Math.cos(yawRad); }
        if (mc.options.keyLeft.isDown())  { dx += -Math.cos(yawRad); dz += -Math.sin(yawRad); }
        if (mc.options.keyRight.isDown()) { dx +=  Math.cos(yawRad); dz +=  Math.sin(yawRad); }

        if (dx == 0 && dz == 0) return false;

        double len = Math.sqrt(dx * dx + dz * dz);
        dx = dx / len * 0.55;
        dz = dz / len * 0.55;

        BlockPos checkPos = BlockPos.containing(x + dx, y - 0.1, z + dz);
        boolean groundAhead = mc.level.getBlockState(checkPos).isSolidRender();

        return !groundAhead;
    }
}
