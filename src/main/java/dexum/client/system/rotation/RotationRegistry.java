package dexum.client.system.rotation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RotationRegistry {

    private static final Map<String, RotationBase> REGISTRY = new LinkedHashMap<>();
    private static final String DEFAULT_MODE = "None";
    private static boolean bootstrapped = false;

    private RotationRegistry() {}

    public static void register(String name, RotationBase instance) {
        REGISTRY.put(name, instance);
    }
    public static RotationBase get(String name) { return REGISTRY.get(name); }
    public static String[] names() { return REGISTRY.keySet().toArray(new String[0]); }
    public static Map<String, RotationBase> all() { return Collections.unmodifiableMap(REGISTRY); }
    public static String defaultMode() { return DEFAULT_MODE; }

    public static void resetAll() {
        for (RotationBase rot : REGISTRY.values()) rot.onTargetChange();
    }

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;


        register("Vanilla",     new dexum.client.system.rotation.modes.VanillaRotation());
        register("FunTimeNeuroRotation", new dexum.client.system.rotation.modes.FunTimeNeuroRotation());
    }
}