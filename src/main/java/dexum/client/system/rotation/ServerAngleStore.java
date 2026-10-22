package dexum.client.system.rotation;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class ServerAngleStore {

    public static final ServerAngleStore INSTANCE = new ServerAngleStore();

    public static final float MAX_DELTA_YAW   = 55.0f;
    public static final float MAX_DELTA_PITCH = 22.0f;

    private float serverYaw;
    private float serverPitch;
    private boolean active = false;
    private int reactionTicks = 0;

    private ServerAngleStore() {}

    public void set(float targetYaw, float targetPitch) {
        if (reactionTicks > 0) return;

        float gcd = computeGcd();
        if (gcd <= 0f) {
            serverYaw   = Mth.wrapDegrees(targetYaw);
            serverPitch = Mth.clamp(targetPitch, -90f, 90f);
            active = true;
            return;
        }

        float dYaw   = Mth.wrapDegrees(targetYaw   - serverYaw);
        float dPitch = targetPitch - serverPitch;

        dYaw   = Mth.clamp(dYaw,   -MAX_DELTA_YAW,   MAX_DELTA_YAW);
        dPitch = Mth.clamp(dPitch, -MAX_DELTA_PITCH, MAX_DELTA_PITCH);

        int stepsYaw   = Math.round(dYaw   / gcd);
        int stepsPitch = Math.round(dPitch / gcd);

        serverYaw   = Mth.wrapDegrees(serverYaw   + stepsYaw   * gcd);
        serverPitch = Mth.clamp(serverPitch + stepsPitch * gcd, -90f, 90f);

        active = true;
    }

    public void clear() {
        active = false;
    }

    public void tick() {
        if (reactionTicks > 0) reactionTicks--;
    }

    public void setReactionDelay(int ticks) {
        this.reactionTicks = Math.max(0, ticks);
    }

    public void syncFromPlayer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            serverYaw   = mc.player.getYRot();
            serverPitch = mc.player.getXRot();
        }
    }

    public float  getServerYaw()     { return serverYaw; }
    public float  getServerPitch()   { return serverPitch; }
    public boolean isActive()        { return active; }
    public int    getReactionTicks() { return reactionTicks; }

    public static float computeGcd() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options == null) return 0f;
        double sens = mc.options.sensitivity().get();
        double f    = sens * 0.6 + 0.2;
        return (float)(f * f * f * 8.0 * 0.15);
    }
}
