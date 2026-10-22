package dexum.client.system.events.impl;

import dexum.client.system.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.KeyboardHandler;

@Getter
@AllArgsConstructor
public class KeyEvent implements Event {
    int action;
    long window;
    int key;
    int scancode;
    int modifiers;
}
