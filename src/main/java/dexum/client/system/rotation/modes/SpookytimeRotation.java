package dexum.client.system.rotation.modes;

import dexum.client.system.rotation.Rotation;
import dexum.client.system.rotation.RotationBase;
import dexum.client.system.rotation.RotationComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SpookytimeRotation extends RotationBase {

    private float currentYaw, currentPitch;
    private boolean isInitialized = false;

    private float lastStepYaw = 0f, lastStepPitch = 0f;

    private Vec3 aimPoint = null;
    private Vec3 aimVelocity = Vec3.ZERO;
    private static final double AIM_FRICTION = 0.85;
    private static final double AIM_FORCE    = 0.025;
    private int aimJitterTimer = 0;

    private int reactionDelay = 0;
    private double currentSpeed = 0.0;
    private double targetSpeed  = 0.0;
    private static final double MAX_SPEED = 3.2;
    private static final double ACCEL     = 0.4;
    private static final double DECEL     = 0.35;
    private int speedChangeTimer = 0;

    private float overshootYaw = 0f, overshootPitch = 0f;
    private int overshootTicks = 0;

    private double noiseTime = 0.0;
    private final double noiseSeedX = rng.nextDouble() * 100.0;
    private final double noiseSeedY = rng.nextDouble() * 100.0;

    private float gcdNoiseYaw = 0f, gcdNoisePitch = 0f;

    @Override
    public void update(Rotation targetAngle, boolean elytraVisual) {
        if (!isInitialized) {
            this.currentYaw = 360f; this.currentPitch = 0f;
            this.isInitialized = true;
        }

        if (reactionDelay > 0) {
            reactionDelay--;
            float microShake = (rng.nextFloat() - 0.5f) * 0.03f;
            this.currentYaw = Mth.wrapDegrees(this.currentYaw + microShake);
            return;
        }

        // aimPoint инициализируется при вызове из Aura через setTargetBox()
        if (aimPoint == null) {
            aimPoint = Vec3.ZERO.add(0, 0, 0);
        }

        aimJitterTimer++;
        if (aimJitterTimer > 8 + rng.nextInt(10)) {
            aimJitterTimer = 0;
            aimVelocity = new Vec3(
                    (rng.nextDouble() - 0.5) * AIM_FORCE * 2.0,
                    (rng.nextDouble() - 0.5) * AIM_FORCE * 1.5,
                    (rng.nextDouble() - 0.5) * AIM_FORCE * 2.0
            );
        } else {
            aimVelocity = aimVelocity.scale(AIM_FRICTION);
        }
        aimPoint = aimPoint.add(aimVelocity);

        float targetYaw   = Mth.wrapDegrees(targetAngle.getYaw());
        float targetPitch = Mth.clamp(targetAngle.getPitch(), -90f, 90f);

        if (overshootTicks > 0) {
            targetYaw   = Mth.wrapDegrees(targetYaw + overshootYaw);
            targetPitch = Mth.clamp(targetPitch + overshootPitch, -90f, 90f);
            overshootTicks--;
        }

        float errorYaw   = Mth.wrapDegrees(targetYaw - this.currentYaw);
        float errorPitch = targetPitch - this.currentPitch;
        double totalError = Math.sqrt(errorYaw * errorYaw + errorPitch * errorPitch);

        double desiredMaxSpeed = totalError > 6.0
                ? 1.8 + Math.min(1.0, totalError / 6.0) * 1.5 + rng.nextDouble() * 0.8
                : 1.0 + rng.nextDouble() * 1.5;
        desiredMaxSpeed = Mth.clamp(desiredMaxSpeed, 0.8, MAX_SPEED);
        if (totalError < 3.0) desiredMaxSpeed *= totalError / 3.0 * 0.7 + 0.3;

        speedChangeTimer--;
        if (speedChangeTimer <= 0) {
            speedChangeTimer = 8 + rng.nextInt(15);
            targetSpeed = desiredMaxSpeed * (0.7 + rng.nextDouble() * 0.6);
        }

        if (currentSpeed < targetSpeed) {
            currentSpeed += ACCEL;
            if (currentSpeed > targetSpeed) currentSpeed = targetSpeed;
        } else {
            currentSpeed -= DECEL;
            if (currentSpeed < targetSpeed) currentSpeed = targetSpeed;
        }
        currentSpeed = Mth.clamp(currentSpeed, 0.0, MAX_SPEED);

        double stepScale = Math.min(1.0, currentSpeed / Math.max(totalError, 0.01));
        double rawStepYaw   = errorYaw   * stepScale;
        double rawStepPitch = errorPitch * stepScale;

        float smoothYaw   = (float)(rawStepYaw   * 0.7 + lastStepYaw   * 0.3);
        float smoothPitch = (float)(rawStepPitch * 0.7 + lastStepPitch * 0.3);
        lastStepYaw   = smoothYaw;
        lastStepPitch = smoothPitch;

        float gcd = Rotation.gcd();
        int mX = Math.round((smoothYaw   + gcdNoiseYaw)   / gcd);
        int mY = Math.round((smoothPitch + gcdNoisePitch) / gcd);
        if (mX == 0 && mY == 0) return;

        gcdNoiseYaw   = Mth.clamp(gcdNoiseYaw   + (rng.nextFloat() - 0.5f) * 0.02f, -0.10f, 0.10f);
        gcdNoisePitch = Mth.clamp(gcdNoisePitch + (rng.nextFloat() - 0.5f) * 0.02f, -0.08f, 0.08f);

        float steppedYaw   = this.currentYaw   + mX * gcd;
        float steppedPitch = Mth.clamp(this.currentPitch + mY * gcd, -90f, 90f);

        noiseTime += 0.01 + rng.nextDouble() * 0.005;
        double n1 = noiseTime * 0.7 + noiseSeedX;
        double n2 = noiseTime * 0.9 + noiseSeedY;
        float shakeYaw   = (float)((smoothNoise(n1, n2) * 2.0 - 1.0) * 0.04);
        float shakePitch = (float)((smoothNoise(n2, n1) * 2.0 - 1.0) * 0.03);

        float finalYaw   = Mth.wrapDegrees(steppedYaw + shakeYaw);
        float finalPitch = Mth.clamp(steppedPitch + shakePitch, -90f, 90f);

        if (totalError > 6.0 && rng.nextFloat() < 0.03 && overshootTicks == 0) {
            overshootYaw   = (rng.nextFloat() - 0.5f) * 1.2f;
            overshootPitch = (rng.nextFloat() - 0.5f) * 0.8f;
            overshootTicks = 2 + rng.nextInt(3);
        }

        Rotation finalRot = new Rotation(finalYaw, finalPitch);
        float visualSpeed = 25f + (float)totalError * 0.8f + rng.nextFloat() * 5f;
        visualSpeed = Mth.clamp(visualSpeed, 25f, 50f);
        RotationComponent.update(finalRot, visualSpeed, visualSpeed, visualSpeed, visualSpeed, 0, 1, elytraVisual);

        this.currentYaw   = finalYaw;
        this.currentPitch = finalPitch;
        this.lastYaw      = finalYaw;
        this.lastPitch    = finalPitch;
    }

    public void onTargetChange() {
        reactionDelay = 2 + rng.nextInt(4);
        currentSpeed = 0.0;
        targetSpeed = 0.0;
        speedChangeTimer = 10 + rng.nextInt(15);
        overshootYaw = 0f; overshootPitch = 0f;
        overshootTicks = 0;
        lastStepYaw = 0f; lastStepPitch = 0f;
        aimPoint = null;
        aimVelocity = Vec3.ZERO;
    }

    private double noise(double x, double y) {
        double n = Math.sin(x * 12.9898 + y * 78.233) * 43758.5453;
        return n - Math.floor(n);
    }

    private double smoothNoise(double x, double y) {
        int ix = (int) Math.floor(x), iy = (int) Math.floor(y);
        double fx = x - ix, fy = y - iy;
        double sx = fx * fx * (3.0 - 2.0 * fx);
        double sy = fy * fy * (3.0 - 2.0 * fy);
        return (1 - sy) * ((1 - sx) * noise(ix, iy) + sx * noise(ix + 1, iy))
             + sy * ((1 - sx) * noise(ix, iy + 1) + sx * noise(ix + 1, iy + 1));
    }
}
