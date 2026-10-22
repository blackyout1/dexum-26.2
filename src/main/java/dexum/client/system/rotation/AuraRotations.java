package dexum.client.system.rotation;

import dexum.client.utils.helpers.combat.AuraUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class AuraRotations {

    private AuraRotations() {}

    public static Rotation calculateTo(LivingEntity target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return new Rotation();

        Vec3 eye    = mc.player.getEyePosition();
        Vec3 aimVec = AuraUtil.getBestAimPoint(eye, target, 6.0, true);
        Vec3 dir    = aimVec.equals(Vec3.ZERO)
                ? target.getBoundingBox().getCenter().subtract(eye)
                : aimVec;

        float yaw   = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dir.y, Math.hypot(dir.x, dir.z))));

        return new Rotation(Mth.wrapDegrees(yaw), Mth.clamp(pitch, -90f, 90f)).normalize();
    }

    public static Rotation smooth(Rotation current, Rotation target, float speed, float randomness) {
        float noise = randomness > 0
                ? (float)(Math.random() * randomness * 2 - randomness)
                : 0f;

        Rotation lerped = Rotation.lerp(current, target, Mth.clamp(speed, 0.01f, 1f));
        return new Rotation(
                Mth.wrapDegrees(lerped.getYaw() + noise),
                lerped.getPitch()
        ).normalize();
    }

    public static float angleTo(LivingEntity target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 180f;

        Rotation to    = calculateTo(target);
        float dyaw     = Math.abs(Mth.wrapDegrees(to.getYaw()   - mc.player.getYRot()));
        float dpitch   = Math.abs(to.getPitch() - mc.player.getXRot());
        return (float) Math.sqrt(dyaw * dyaw + dpitch * dpitch);
    }
}
