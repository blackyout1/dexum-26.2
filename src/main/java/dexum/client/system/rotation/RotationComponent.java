package dexum.client.system.rotation;

import net.minecraft.client.Minecraft;

public class RotationComponent {

    public static final RotationComponent INSTANCE = new RotationComponent();

    private RotationTask currentTask     = RotationTask.IDLE;
    private int          currentPriority = 0;

    private RotationComponent() {}

    public static void activate(int priority, boolean silent) {
        RotationComponent inst = INSTANCE;
        if (inst.currentPriority > priority) return;

        if (inst.currentTask == RotationTask.IDLE && silent) {
            FreeLookComponent.setActive(true);
        }

        inst.currentTask     = RotationTask.AIM;
        inst.currentPriority = priority;
    }

    public void stopRotation() {
        currentTask     = RotationTask.IDLE;
        currentPriority = 0;
        FreeLookComponent.setActive(false);
        ServerAngleStore.INSTANCE.clear();
    }

    public boolean isRotating() {
        return currentTask != RotationTask.IDLE;
    }

    public static void update(Rotation target, float yawSpeed, float pitchSpeed,
                               float yawReturnSpeed, float pitchReturnSpeed,
                               int timeout, int priority, boolean silent) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        RotationComponent inst = INSTANCE;
        if (inst.currentPriority > priority) return;

        ServerAngleStore.INSTANCE.set(target.getYaw(), target.getPitch());
        activate(priority, silent);
    }

    public void tick() {}

    public float getServerYaw()   { return ServerAngleStore.INSTANCE.getServerYaw(); }
    public float getServerPitch() { return ServerAngleStore.INSTANCE.getServerPitch(); }

    public enum RotationTask { AIM, IDLE }
}
