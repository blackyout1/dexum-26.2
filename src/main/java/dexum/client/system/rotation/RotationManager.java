package dexum.client.system.rotation;

import net.minecraft.client.Minecraft;

public class RotationManager {

    public static final RotationManager INSTANCE = new RotationManager();

    private RotationManager() {}

    public void setRotation(Rotation rotation) {
        ServerAngleStore.INSTANCE.set(rotation.getYaw(), rotation.getPitch());
    }

    @Deprecated
    public void setRotation(Rotation rotation, boolean applyMoveCorrection) {
        setRotation(rotation);
    }

    public void setRotationWithLook(Rotation rotation) {
        ServerAngleStore.INSTANCE.set(rotation.getYaw(), rotation.getPitch());
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            mc.player.setYRot(rotation.getYaw());
            mc.player.setXRot(rotation.getPitch());
        }
    }

    public void reset() {
        ServerAngleStore.INSTANCE.clear();
    }

    public boolean isActive() {
        return ServerAngleStore.INSTANCE.isActive();
    }

    public boolean isMoveCorrection() {
        return true;
    }

    public Rotation getMoveRotation() {
        return getRotation();
    }

    public float getServerYaw() {
        return ServerAngleStore.INSTANCE.getServerYaw();
    }

    public float getServerPitch() {
        return ServerAngleStore.INSTANCE.getServerPitch();
    }

    public Rotation getRotation() {
        if (isActive()) {
            return new Rotation(getServerYaw(), getServerPitch());
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            return new Rotation(mc.player.getYRot(), mc.player.getXRot());
        }
        return new Rotation();
    }
}
