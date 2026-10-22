package dexum.client.utils.helpers.movement;

import net.minecraft.util.Mth;
import dexum.client.system.events.impl.InputEvent;

public class MoveCorrection {

    public static void applySilentMoveFix(InputEvent event, float clientYaw, float auraYaw) {
        float forward = event.getMovementForward();
        float strafe  = event.getMovementStrafe();

        if (forward == 0.0f && strafe == 0.0f) return;

        float delta = Mth.wrapDegrees(auraYaw - clientYaw);
        if (Math.abs(delta) < 0.01f) return;

        float rad = delta * Mth.DEG_TO_RAD;
        float cos = Mth.cos(rad);
        float sin = Mth.sin(rad);

        float correctedForward = forward * cos - strafe * sin;
        float correctedStrafe  = strafe  * cos + forward * sin;

        correctedForward = Mth.clamp(correctedForward, -1.0f, 1.0f);
        correctedStrafe  = Mth.clamp(correctedStrafe,  -1.0f, 1.0f);

        event.setMovementForward(correctedForward);
        event.setMovementStrafe(correctedStrafe);
    }
}
