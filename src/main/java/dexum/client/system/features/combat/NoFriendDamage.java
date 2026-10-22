package dexum.client.system.features.combat;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.AttackEvent;
import dexum.client.system.friends.FriendManager;
import net.minecraft.world.entity.player.Player;

@NewFunction(name = "NoFriendDamage", desc = "Отменяет удар по игрокам из списка друзей", category = Category.COMBAT)
public class NoFriendDamage extends Function {

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (nullCheck.all()) return;
        if (!(event.getTarget() instanceof Player player)) return;

        if (FriendManager.isFriend(player.getGameProfile().name())) {
            event.setCancelled(true);
        }
    }
}
