package dexum.client.utils.helpers;

import net.minecraft.client.Minecraft;

public class NullCheck {
    private final Minecraft mc = Minecraft.getInstance();

    public boolean player() {
        return mc.player == null;
    }

    public boolean world() {
        return mc.level == null;
    }

    public boolean all() {
        return mc.player == null || mc.level == null;
    }
}
