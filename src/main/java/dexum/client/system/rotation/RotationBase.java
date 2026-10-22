package dexum.client.system.rotation;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.security.SecureRandom;

public abstract class RotationBase {

    protected final SecureRandom rng = new SecureRandom();

    protected float lastYaw;
    protected float lastPitch;
    protected boolean noCircling;

    public abstract void update(Rotation targetAngle, boolean elytraVisual);

    public void onTargetChange() {}
    public void onAttack() {}

    public float getYaw()   { return lastYaw; }
    public float getPitch() { return lastPitch; }

    public void setYaw(float yaw)        { this.lastYaw = yaw; }
    public void setPitch(float p)        { this.lastPitch = p; }
    public void setNoCircling(boolean v) { this.noCircling = v; }

    protected static Rotation rotationFromVec(Vec3 vec) {
        float yaw   = (float) Mth.wrapDegrees(
                Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
        float pitch = (float) Mth.wrapDegrees(
                Math.toDegrees(-Math.atan2(vec.y, Math.hypot(vec.x, vec.z))));
        return new Rotation(yaw, pitch);
    }
}
