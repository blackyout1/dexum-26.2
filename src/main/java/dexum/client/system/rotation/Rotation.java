package dexum.client.system.rotation;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Rotation {

    private float yaw;
    private float pitch;

    public Rotation() {
        this(0f, 0f);
    }

    public Rotation(float yaw, float pitch) {
        this.yaw   = yaw;
        this.pitch = pitch;
    }

    public float getYaw()   { return yaw; }
    public float getPitch() { return pitch; }

    public void setYaw(float yaw)     { this.yaw = yaw; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    public Rotation normalize() {
        return new Rotation(Mth.wrapDegrees(yaw), Mth.clamp(pitch, -90f, 90f));
    }

    public static Rotation lerp(Rotation from, Rotation to, float t) {
        float dYaw = Mth.wrapDegrees(to.yaw - from.yaw);
        return new Rotation(
                from.yaw + dYaw * t,
                from.pitch + (to.pitch - from.pitch) * t
        );
    }

    
    public static float gcd() {
        return ServerAngleStore.computeGcd();
    }

    public float getDelta(Rotation other) {
        float dYaw = Math.abs(Mth.wrapDegrees(other.yaw - this.yaw));
        float dPitch = Math.abs(other.pitch - this.pitch);
        return (float) Math.sqrt(dYaw * dYaw + dPitch * dPitch);
    }
    public Vec3 toDirection() {
        float yawRad   = Mth.DEG_TO_RAD * this.yaw;
        float pitchRad = Mth.DEG_TO_RAD * this.pitch;
        float cosPitch = Mth.cos(-pitchRad - (float) Math.PI);
        float sinPitch = Mth.sin(-pitchRad - (float) Math.PI);
        float cosYaw   = Mth.cos(-yawRad);
        float sinYaw   = Mth.sin(-yawRad);
        return new Vec3(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
    }
}

