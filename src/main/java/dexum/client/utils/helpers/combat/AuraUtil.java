package dexum.client.utils.helpers.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AuraUtil {

    private static final Minecraft mc = Minecraft.getInstance();

    private AuraUtil() {}

    public static double getDistanceSq(Vec3 eye, LivingEntity entity) {
        AABB box = entity.getBoundingBox();
        double cx = Mth.clamp(eye.x, box.minX, box.maxX);
        double cy = Mth.clamp(eye.y, box.minY, box.maxY);
        double cz = Mth.clamp(eye.z, box.minZ, box.maxZ);
        double dx = cx - eye.x, dy = cy - eye.y, dz = cz - eye.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public static double getDistanceSq(LivingEntity entity) {
        if (mc.player == null) return Double.POSITIVE_INFINITY;
        return getDistanceSq(mc.player.getEyePosition(), entity);
    }

    public static boolean isInReach(LivingEntity entity, double maxReach) {
        return getDistanceSq(entity) <= maxReach * maxReach;
    }

    public static boolean canHitWithRotation(Vec3 eye, float yaw, float pitch,
                                              double distance, LivingEntity entity,
                                              boolean throughWalls) {
        if (mc.level == null || mc.player == null) return false;

        Vec3 dir = Vec3.directionFromRotation(pitch, yaw).scale(distance);
        java.util.Optional<Vec3> opt = entity.getBoundingBox().contains(eye)
                ? java.util.Optional.of(eye)
                : entity.getBoundingBox().clip(eye, eye.add(dir));

        if (opt.isEmpty()) return false;

        if (!throughWalls) {
            HitResult hit = mc.level.clip(new ClipContext(
                    eye, opt.get(),
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    mc.player));
            if (hit.getType() != HitResult.Type.MISS) return false;
        }
        return true;
    }

    public static boolean canHitWithRotation(float yaw, float pitch, double distance,
                                              LivingEntity entity, boolean throughWalls) {
        if (mc.player == null) return false;
        return canHitWithRotation(mc.player.getEyePosition(), yaw, pitch, distance, entity, throughWalls);
    }

    public static boolean canSeeEntity(Vec3 from, LivingEntity entity, double reach) {
        if (mc.level == null || mc.player == null) return false;

        AABB bb = entity.getBoundingBox();
        double[] t = {0, .125, .25, .375, .5, .625, .75, .875, 1};
        int last = t.length - 1;
        double reachSq = reach * reach;

        for (int a = 0; a <= last; a++) {
            for (int b = 0; b <= last; b++) {
                for (int c = 0; c <= last; c++) {
                    if (a > 0 && a < last && b > 0 && b < last && c > 0 && c < last) continue;
                    Vec3 p = new Vec3(
                            Mth.lerp(t[a], bb.minX, bb.maxX),
                            Mth.lerp(t[b], bb.minY, bb.maxY),
                            Mth.lerp(t[c], bb.minZ, bb.maxZ));
                    if (from.distanceToSqr(p) > reachSq) continue;
                    Vec3 end = p.add(from.subtract(p).scale(0.05 / Math.sqrt(from.distanceToSqr(p))));
                    if (mc.level.clip(new ClipContext(from, end,
                            ClipContext.Block.OUTLINE,
                            ClipContext.Fluid.NONE,
                            mc.player)).getType() == HitResult.Type.MISS) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Vec3 getBestAimPoint(Vec3 eye, LivingEntity target,
                                        double reach, boolean throughWalls) {
        if (mc.level == null || mc.player == null) return Vec3.ZERO;

        AABB bb = target.getBoundingBox();
        double mx = (bb.minX + bb.maxX) * 0.5;
        double mz = (bb.minZ + bb.maxZ) * 0.5;

        Vec3 targetEye = new Vec3(target.getX(), target.getY(), target.getZ()).add(0, target.getEyeHeight(), 0);
        double dist = eye.distanceTo(targetEye);

        double ay = Mth.lerp(
                Mth.clamp(dist / 3.0, 0.0, 1.0),
                bb.minY,
                Mth.clamp(eye.y, bb.minY, bb.maxY));

        Vec3 ideal = new Vec3(mx, ay, mz);

        double[] t = {0, .125, .25, .375, .5, .625, .75, .875, 1};
        int last = t.length - 1;
        List<Vec3> pts = new ArrayList<>();
        pts.add(ideal);
        for (int a = 0; a < t.length; a++) {
            for (int b = 0; b < t.length; b++) {
                for (int c = 0; c < t.length; c++) {
                    if (a > 0 && a < last && b > 0 && b < last && c > 0 && c < last) continue;
                    pts.add(new Vec3(
                            Mth.lerp(t[a], bb.minX, bb.maxX),
                            Mth.lerp(t[b], bb.minY, bb.maxY),
                            Mth.lerp(t[c], bb.minZ, bb.maxZ)));
                }
            }
        }

        for (double pad : new double[]{0.0, 0.2}) {
            List<Vec3> visible = filterVisible(pts, eye, target, reach + pad, false);
            if (!visible.isEmpty()) return centroidNearest(visible).subtract(eye);

            if (throughWalls) {
                List<Vec3> through = filterVisible(pts, eye, target, reach + pad, true);
                if (!through.isEmpty()) return centroidNearest(through).subtract(eye);
            }
        }
        return Vec3.ZERO;
    }

    private static List<Vec3> filterVisible(List<Vec3> pts, Vec3 eye,
                                             LivingEntity target, double reach, boolean throughWalls) {
        List<Vec3> result = new ArrayList<>();
        for (Vec3 p : pts) {
            Vec3 d = p.subtract(eye);
            double len = d.length();
            if (len > reach) continue;
            float yaw   = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0);
            float pitch = (float) (-Math.toDegrees(Math.atan2(d.y, Math.hypot(d.x, d.z))));
            if (canHitWithRotation(eye, yaw, pitch, (float) reach, target, throughWalls)) result.add(p);
        }
        return result;
    }

    private static Vec3 centroidNearest(List<Vec3> pts) {
        Vec3 centroid = pts.stream().reduce(Vec3.ZERO, Vec3::add).scale(1.0 / pts.size());
        return pts.stream().min(Comparator.comparingDouble(p -> p.distanceToSqr(centroid))).orElse(centroid);
    }

    public static boolean canDoCriticals() {
        if (mc.player == null) return false;
        Level world = mc.player.level();
        if (world == null) return false;
        BlockPos eye = BlockPos.containing(mc.player.getEyePosition());
        FluidState fluid = world.getFluidState(eye);

        return !(mc.player.hasEffect(MobEffects.LEVITATION)
                || mc.player.hasEffect(MobEffects.BLINDNESS)
                || fluid.is(FluidTags.WATER)
                || fluid.is(FluidTags.LAVA)
                || mc.player.getAbilities().flying
                || mc.player.isFallFlying()
                || mc.player.onClimbable()
                || mc.player.isPassenger());
    }

    public static boolean isCriticalFalling() {
        return mc.player != null
                && canDoCriticals()
                && mc.player.fallDistance > 0f
                && !mc.player.onGround();
    }

    public static float lerpAngle(float start, float end, float amount) {
        float a = Mth.clamp(amount, 0f, 1f);
        float d = Mth.wrapDegrees(end - start);
        if (Math.abs(d) < 0.5f) return end;
        return Mth.wrapDegrees(start + d * a);
    }
}
