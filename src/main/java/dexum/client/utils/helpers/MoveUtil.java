package dexum.client.utils.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class MoveUtil {

    public static boolean isMoving() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return false;
        var input = player.input;
        if (input == null) return false;
        return input.getMoveVector().lengthSquared() > 1.0E-7f;
    }

    public static double getSpeed() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return 0;
        double dx = player.getX() - player.xo;
        double dz = player.getZ() - player.zo;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
