package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.*;
import dexum.client.system.settings.impl.BooleanSetting;
import dexum.client.system.settings.impl.NumberSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

@NewFunction(name = "FreeCam", desc = "Свободная камера — тело стоит на месте", category = Category.RENDER)
public class FreeCam extends Function {

    public static FreeCam INSTANCE;

    private final NumberSetting speed = new NumberSetting("Скорость", 1.0, 0.1, 5.0, 0.1);
    private final BooleanSetting exitOnDamage = new BooleanSetting("Выход при уроне", true);

    private static final ThreadLocal<Boolean> SENDING = ThreadLocal.withInitial(() -> false);

    private double camX, camY, camZ;
    private double frozenX, frozenY, frozenZ;
    private float frozenYaw, frozenPitch;
    private boolean active = false;
    private long lastFrameTime = 0;

    public FreeCam() {
        INSTANCE = this;
        addSettings(speed, exitOnDamage);
    }

    @Override
    public void onEnable() {
        if (nullCheck.all()) { setEnabled(false); return; }
        frozenX = mc.player.getX();
        frozenY = mc.player.getY();
        frozenZ = mc.player.getZ();
        frozenYaw = mc.player.getYRot();
        frozenPitch = mc.player.getXRot();
        camX = frozenX;
        camY = mc.player.getEyeY();
        camZ = frozenZ;
        lastFrameTime = System.nanoTime();
        active = true;
    }

    @Override
    public void onDisable() {
        active = false;
        if (!nullCheck.all()) {
            mc.player.setDeltaMovement(Vec3.ZERO);
            // Возвращаем исходные углы, чтобы не было резкого скачка после выхода
            mc.player.setYRot(frozenYaw);
            mc.player.setXRot(frozenPitch);
        }
    }

    public boolean isActive()            { return active; }
    public Vec3 getCameraPosition()      { return new Vec3(camX, camY, camZ); }
    public float getFrozenYaw()          { return frozenYaw; }
    public float getFrozenPitch()        { return frozenPitch; }

    /**
     * Обновляет только позицию камеры. Rotation НЕ трогаем — она идёт от player,
     * который крутится от мыши как обычно. Камера следует за player.
     */
    @EventHandler
    public void onCamera(CameraPositionEvent event) {
        if (!active || nullCheck.all() || !mc.player.isAlive()) return;

        long now = System.nanoTime();
        double dt = Math.min((now - lastFrameTime) / 1_000_000_000.0, 0.1);
        lastFrameTime = now;
        double spd = speed.getValue();

        // WASD летит камеру относительно её текущего yaw (player.yRot, крутится от мыши)
        float camYaw = mc.player.getYRot();
        double yawRad = Math.toRadians(camYaw);

        boolean fwd   = mc.options.keyUp.isDown();
        boolean back  = mc.options.keyDown.isDown();
        boolean left  = mc.options.keyLeft.isDown();
        boolean right = mc.options.keyRight.isDown();
        boolean up    = mc.options.keyJump.isDown();
        boolean down  = mc.options.keyShift.isDown();

        double mf = (fwd ? 1 : 0) - (back  ? 1 : 0);
        double ms = (left ? 1 : 0) - (right ? 1 : 0);
        double my = (up  ? 1 : 0) - (down  ? 1 : 0);

        double step = spd * dt * 10.0;
        camX += (-Math.sin(yawRad) * mf + Math.cos(yawRad) * ms) * step;
        camZ += ( Math.cos(yawRad) * mf + Math.sin(yawRad) * ms) * step;
        camY += my * step;

        event.setPos(new Vec3(camX, camY, camZ));
        event.setYaw(mc.player.getYRot());
        event.setPitch(mc.player.getXRot());
        event.setCancelled(true);
    }

    /** Держим тело в исходной точке. Углы не трогаем — пусть мышь крутит. */
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!active || nullCheck.all()) return;
        mc.player.setDeltaMovement(Vec3.ZERO);
        mc.player.setPos(frozenX, frozenY, frozenZ);
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if (active) event.setMovement(Vec3.ZERO);
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (!active) return;
        if (event.getType() != PacketEvent.Type.SEND) return;
        if (!(event.getPacket() instanceof ServerboundMovePlayerPacket)) return;

        event.setCancelled(true);

        if (SENDING.get()) return;
        if (mc.player == null || mc.player.connection == null) return;

        SENDING.set(true);
        try {
            // На сервер — frozen позиция и frozen углы (там игрок стоит и смотрит в исходную сторону)
            mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                    frozenX, frozenY, frozenZ, frozenYaw, frozenPitch, true, false
            ));
        } finally {
            SENDING.set(false);
        }
    }

    @EventHandler
    public void onDamage(DamageEvent event) {
        if (exitOnDamage.isEnabled() && isEnabled()) setEnabled(false);
    }
}