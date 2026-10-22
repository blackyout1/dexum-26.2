package dexum.client.system.events.impl;

import dexum.client.system.events.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Событие отталкивания игрока
 */
@Getter
@AllArgsConstructor
public class PushEvent extends EventCancellable {
    private Type type;

    public enum Type {
        BLOCK,      // Отталкивание от блоков
        COLLISION,  // Отталкивание от сущностей
        WATER,      // Отталкивание водой
        EXPLOSION   // Отталкивание взрывом
    }
}
