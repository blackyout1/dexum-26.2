package dexum.client.system.rotation.modes;

import dexum.client.system.rotation.Rotation;
import dexum.client.system.rotation.RotationBase;
import dexum.client.system.rotation.RotationComponent;
import dexum.client.system.rotation.ServerAngleStore;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.util.concurrent.ThreadLocalRandom;

/**
 * FunTimeNeuroRotation v4
 *
 * Что изменилось от v3:
 *  - GCD-квантизация и delta-лимиты убраны отсюда полностью.
 *    Теперь они живут исключительно в ServerAngleStore.set().
 *    apply() просто пишет сырой угол в хранилище — без дублирования.
 *  - logging отключён по умолчанию (был true).
 *  - timeout=0 убран — активация через RotationComponent.activate().
 *  - SecureRandom заменён на ThreadLocalRandom (rng в RotationBase остаётся,
 *    но здесь везде используется ThreadLocalRandom).
 *
 * Уровни (без изменений по алгоритму):
 *  1. Протокол   — GCD и delta-лимиты → ServerAngleStore
 *  2. Биомеханика — розовый шум Voss-McCartney 5 октав
 *  3. CA          — клеточный автомат 48 ячеек + логистическое отображение r=3.9
 *  4. Контекст    — combatTicks, fatigue, задержка реакции, Lévy flight
 */
public class FunTimeNeuroRotation extends RotationBase {

    // ─── Режимы транзита ─────────────────────────────────────────────────────

    private enum Mode { LEGIT_HOP, FUN_TIME_SNAP, NEURO_FADE }

    private Mode    currentMode  = Mode.LEGIT_HOP;
    private boolean initialized  = false;
    private boolean inTransit    = false;

    private Rotation transitStart         = new Rotation(0f, 0f);
    private Rotation transitEnd           = new Rotation(0f, 0f);
    private long     transitStartNanos;
    private long     transitDurationNanos;

    // ─── Биомеханика: розовый шум (Voss-McCartney 5 октав) ───────────────────

    private final float[] pinkOctaves   = new float[5];
    private int           pinkTickCount = 0;

    private float arcBiasYaw   = 0f;
    private float arcBiasPitch = 0f;

    // ─── NEURO_FADE параметры ────────────────────────────────────────────────

    private float neuroFadeYawAmp, neuroFadePitchAmp;
    private float neuroFadeFreq, neuroFadePhase;

    // ─── Клеточный автомат ───────────────────────────────────────────────────

    private static final int CA_SIZE  = 48;
    private final float[]    caBuffer = new float[CA_SIZE];
    private float            logisticT   = 0.5f;
    private float            caAmplitude = 0.5f;

    // ─── Контекст ────────────────────────────────────────────────────────────

    private int reactionDelay = 0;
    private int combatTicks   = 0;

    // ─── Инициализация ───────────────────────────────────────────────────────

