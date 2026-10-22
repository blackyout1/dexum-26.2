package dexum.client.system.rotation;

import dexum.client.system.events.EventBus;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.LookEvent;
import dexum.client.system.events.impl.RotationEvent;
import net.minecraft.util.Mth;

public class FreeLookComponent {

    private static FreeLookComponent INSTANCE;
    private static boolean active = false;
    private static float freeYaw   = 0f;
    private static float freePitch = 0f;
    private static final float SENS = 0.15f;

    public FreeLookComponent() {
        INSTANCE = this;
        EventBus.register(this);
    }

    @EventHandler
    public void onLook(LookEvent event) {
        if (!active) return;
        freeYaw   = (float)(freeYaw   + event.getYaw()   * SENS);
        freePitch = Mth.clamp((float)(freePitch + event.getPitch() * SENS), -90f, 90f);
        event.cancel();
    }

    @EventHandler
    public void onRotation(RotationEvent event) {
        if (active) {
            event.setYaw(freeYaw);
            event.setPitch(freePitch);
        } else {
            freeYaw   = event.getYaw();
            freePitch = event.getPitch();
        }
    }

    public static void setActive(boolean v) { active = v; }
    public static boolean isActive()        { return active; }
    public static float getFreeYaw()        { return freeYaw; }
    public static float getFreePitch()      { return freePitch; }
    public static void setFreeYaw(float y)  { freeYaw = y; }
    public static void setFreePitch(float p){ freePitch = p; }

    public static void bootstrap() {
        if (INSTANCE == null) new FreeLookComponent();
    }
}
