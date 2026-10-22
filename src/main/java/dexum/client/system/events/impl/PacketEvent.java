package dexum.client.system.events.impl;

import dexum.client.system.events.EventCancellable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.protocol.Packet;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PacketEvent extends EventCancellable {
    Packet<?> packet;
    Type type;

    public enum Type {
        SEND,
        RECEIVE
    }

    public static class Send extends PacketEvent {
        public Send(Packet<?> packet) {
            super(packet, Type.SEND);
        }
    }

    public static class Receive extends PacketEvent {
        public Receive(Packet<?> packet) {
            super(packet, Type.RECEIVE);
        }
    }
}
