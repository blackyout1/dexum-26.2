package dexum.client.system.features.combat;

import dexum.client.utils.helpers.movement.MoveCorrection;
import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.InputEvent;
import dexum.client.system.events.impl.UpdateEvent;
import dexum.client.system.rotation.FreeLookComponent;
import dexum.client.system.rotation.Rotation;
import dexum.client.system.rotation.RotationBase;
import dexum.client.system.rotation.RotationComponent;
import dexum.client.system.rotation.RotationRegistry;
import dexum.client.system.settings.impl.BooleanSetting;
import dexum.client.system.settings.impl.MultiSetting;
import dexum.client.system.settings.impl.NumberSetting;
import dexum.client.utils.helpers.combat.AuraUtil;

import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@NewFunction(name = "Aura", desc = "Автоматически атакует ближайшую цель", category = Category.COMBAT)
public class Aura extends Function {

    public static Aura Instance;

    private final BooleanSetting targetPlayers = new BooleanSetting("Игроки", true);
    private final BooleanSetting targetHostile = new BooleanSetting("Враждебные мобы", true);
    private final BooleanSetting targetPassive = new BooleanSetting("Животные", false);

    private final NumberSetting distance      = new NumberSetting("Дистанция", 6.0, 0.5, 6.0, 0.1);
    private final NumberSetting distanceExtra = new NumberSetting("Доп. дистанция", 0.5, 0.0, 6.0, 0.1);

    private final MultiSetting rotationMode   = new MultiSetting("Ротация", buildModeNames());
    private final MultiSetting targetPriority = new MultiSetting("Приоритет", "Прицел", "Дистанция", "ХП");
    private final BooleanSetting critsOnly    = new BooleanSetting("Только критические удары", true);
    private final BooleanSetting smartSprint  = new BooleanSetting("Умный спринт", false);
    private final BooleanSetting shieldBreak  = new BooleanSetting("Ломать щит", true);
    private final BooleanSetting keepTarget   = new BooleanSetting("Удерживать цель", true);
    private final MultiSetting dontAttack     = new MultiSetting("Не бить когда", "Используется предмет", "Открыт контейнер");
    private final MultiSetting correction     = new MultiSetting("Коррекция движения", "Фокус", "Свободная", "Нет");

    private Vec3 aimOffset = Vec3.ZERO;
    private int  aimJitterTimer = 0;
    private float aimJitterScale = 1.0f;

    private LivingEntity target = null;
    private long hurtTimer = 0L;
    private long nextAttackDelay = 0L;
    private int targetLostTicks = 0;

    private long combatStartTime = 0L;
    private long lastCombatActivity = 0L;

    private static String[] buildModeNames() {
        RotationRegistry.bootstrap();
        String[] registered = RotationRegistry.names();
        String[] result = new String[registered.length + 1];
        System.arraycopy(registered, 0, result, 0, registered.length);
        result[registered.length] = RotationRegistry.defaultMode();
        return result;
    }

    public Aura() {
        Instance = this;
        addSettings(targetPlayers, targetHostile, targetPassive,
                distance, distanceExtra, rotationMode, targetPriority,
                critsOnly, smartSprint, shieldBreak, keepTarget, dontAttack, correction);
    }

    @Override
    public void onEnable() {
        if (nullCheck.all()) { toggle(); return; }
        FreeLookComponent.bootstrap();
        hurtTimer = 0L;
        nextAttackDelay = 0L;
        targetLostTicks = 0;
        combatStartTime = System.currentTimeMillis();
        lastCombatActivity = combatStartTime;
        RotationRegistry.resetAll();
        RotationComponent.INSTANCE.stopRotation();
    }

