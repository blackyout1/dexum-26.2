package dexum.client.system.events.impl;

import dexum.client.system.events.EventCancellable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Событие отправки сообщения в чат
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatEvent extends EventCancellable {
    String message;
    
    public ChatEvent(String message) {
        this.message = message;
    }
}
