package dexum.client.mixins.client;

import dexum.client.interfaces.IMinecraftClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

import java.lang.reflect.Field;

@Mixin(Minecraft.class)
public class MinecraftMixin implements IMinecraftClient {

    // "rightClickDelay" in MC 26.2 (confirmed from Lenrete FastPlaceModule)
    private static final Field RIGHT_CLICK_DELAY = resolveField();
    private static Field resolveField() {
        for (String name : new String[]{"rightClickDelay", "itemUseCooldown", "useItemCooldown"}) {
            try {
                Field f = Minecraft.class.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }

    @Override
    public void setItemUseCooldown(int val) {
        if (RIGHT_CLICK_DELAY == null) return;
        try { RIGHT_CLICK_DELAY.setInt(this, val); } catch (IllegalAccessException ignored) {}
    }
}