    @Override
    public void onDisable() {
        RotationRegistry.resetAll();
        RotationComponent.INSTANCE.stopRotation();
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (nullCheck.all()) return;

        long now = System.currentTimeMillis();
        if (now - lastCombatActivity > 1500L) combatStartTime = now;

        LivingEntity newTarget = selectTarget();
        if (keepTarget.isEnabled() && target != null && isValid(target)) {
            // keep
        } else if (newTarget != null) {
            if (newTarget != target) {
                RotationBase rot = getRotation();
                if (rot != null) rot.onTargetChange();
                aimOffset = Vec3.ZERO;
                aimJitterTimer = 0;
            }
            target = newTarget;
            targetLostTicks = 0;
        } else if (target != null && !isValid(target)) {
            if (++targetLostTicks > 5) target = null;
        }

        RotationComponent.INSTANCE.tick();

        if (target == null) {
            RotationComponent.INSTANCE.stopRotation();
            return;
        }

        lastCombatActivity = now;

        float fatigue = Math.min(1f, (now - combatStartTime) / 60_000f);
        aimJitterScale = 0.6f + fatigue * 0.8f;

        Vec3 eye = mc.player.getEyePosition();
        Vec3 aim = getFloatingAimPoint(target, eye, fatigue);
        if (aim.lengthSqr() < 1e-6) return;

        float idealYaw = (float) Mth.wrapDegrees(
                Math.toDegrees(Math.atan2(aim.z, aim.x)) - 90.0);
        float idealPitch = (float)(-Math.toDegrees(
                Math.atan2(aim.y, Math.hypot(aim.x, aim.z))));

        RotationBase rot = getRotation();
        if (rot != null) {
            rot.update(new Rotation(idealYaw, idealPitch), false);
        } else {
            RotationComponent.update(new Rotation(idealYaw, idealPitch),
                    180f, 180f, 180f, 180f, 2, 1, false);
        }

        tryAttack();
    }

    @EventHandler
    public void onInput(InputEvent event) {
        if (nullCheck.all()) return;
        if (target == null) return;
        if (!RotationComponent.INSTANCE.isRotating()) return;

        float clientYaw = FreeLookComponent.isActive()
                ? FreeLookComponent.getFreeYaw()
                : mc.player.getYRot();

        MoveCorrection.applySilentMoveFix(event, clientYaw, mc.player.getYRot());
    }

    private RotationBase getRotation() {
        List<String> enabled = rotationMode.getEnabled();
        if (enabled.isEmpty()) return null;
        return RotationRegistry.get(enabled.get(0));
    }

    private Vec3 getFloatingAimPoint(LivingEntity tgt, Vec3 eye, float fatigue) {
        AABB box = tgt.getBoundingBox();
        Vec3 center = box.getCenter();

        if (--aimJitterTimer <= 0) {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            aimJitterTimer = 6 + r.nextInt(6);
            double halfX = (box.maxX - box.minX) * 0.5;
            double halfY = (box.maxY - box.minY) * 0.5;
            double halfZ = (box.maxZ - box.minZ) * 0.5;
            double scatterX = 0.35 + fatigue * 0.25;
            double scatterY = 0.25 + fatigue * 0.15;
            double scatterZ = 0.35 + fatigue * 0.25;
            aimOffset = new Vec3(
                    (r.nextDouble() - 0.5) * 2.0 * halfX * scatterX,
                    (r.nextDouble() - 0.5) * 2.0 * halfY * scatterY,
                    (r.nextDouble() - 0.5) * 2.0 * halfZ * scatterZ
            );
        } else {
            aimOffset = aimOffset.scale(0.88);
        }

        double margin = 0.08;
        Vec3 point = new Vec3(
                Mth.clamp(center.x + aimOffset.x, box.minX + margin, box.maxX - margin),
                Mth.clamp(center.y + aimOffset.y, box.minY + margin, box.maxY - margin),
                Mth.clamp(center.z + aimOffset.z, box.minZ + margin, box.maxZ - margin)
        );
        return point.subtract(eye);
    }

    private boolean isAimed(LivingEntity tgt, double reach) {
        Vec3 eye  = mc.player.getEyePosition();
        Vec3 look = mc.player.getLookAngle();
        Vec3 end  = eye.add(look.scale(reach + 1.0));
        return tgt.getBoundingBox().clip(eye, end).isPresent();
    }

