package dexum.client.system.rotation.modes;

import dexum.client.system.rotation.Rotation;
import dexum.client.system.rotation.RotationBase;
import dexum.client.system.rotation.RotationComponent;
import net.minecraft.util.Mth;

public class IkedaRotation extends RotationBase {

    private static final double R = 0.918, C1 = 0.4, C2 = 6.0;

    private double yawX = 0.1, yawY = 0.1;
    private double pitchX = 0.7, pitchY = -0.3;

    private static final float YAW_TREMOR   = 0.05f;
    private static final float PITCH_TREMOR = 0.02f;

    private float velYaw = 0f, velPitch = 0f;

    private static final float FRICTION_FAR  = 0.55f;
    private static final float FRICTION_NEAR = 0.88f;
    private static final float ACCEL_FAR     = 0.55f;
    private static final float ACCEL_NEAR    = 0.20f;
    private static final float MAX_SPEED     = 9.0f;

    private static final float MAX_YAW   = 22.0f;
    private static final float MAX_PITCH = 10.0f;

    private static final float PITCH_DEADZONE = 0.8f;

    private float overshootYaw = 0f, overshootPitch = 0f;
    private int overshootTicks = 0;

    private float spasmYawBias = 0f, spasmPitchBias = 0f;
    private int spasmTicks = 0;

    private float gcdNoiseYaw = 0f, gcdNoisePitch = 0f;

    private float currentYaw, currentPitch;
    private boolean initialized = false;
    private int reactionDelay = 0;

    @Override
    public void update(Rotation targetAngle, boolean elytraVisual) {
        if (!initialized) {
            currentYaw = 0; currentPitch = 0;
            initialized = true;
        }
        if (reactionDelay > 0) { reactionDelay--; return; }

        float idealYaw   = targetAngle.getYaw();
        float idealPitch = targetAngle.getPitch();

        if (overshootTicks > 0) {
            idealYaw   += overshootYaw;
            idealPitch += overshootPitch;
            overshootTicks--;
        }

        float errYaw   = Mth.wrapDegrees(idealYaw - currentYaw);
        float errPitch = idealPitch - currentPitch;
        float totalErr = (float) Math.hypot(errYaw, errPitch);

        // Адаптивное трение — ближе цель, сильнее торможение
        float dist = totalErr;
        float t = Mth.clamp((dist - 3.0f) / 4.0f, 0f, 1f);
        float friction = FRICTION_NEAR + (FRICTION_FAR - FRICTION_NEAR) * t;
        float accel    = ACCEL_NEAR    + (ACCEL_FAR    - ACCEL_NEAR)    * t;

        // Икеда-шаг
        double[] ys = stepIkeda(yawX, yawY);
        double[] ps = stepIkeda(pitchX, pitchY);
        yawX = ys[0]; yawY = ys[1];
        pitchX = ps[0]; pitchY = ps[1];

        float noiseY = (float)(yawX * YAW_TREMOR);
        float noiseP = (float)(pitchX * PITCH_TREMOR);

        float speed = MAX_SPEED;
        float desY = Mth.clamp(errYaw   * 0.5f,  -speed,        speed);
        float desP = Mth.clamp(errPitch * 0.35f, -speed * 0.6f, speed * 0.6f);

        velYaw   = velYaw   * friction + desY * (1f - friction) * accel;
        velPitch = velPitch * friction + desP * (1f - friction) * accel;

        if (Math.abs(errPitch) < PITCH_DEADZONE) velPitch = 0f;

        float dY = Mth.clamp(velYaw   + noiseY, -MAX_YAW,   MAX_YAW);
        float dP = Mth.clamp(velPitch + noiseP, -MAX_PITCH, MAX_PITCH);

        if (spasmTicks > 0) {
            float fade = (float) spasmTicks / 3f;
            dY += spasmYawBias   * fade;
            dP += spasmPitchBias * fade;
            spasmTicks--;
        }

        float gcd = Rotation.gcd();
        if (gcd <= 0f) return;
        int mX = Math.round((dY + gcdNoiseYaw)   / gcd);
        int mY = Math.round((dP + gcdNoisePitch) / gcd);
        if (mX == 0 && mY == 0) return;

        currentYaw   = Mth.wrapDegrees(currentYaw + mX * gcd);
        currentPitch = Mth.clamp(currentPitch + mY * gcd, -90f, 90f);
        lastYaw      = currentYaw;
        lastPitch    = currentPitch;

        gcdNoiseYaw   = Mth.clamp(gcdNoiseYaw   + (rng.nextFloat() - 0.5f) * 0.015f, -0.08f, 0.08f);
        gcdNoisePitch = Mth.clamp(gcdNoisePitch + (rng.nextFloat() - 0.5f) * 0.015f, -0.06f, 0.06f);

        Rotation finalRot = new Rotation(currentYaw, currentPitch);
        RotationComponent.update(finalRot, 360f, 360f, 360f, 360f, 0, 1, elytraVisual);

        if (overshootTicks == 0 && totalErr > 6f && rng.nextFloat() < 0.03f) {
            overshootYaw   = (rng.nextFloat() - 0.5f) * 1.2f;
            overshootPitch = (rng.nextFloat() - 0.5f) * 0.8f;
            overshootTicks = 2 + rng.nextInt(3);
        }
    }

    @Override
    public void onTargetChange() {
        reactionDelay = 2 + rng.nextInt(4);
        velYaw = 0f; velPitch = 0f;
        yawX = 0.1; yawY = 0.1;
        pitchX = 0.7; pitchY = -0.3;
        overshootTicks = 0;
        spasmTicks = 0;
    }

    @Override
    public void onAttack() {
        yawX += 0.63; yawY -= 0.28;
        pitchX -= 0.41; pitchY += 0.19;
        spasmYawBias   = (rng.nextFloat() - 0.5f) * 0.5f;
        spasmPitchBias = -0.20f - rng.nextFloat() * 0.10f;
        spasmTicks = 3;
        velPitch -= 0.06f;
    }

    private double[] stepIkeda(double x, double y) {
        double t = C1 - (C2 / (1.0 + x * x + y * y));
        double c = Math.cos(t), s = Math.sin(t);
        return new double[]{
                1.0 + R * (x * c - y * s),
                      R * (x * s + y * c)
        };
    }
}
