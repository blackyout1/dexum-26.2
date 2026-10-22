package dexum.client.system.rotation.modes;

import dexum.client.system.rotation.Rotation;
import dexum.client.system.rotation.RotationBase;
import dexum.client.system.rotation.RotationComponent;
import net.minecraft.util.Mth;

public class VanillaRotation extends RotationBase {

    @Override
    public void update(Rotation angle, boolean elytraVisual) {
        float dYaw   = Mth.wrapDegrees(angle.getYaw() - lastYaw);
        float dPitch = angle.getPitch() - lastPitch;

        float yaw   = lastYaw + dYaw;
        float pitch = lastPitch + dPitch;

        float gcd = Rotation.gcd();
        yaw   -= (yaw   - lastYaw)   % gcd;
        pitch -= (pitch - lastPitch) % gcd;

        Rotation smoothRot = new Rotation(yaw, pitch);
        RotationComponent.update(smoothRot, 360f, 360f, 360f, 360f, 0, 1, elytraVisual);

        this.lastYaw   = smoothRot.getYaw();
        this.lastPitch = smoothRot.getPitch();
    }
}