    private void tryAttack() {
        if (target == null || !target.isAlive()) return;

        long now = System.currentTimeMillis();
        if (now - hurtTimer < nextAttackDelay) return;

        float requiredCooldown = 0.85f + ThreadLocalRandom.current().nextFloat() * 0.12f;
        if (mc.player.getAttackStrengthScale(0.5f) < requiredCooldown) return;

        if (!canAttackNow()) return;
        if (!isAimed(target, distance.getValue())) return;

        if (smartSprint.isEnabled() && critsOnly.isEnabled()
                && mc.player.isSprinting()
                && !mc.player.onGround()
                && !mc.player.isInWater()
                && !mc.player.isSwimming()) {
            mc.player.setSprinting(false);
            if (mc.player.connection != null) {
                mc.player.connection.send(new ServerboundPlayerCommandPacket(
                        mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
            }
            return;
        }

        if (target.isBlocking() && shieldBreak.isEnabled()) {
            breakShieldAndAttack();
        } else {
            doAttack();
        }

        nextAttackDelay = computeNextDelay(now);
    }

    private long computeNextDelay(long now) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        float fatigue = Math.min(1f, (now - combatStartTime) / 60_000f);
        long base = 420L + r.nextInt(120);
        if (r.nextFloat() < 0.15f) base += 150L + r.nextInt(200);
        if (r.nextFloat() < 0.05f) base = 300L + r.nextInt(80);
        base = (long)(base * (1.0f + fatigue * 0.25f));
        return base;
    }

    private boolean canAttackNow() {
        if (dontAttack.getEnabled().contains("Используется предмет")
                && mc.player.isUsingItem() && mc.player.getUseItemRemainingTicks() > 0) return false;
        if (dontAttack.getEnabled().contains("Открыт контейнер")
                && mc.gui.screen() != null) return false;
        if (critsOnly.isEnabled() && !AuraUtil.isCriticalFalling()) return false;
        return AuraUtil.isInReach(target, distance.getValue());
    }

    private void doAttack() {
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
        hurtTimer = System.currentTimeMillis();
        RotationBase rot = getRotation();
        if (rot != null) rot.onAttack();
    }

    private void breakShieldAndAttack() {
        int axeSlot = findAxeSlot(0, 9);
        int prev = mc.player.getInventory().getSelectedSlot();
        if (axeSlot != -1 && axeSlot != prev) {
            mc.player.getInventory().setSelectedSlot(axeSlot);
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(axeSlot));
            doAttack();
            mc.player.getInventory().setSelectedSlot(prev);
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(prev));
            return;
        }
        doAttack();
    }

    private int findAxeSlot(int from, int to) {
        for (int i = from; i < to; i++) {
            var stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof AxeItem) return i;
        }
        return -1;
    }

    private LivingEntity selectTarget() {
        List<LivingEntity> c = new ArrayList<>();
        for (Player p : mc.level.players()) if (isValid(p)) c.add(p);
        try {
            for (var e : mc.level.entitiesForRendering()) {
                if (e instanceof LivingEntity le && !(e instanceof Player) && isValid(le)) c.add(le);
            }
        } catch (Exception ignored) {}
        if (c.isEmpty()) return null;

        boolean byFov  = targetPriority.getEnabled().contains("Прицел");
        boolean byDist = targetPriority.getEnabled().contains("Дистанция");
        boolean byHp   = targetPriority.getEnabled().contains("ХП");
        c.sort(Comparator.comparingDouble(e -> score(e, byFov, byDist, byHp)));
        return c.get(0);
    }

    private double score(LivingEntity e, boolean byFov, boolean byDist, boolean byHp) {
        double s = 0;
        if (byHp)   s += e.getHealth();
        if (byDist) s += mc.player.distanceToSqr(e) * 0.1;
        if (byFov) {
            Vec3 eye = mc.player.getEyePosition();
            Vec3 to  = e.getBoundingBox().getCenter().subtract(eye);
            if (to.lengthSqr() > 1e-6) {
                Vec3 look = mc.player.getLookAngle();
                double dot = look.normalize().dot(to.normalize());
                s += (1.0 - dot) * 180.0;
            }
        }
        return s;
    }

    public boolean isValid(LivingEntity entity) {
        if (entity == mc.player || entity instanceof ArmorStand) return false;
        if (!entity.isAlive() || entity.getHealth() <= 0f) return false;
        if (!mc.player.isAlive() || mc.player.getHealth() <= 0f) return false;
        double maxDist = distance.getValue() + distanceExtra.getValue();
        if (!AuraUtil.isInReach(entity, maxDist)) return false;
        if (entity instanceof Player)      return targetPlayers.isEnabled();
        if (entity instanceof Monster || entity instanceof AmbientCreature) return targetHostile.isEnabled();
        if (entity instanceof Animal)     return targetPassive.isEnabled();
        return false;
    }

    public boolean        hasTarget()    { return target != null; }
    public LivingEntity   getTarget()    { return isEnabled() ? target : null; }
    public LivingEntity   getLastTarget(){ return target; }
    public BooleanSetting getCritsOnly() { return critsOnly; }
}