    private void init() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            lastYaw   = mc.player.getYRot();
            lastPitch = mc.player.getXRot();
            // Синхронизируем хранилище с текущим положением игрока
            ServerAngleStore.INSTANCE.syncFromPlayer();
        }
        ThreadLocalRandom r = ThreadLocalRandom.current();
        logisticT = 0.3f + r.nextFloat() * 0.4f;
        for (int i = 0; i < CA_SIZE; i++) caBuffer[i] = (r.nextFloat() - 0.5f) * 0.2f;
        for (int i = 0; i < 5; i++) pinkOctaves[i] = (r.nextFloat() - 0.5f) * 0.5f;
        initialized = true;
    }

    // ─── Главный метод ───────────────────────────────────────────────────────

    @Override
    public void update(Rotation angle, boolean elytraVisual) {
        if (!initialized) init();

        combatTicks++;
        tickCA();
        tickPinkNoise();

        if (reactionDelay > 0) {
            reactionDelay--;
            // Активируем компонент даже в delay — чтобы FreeLook работал
            RotationComponent.activate(1, !elytraVisual);
            return;
        }

        Rotation current    = new Rotation(lastYaw, lastPitch);
        float    distToTarget = current.getDelta(angle);

        if (distToTarget <= 2.5f) {
            // На цели — просто фиксируем
            inTransit = false;
            apply(angle, elytraVisual);
            return;
        }

        if (!inTransit) {
            beginTransit(current, angle, distToTarget);
        }

        float t = (float)(System.nanoTime() - transitStartNanos) / (float)transitDurationNanos;
        if (t >= 1f) {
            inTransit = false;
            apply(angle, elytraVisual);
            return;
        }

        Rotation out = switch (currentMode) {
            case LEGIT_HOP     -> computeLegitHop(t);
            case FUN_TIME_SNAP -> computeSnap(t);
            case NEURO_FADE    -> computeNeuroFade(t);
        };

        apply(out, elytraVisual);
    }

    // ─── Начало транзита ─────────────────────────────────────────────────────

    private void beginTransit(Rotation from, Rotation to, float distDeg) {
        transitStart = from;
        transitEnd   = to;
        inTransit    = true;

        ThreadLocalRandom r       = ThreadLocalRandom.current();
        float             fatigue = Math.min(1f, combatTicks / 2400f);

        if (distDeg > 55f || fatigue > 0.7f) {
            currentMode = Mode.LEGIT_HOP;
        } else if (distDeg < 12f) {
            currentMode = r.nextFloat() < 0.6f ? Mode.FUN_TIME_SNAP : Mode.NEURO_FADE;
        } else {
            currentMode = r.nextFloat() < 0.55f ? Mode.NEURO_FADE : Mode.LEGIT_HOP;
        }

        // Длительность — закон Фиттса
        float fittsBase;
        if      (distDeg < 5f)  fittsBase = 40f  + r.nextFloat() * 30f;
        else if (distDeg < 20f) fittsBase = 70f  + r.nextFloat() * 60f;
        else if (distDeg < 50f) fittsBase = 110f + r.nextFloat() * 90f;
        else                    fittsBase = 160f + r.nextFloat() * 120f;

        fittsBase *= 1f + fatigue * 0.7f;

        // Lévy flight — редкий крупный рывок (8%)
        if (r.nextFloat() < 0.08f) {
            float levy = (float)(1.0 / Math.pow(Math.max(r.nextFloat(), 0.01f), 1.0 / 1.5));
            fittsBase *= Mth.clamp(levy, 1.0f, 4.5f);
        }

        transitDurationNanos = (long)(fittsBase * 1_000_000L);
        transitStartNanos    = System.nanoTime();

        // Arc bias
        float arcStrength = (0.3f + r.nextFloat() * 0.7f) * Math.min(1f, distDeg / 30f);
        arcBiasYaw   = (r.nextFloat() - 0.5f) * 2f * arcStrength * 4f;
        arcBiasPitch = (r.nextFloat() - 0.5f) * 2f * arcStrength * 2f;

        caAmplitude = 0.3f + r.nextFloat() * 0.5f;

        if (currentMode == Mode.NEURO_FADE) {
            float scale = Mth.clamp(distDeg / 50f, 0.3f, 1.2f);
            neuroFadeYawAmp   = (1.0f + r.nextFloat() * 1.8f) * scale;
            neuroFadePitchAmp = (0.5f + r.nextFloat() * 1.0f) * scale;
            neuroFadeFreq     = 1.6f + r.nextFloat() * 2.2f;
            neuroFadePhase    = r.nextFloat() * (float)(Math.PI * 2.0);
        }
    }

    // ─── Траектории ──────────────────────────────────────────────────────────

    private Rotation computeLegitHop(float t) {
        float e       = easeOutQuint(t);
        float arcFade = (float)Math.sin(Math.PI * t);

        float yawDiff   = Mth.wrapDegrees(transitEnd.getYaw()   - transitStart.getYaw());
        float pitchDiff = transitEnd.getPitch() - transitStart.getPitch();

        float yaw   = transitStart.getYaw()   + yawDiff   * e + arcBiasYaw   * arcFade;
        float pitch = transitStart.getPitch() + pitchDiff * e + arcBiasPitch * arcFade;

        float pink = pinkValue();
        yaw   += pink * 0.6f;
        pitch += pink * 0.3f;

        return new Rotation(yaw, Mth.clamp(pitch, -90f, 90f));
    }

    private Rotation computeSnap(float t) {
        float e = 1f - (float)Math.pow(1.0 - t, 4.0);

        float yawDiff   = Mth.wrapDegrees(transitEnd.getYaw()   - transitStart.getYaw());
        float pitchDiff = transitEnd.getPitch() - transitStart.getPitch();

        float yaw   = transitStart.getYaw()   + yawDiff   * e;
        float pitch = transitStart.getPitch() + pitchDiff * e;

        if (t > 0.65f) {
            float fade = (t - 0.65f) / 0.35f;
            float ca   = caValue() * (float)Math.sin(fade * Math.PI);
            yaw   += ca * 0.8f;
            pitch += ca * 0.35f;
        }

        return new Rotation(yaw, Mth.clamp(pitch, -90f, 90f));
    }

    private Rotation computeNeuroFade(float t) {
        float e = sigmoid(t, 8.0f, 0.42f);

        float yawDiff   = Mth.wrapDegrees(transitEnd.getYaw()   - transitStart.getYaw());
        float pitchDiff = transitEnd.getPitch() - transitStart.getPitch();

        float baseYaw   = transitStart.getYaw()   + yawDiff   * e;
        float basePitch = transitStart.getPitch() + pitchDiff * e;

        float arcFade = (float)Math.sin(Math.PI * t);
        baseYaw   += arcBiasYaw   * arcFade;
        basePitch += arcBiasPitch * arcFade;

        float taper  = (float)(Math.sin(Math.PI * t) * (1f - t * 0.6f));
        float wobYaw = (float)Math.sin(neuroFadeFreq * Math.PI * 2.0 * t + neuroFadePhase);
        float wobPit = (float)Math.sin(neuroFadeFreq * Math.PI * 2.0 * t * 1.618f + neuroFadePhase + 1.0f);

        baseYaw   += wobYaw * neuroFadeYawAmp   * taper;
        basePitch += wobPit * neuroFadePitchAmp * taper * 0.5f;

        float pink = pinkValue();
        baseYaw   += pink * 0.5f;
        basePitch += pink * 0.22f;

        return new Rotation(baseYaw, Mth.clamp(basePitch, -90f, 90f));
    }

    // ─── Применение угла ─────────────────────────────────────────────────────

    /**
     * Пишет сырой вычисленный угол в ServerAngleStore.
     * GCD и delta-лимиты применяются ТОЛЬКО там — никакой дублирующей математики здесь.
     * lastYaw/lastPitch обновляются ПОСЛЕ записи, чтобы следующий тик считал дельту правильно.
     */
    private void apply(Rotation raw, boolean elytraVisual) {
        // CA-шум поверх вычисленного угла (финальный штрих биомеханики)
        float ca   = caValue() * caAmplitude;
        float pink = pinkValue();

        float yaw   = raw.getYaw()   + ca * 0.4f + pink * 0.1f;
        float pitch = Mth.clamp(raw.getPitch() + ca * 0.2f, -90f, 90f);

        // Записываем в хранилище — оно само применит GCD и delta-лимиты
        ServerAngleStore.INSTANCE.set(yaw, pitch);

        // Обновляем lastYaw/lastPitch по тому, что реально записалось
        lastYaw   = ServerAngleStore.INSTANCE.getServerYaw();
        lastPitch = ServerAngleStore.INSTANCE.getServerPitch();

        // Сообщаем RotationComponent что ротация активна
        RotationComponent.activate(1, !elytraVisual);
    }

    // ─── Клеточный автомат ───────────────────────────────────────────────────

    private void tickCA() {
        logisticT = 3.9f * logisticT * (1f - logisticT);
        float[] next = new float[CA_SIZE];
        for (int i = 0; i < CA_SIZE; i++) {
            int l = (i - 1 + CA_SIZE) % CA_SIZE;
            int ri = (i + 1) % CA_SIZE;
            float sum = caBuffer[l] * 0.35f + caBuffer[i] * 0.30f + caBuffer[ri] * 0.35f
                      + (logisticT - 0.5f) * 0.08f;
            next[i] = tanhF(sum);
        }
        System.arraycopy(next, 0, caBuffer, 0, CA_SIZE);
    }

    private float caValue() {
        float sum = 0f;
        for (float v : caBuffer) sum += v;
        return sum / CA_SIZE;
    }

    // ─── Розовый шум ─────────────────────────────────────────────────────────

    private void tickPinkNoise() {
        pinkTickCount++;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int k = 0; k < 5; k++) {
            if ((pinkTickCount & ((1 << k) - 1)) == 0) {
                pinkOctaves[k] = (r.nextFloat() - 0.5f) * (1f / (1 << k));
            }
        }
    }

    private float pinkValue() {
        float sum = 0f;
        for (float v : pinkOctaves) sum += v;
        return sum * 0.4f;
    }

    // ─── Callbacks ───────────────────────────────────────────────────────────

    @Override
    public void onTargetChange() {
        inTransit = false;
        // 2–5 тиков задержки реакции (100–250 мс)
        reactionDelay = 2 + ThreadLocalRandom.current().nextInt(4);
        ServerAngleStore.INSTANCE.setReactionDelay(reactionDelay);
    }

    @Override
    public void onAttack() {
        inTransit = false;
        if (ThreadLocalRandom.current().nextFloat() < 0.25f) {
            combatTicks = Math.max(0, combatTicks - 120);
        }
    }

    // ─── Математика ──────────────────────────────────────────────────────────

    private static float easeOutQuint(float t) {
        float u = 1f - t;
        return 1f - u * u * u * u * u;
    }

    private static float sigmoid(float t, float steep, float mid) {
        return (float)(1.0 / (1.0 + Math.exp(-steep * (t - mid))));
    }

    private static float tanhF(float x) {
        if (x >  3f) return  1f;
        if (x < -3f) return -1f;
        double e2 = Math.exp(2.0 * x);
        return (float)((e2 - 1.0) / (e2 + 1.0));
    }
}

