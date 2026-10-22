package dexum.client.system.features.movement;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.PacketEvent;
import dexum.client.system.settings.impl.ModeSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

@NewFunction(name = "NoFall", desc = "Отменяет урон от падения", category = Category.MOVEMENT)
public class NoFall extends Function {

    private final ModeSetting mode = new ModeSetting("Режим", "Packet", "Vanilla");

    public NoFall() {
        addSettings(mode);
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (nullCheck.player()) return;
        if (event.getType() != PacketEvent.Type.SEND) return;
        if (!(event.getPacket() instanceof ServerboundMovePlayerPacket)) return;

        if (mc.player.fallDistance <= 2.5f) return;

        switch (mode.getActiveMode()) {
            case "Packet" -> {
                event.setCancelled(true);

                if (mc.player.connection != null) {
                    mc.player.connection.send(
                            new ServerboundMovePlayerPacket.PosRot(
                                    mc.player.getX(),
                                    mc.player.getY(),
                                    mc.player.getZ(),
                                    mc.player.getYRot(),
                                    mc.player.getXRot(),
                                    true,
                                    false
                            )
                    );
                }
            }
            case "Vanilla" -> {
                mc.player.fallDistance = 0f;
            }
        }
    }
}
