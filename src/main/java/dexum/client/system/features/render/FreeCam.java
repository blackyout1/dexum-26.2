package dexum.client.system.features.render;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.CameraPositionEvent;
import dexum.client.system.events.impl.DamageEvent;
import dexum.client.system.events.impl.MoveEvent;
import dexum.client.system.events.impl.PacketEvent;
import dexum.client.system.events.impl.UpdateEvent;
import dexum.client.system.settings.impl.BooleanSetting;
import dexum.client.system.settings.impl.NumberSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

@NewFunction(name = "FreeCam", desc = "Свободная камера — тело стоит на месте", category = Category.RENDER)
public class FreeCam extends Function {

    private final NumberSetting speed = new NumberSetting("Скорость", 1.0, 0.1, 5.0, 0.1);
    private final BooleanSetting freezePackets = new BooleanSetting("Спуф позиции", true);
    private final BooleanSetting exitOnDamage  = new BooleanSetting("Выход при уроне", true);

    private double camX, camY, camZ;
    private boolean active = false;

    private double frozenX, frozenY, frozenZ;
    private float  frozenYaw, frozenPitch;

    private long lastFrameTime = 0;

    public FreeCam() {
        addSettings(speed, freezePackets, exitOnDamage);
    }

    @Override
    public void onEnable() {
        if (nullCheck.all()) { toggle(); return; }

        camX = mc.player.getX();
        camY = mc.player.getEyeY();
        camZ = mc.player.getZ();

        frozenX     = mc.player.getX();
        frozenY     = mc.player.getY();
        frozenZ     = mc.player.getZ();
        frozenYaw   = mc.player.getYRot();
        frozenPitch = mc.player.getXRot();

        lastFrameTime = System.nanoTime();
        active = true;
    }

    @Override
    public void onDisable() {
        active = false;
        if (!nullCheck.all()) {
            mc.player.setDeltaMovement(Vec3.ZERO);
        }
    }

    @EventHandler
    public void onCamera(CameraPositionEvent event) {
        if (!active || nullCheck.all() || !mc.player.isAlive()) return;

        long now = System.nanoTime();
        double dt = Math.min((now - lastFrameTime) / 1_000_000_000.0, 0.1);
        lastFrameTime = now;

        double spd = speed.getValue();

        boolean fwd   = mc.options.keyUp.isDown();
        boolean back  = mc.options.keyDown.isDown();
        boolean left  = mc.options.keyLeft.isDown();
        boolean right = mc.options.keyRight.isDown();
        boolean up    = mc.options.keyJump.isDown();
        boolean down  = mc.options.keyShift.isDown();

        float yaw = mc.player.getYRot();
        double yawRad = Math.toRadians(yaw);

        double mf = (fwd ? 1 : 0) - (back  ? 1 : 0);
        double ms = (left? 1 : 0) - (right ? 1 : 0);
        double my = (up  ? 1 : 0) - (down  ? 1 : 0);

        double dx = (-Math.sin(yawRad) * mf + Math.cos(yawRad) * ms) * spd * dt * 5.0;
        double dz = ( Math.cos(yawRad) * mf + Math.sin(yawRad) * ms) * spd * dt * 5.0;
        double dy = my * spd * dt * 5.0;

        camX += dx; camY += dy; camZ += dz;

        event.setPos(new Vec3(camX, camY, camZ));
        event.setYaw(mc.player.getYRot());
        event.setPitch(mc.player.getXRot());
        event.setCancelled(true);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!active || nullCheck.all()) return;
        mc.player.setDeltaMovement(Vec3.ZERO);
        mc.player.setPos(frozenX, frozenY, frozenZ);
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if (!active) return;
        event.setMovement(Vec3.ZERO);
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (!active) return;
        if (event.getType() != PacketEvent.Type.SEND) return;
        if (!freezePackets.isEnabled()) return;
        if (!(event.getPacket() instanceof ServerboundMovePlayerPacket)) return;

        event.setCancelled(true);

        if (mc.player.connection != null) {
            mc.player.connection.send(
                new ServerboundMovePlayerPacket.PosRot(
                    frozenX, frozenY, frozenZ,
                    frozenYaw, frozenPitch,
                    true, false
                )
            );
        }
    }

    @EventHandler
    public void onDamage(DamageEvent event) {
        if (exitOnDamage.isEnabled() && isEnabled()) {
            toggle();
        }
    }
}
