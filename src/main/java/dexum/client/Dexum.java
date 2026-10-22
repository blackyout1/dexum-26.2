package dexum.client;

import com.mojang.blaze3d.platform.InputConstants;
import dexum.client.system.api.FunctionManager;
import dexum.client.system.command.CommandManager;
import dexum.client.system.command.impl.*;
import dexum.client.system.config.ConfigManager;
import dexum.client.system.events.EventBus;
import dexum.client.system.events.EventManager;
import dexum.client.system.events.impl.Render2DEvent;
import dexum.client.system.events.impl.UpdateEvent;
import dexum.client.system.friends.FriendManager;
import dexum.client.system.ui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class Dexum implements ClientModInitializer {

    public static final String MOD_ID = "dexum";

    // Tracks previous key-down state per keycode to detect press edges
    private final Map<Integer, Boolean> keyStateMap = new HashMap<>();
    
    private static ClickGuiScreen clickGuiScreen;

    @Override
    public void onInitializeClient() {
        FunctionManager.init();
        ConfigManager.init();
        FriendManager.init();

        EventBus.register(new EventManager());

        CommandManager.register(
                new HelpCommand(),
                new ToggleCommand(),
                new BindCommand(),
                new ModulesCommand(),
                new ConfigCommand(),
                new FriendCommand()
        );

        clickGuiScreen = new ClickGuiScreen();

        Runtime.getRuntime().addShutdownHook(new Thread(ConfigManager::shutdown));

        // Render2DEvent via HudElementRegistry (26.2 way)
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
                (g, delta) -> {
                    dexum.client.utils.render.RenderUtil.setContext(g);
                    EventBus.post(new Render2DEvent(g, delta));
                }
        );

        // UpdateEvent via ClientTickEvents + keybind handling
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.level != null) {
                EventBus.post(new UpdateEvent());
            }

            // Keybind handling — don't fire while a screen (inventory, chat, etc.) is open
            boolean screenOpen = client.gui.screen() != null;
            if (!screenOpen && client.player != null) {
                for (dexum.client.system.api.Function f : dexum.client.system.api.FunctionManager.getFunctions()) {
                    int key = f.getKeybind();
                    if (key > 0) {
                        boolean isPressed = InputConstants.isKeyDown(client.getWindow(), key);
                        boolean wasPressed = keyStateMap.getOrDefault(key, false);
                        if (isPressed && !wasPressed) {
                            f.toggle();
                        }
                        keyStateMap.put(key, isPressed);
                    }
                }
                
                // Click GUI toggle with Right Shift
                boolean rightShiftDown = InputConstants.isKeyDown(client.getWindow(), 344);
                boolean wasRightShiftDown = keyStateMap.getOrDefault(344, false);
                if (rightShiftDown && !wasRightShiftDown) {
                    if (clickGuiScreen != null && client.gui.screen() == null) {
                        client.setScreenAndShow(clickGuiScreen);
                    }
                }
                keyStateMap.put(344, rightShiftDown);
            }
        });
    }
}
