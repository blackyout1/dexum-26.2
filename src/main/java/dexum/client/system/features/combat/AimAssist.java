package dexum.client.system.features.combat;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.LookEvent;
import dexum.client.system.settings.impl.BooleanSetting;
import dexum.client.system.settings.impl.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@NewFunction(name = "AimAssist", desc = "Плавно тянет прицел к цели при движении мыши", category = Category.COMBAT)
public class AimAssist extends Function {

    private final NumberSetting range    = new NumberSetting("Дальность", 4.5, 1.0, 8.0,   0.1);
    private final NumberSetting fov      = new NumberSetting("FOV",       60.0, 5.0, 180.0, 5.0);
    private final NumberSetting strength = new NumberSetting("Сила",       0.6,  0.0, 1.0,  0.05);
    private final BooleanSetting players = new BooleanSetting("Игроки",   true);
    private final BooleanSetting mobs    = new BooleanSetting("Мобы",     false);
    private final BooleanSetting animals = new BooleanSetting("Животные", false);

    private LivingEntity target = null;

    // Для плавного шума — сохраняем предыдущий случайный offset чтобы не дёргало
    private float noiseBias = 0f;

    public AimAssist() {
        addSettings(range, fov, strength, players, mobs, animals);
    }

    @EventHandler
    public void onLook(LookEvent event) {
        if (nullCheck.all()) { target = null; return; }

        // Обновляем цель в том же событии — атомарно с изменением угла
        refreshTarget();
        if (target == null) return;

        double deltaX = event.getYaw();
        double deltaY = event.getPitch();
        if (deltaX == 0.0 && deltaY == 0.0) return;

        ThreadLocalRandom rng = ThreadLocalRandom.current();

        // ── Случайный пропуск ─────────────────────────────────────────────
        // ~20% тиков полностью пропускаем — паттерн становится нерегулярным
        if (rng.nextDouble() < 0.20) return;

        // ── Направление к цели ────────────────────────────────────────────
        Vec3 eye    = mc.player.getEyePosition();
        Vec3 center = target.getBoundingBox().getCenter();
        double dx = center.x - eye.x;
        double dy = center.y - eye.y;
        double dz = center.z - eye.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);

        float targetYaw   = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, hDist)));
        float yawDiff     = Mth.wrapDegrees(targetYaw - mc.player.getYRot());
        float pitchDiff   = targetPitch - mc.player.getXRot();

        // ── Сила по yaw ───────────────────────────────────────────────────
        float str = (float) strength.getValue();

        boolean toward = deltaX * yawDiff > 0.0;
        float mult = toward ? 1.35f : 0.65f;

        // Ослабляем при быстром движении
        float speed = (float) Math.abs(deltaX);
        float blend = Mth.clamp(1.0f - speed / 55.0f, 0.0f, 1.0f);
        mult = Mth.lerp(blend, 1.0f, mult);

        // Применяем силу с небольшим случайным шумом ±8%
        float noise = (float)(rng.nextDouble(-0.08, 0.08));
        mult = Mth.lerp(str, 1.0f, mult) + noise;

        // Плавный шум bias — медленно дрейфует, не прыгает
        noiseBias = noiseBias * 0.85f + (float)(rng.nextDouble(-0.04, 0.04)) * 0.15f;
        mult += noiseBias;

        float newDeltaX = applyGCD((float)(deltaX * mult), (float) deltaX);

        // ── Лёгкая коррекция pitch ────────────────────────────────────────
        // Реальный игрок двигает мышь в обоих осях — добавляем мягкое притяжение
        // по вертикали, но только если мышь уже движется по Y
        float newDeltaY = (float) deltaY;
        if (Math.abs(pitchDiff) > 1.5f && Math.abs(deltaY) > 0.01) {
            boolean pitchToward = deltaY * pitchDiff > 0.0;
            float pitchMult = pitchToward ? 1.2f : 0.8f;
            // Pitch ещё слабее чем yaw + больше шума
            pitchMult = Mth.lerp(str * 0.5f, 1.0f, pitchMult);
            pitchMult += (float)(rng.nextDouble(-0.06, 0.06));
            newDeltaY = applyGCD((float)(deltaY * pitchMult), (float) deltaY);
        }

        event.setYaw(newDeltaX);
        event.setPitch(newDeltaY);
    }

    // ── Обновление цели ────────────────────────────────────────────────────

    private void refreshTarget() {
        double r = range.getValue();
        if (target != null && target.isAlive() && !target.isDeadOrDying()
                && mc.player.distanceTo(target) <= r
                && isValid(target)
                && getAngleTo(target) <= fov.getValue()) {
            return; // текущая цель ещё валидна
        }
        target = findTarget(r);
    }

    private LivingEntity findTarget(double r) {
        AABB box = mc.player.getBoundingBox().inflate(r);
        List<LivingEntity> list = mc.level.getEntitiesOfClass(
                LivingEntity.class, box,
                e -> e != mc.player
                        && e.isAlive()
                        && !e.isDeadOrDying()
                        && isValid(e)
                        && mc.player.distanceTo(e) <= r
                        && getAngleTo(e) <= fov.getValue()
        );
        return list.stream()
                .min(Comparator.comparingDouble(this::getAngleTo))
                .orElse(null);
    }

    private double getAngleTo(LivingEntity e) {
        Vec3 eye    = mc.player.getEyePosition();
        Vec3 center = e.getBoundingBox().getCenter();
        double dx = center.x - eye.x;
        double dy = center.y - eye.y;
        double dz = center.z - eye.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);
        float tYaw   = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float tPitch = (float)(-Math.toDegrees(Math.atan2(dy, hDist)));
        float dYaw   = Mth.wrapDegrees(tYaw - mc.player.getYRot());
        float dPitch = tPitch - mc.player.getXRot();
        return Math.sqrt(dYaw * dYaw + dPitch * dPitch);
    }

    private boolean isValid(LivingEntity e) {
        if (e instanceof Player p) {
            if (p == mc.player) return false;
            return players.isEnabled();
        }
        if (e instanceof Monster) return mobs.isEnabled();
        if (e instanceof Animal)  return animals.isEnabled();
        return false;
    }

    // ── GCD ────────────────────────────────────────────────────────────────

    private static float applyGCD(float angle, float previous) {
        float gcd = getGCD();
        if (gcd < 1e-6f) return angle;
        return previous + Math.round((angle - previous) / gcd) * gcd;
    }

    private static float getGCD() {
        double sens = Minecraft.getInstance().options.sensitivity().get();
        double f = sens * 0.6 + 0.2;
        return (float)(f * f * f * 8.0 * 0.15);
    }
}
