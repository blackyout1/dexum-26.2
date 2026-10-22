package dexum.client.mixins.network;

import dexum.client.system.rotation.ServerAngleStore;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerboundMovePlayerPacket.PosRot.class)
public class FullPacketMixin {

    @ModifyVariable(method = "<init>(DDDFFZZ)V", at = @At("HEAD"), ordinal = 0, argsOnly = true, require = 0)
    private static float dexum$yaw(float yaw) {
        ServerAngleStore store = ServerAngleStore.INSTANCE;
        return store.isActive() ? store.getServerYaw() : yaw;
    }

    @ModifyVariable(method = "<init>(DDDFFZZ)V", at = @At("HEAD"), ordinal = 1, argsOnly = true, require = 0)
    private static float dexum$pitch(float pitch) {
        ServerAngleStore store = ServerAngleStore.INSTANCE;
        return store.isActive() ? store.getServerPitch() : pitch;
    }
}
