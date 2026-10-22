package dexum.client.system.api;

import dexum.client.system.features.combat.AimAssist;
import dexum.client.system.features.combat.NoFriendDamage;
import dexum.client.system.features.combat.TriggerBot;
import dexum.client.system.features.movement.*;
import dexum.client.system.features.player.AutoRespawn;
import dexum.client.system.features.player.ChestStealer;
import dexum.client.system.features.player.NoDelay;
import dexum.client.system.features.player.NoInteract;
import dexum.client.system.features.player.StreamerMode;
import dexum.client.system.features.render.*;
import dexum.client.system.features.world.NoWeather;
import dexum.client.system.features.world.TimeChanger;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionManager {
    @Getter private static final List<Function> functions = new ArrayList<>();

    public static void init() {
        // combat
        register(new TriggerBot(), new NoFriendDamage(), new AimAssist());

        // movement
        register(new Sprint(), new NoSlow(), new NoPush(), new AutoSneak(), new NoFall());

        // render
        register(
            new ClickGui(), new NoBob(), new Watermark(), new FreeCam(),
            new Nametags(), new NoFov(), new TargetESP(),
            new FullBright(), new Zoom(), new NoHurtCam(), new AntiBlind(),
            new FriendESP(), new FriendHUD(), new FriendArrow()
        );

        // player
        register(new NoDelay(), new NoInteract(), new ChestStealer(), new AutoRespawn(), new StreamerMode());

        // world
        register(new TimeChanger(), new NoWeather());
    }

    private static void register(Function... functionArray) {
        functions.addAll(Arrays.asList(functionArray));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Function> T getFunction(Class<T> clazz) {
        return (T) functions.stream()
                .filter(f -> f.getClass() == clazz)
                .findFirst()
                .orElse(null);
    }

    public static List<Function> getFunctionsByCategory(Category category) {
        return functions.stream()
                .filter(f -> f.getCategory() == category)
                .collect(Collectors.toList());
    }
}
