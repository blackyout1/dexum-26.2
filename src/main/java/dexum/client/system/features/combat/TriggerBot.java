package dexum.client.system.features.combat;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.events.EventHandler;
import dexum.client.system.events.impl.InputEvent;
import dexum.client.system.events.impl.UpdateEvent;
import dexum.client.system.friends.FriendManager;
import dexum.client.system.settings.impl.BooleanSetting;
import dexum.client.system.settings.impl.MultiSetting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.concurrent.ThreadLocalRandom;

@NewFunction(name = "TriggerBot", desc = "Бьёт когда прицел на цели", category = Category.COMBAT)
public class TriggerBot extends Function {

    private static final int ATTACK_DELAY = 10;
    private static final int MIN_TINKER_MS = 1;
    private static final int MAX_TINKER_MS = 10;
    
    private final MultiSetting targets = new MultiSetting("Цели", "Игроки", "Мобы", "Животные");
    private final BooleanSetting onlyCriticalHit = new BooleanSetting("Только криты", true);
    private final BooleanSetting critsWithSpace = new BooleanSetting("Криты с пробелом", false);
    private final BooleanSetting tinkerTimings = new BooleanSetting("Тайминги", true);
    
    private int sprintStopTicks = 0;
    private int attackCooldown = 0;

    public TriggerBot() {
        addSettings(targets, onlyCriticalHit, critsWithSpace, tinkerTimings);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        sprintStopTicks = 0;
        attackCooldown = 0;
    }

    @EventHandler
    public void onInput(InputEvent event) {
        // Легитный сброс спринта при движении (как в expensive)
        if (sprintStopTicks > 0 && event.getMovementForward() != 0) {
            mc.player.setSprinting(false);
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (nullCheck.all()) return;

        // Уменьшаем счётчики
        if (sprintStopTicks > 0) {
            sprintStopTicks--;
        }
        
        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }

        // Проверяем прицел
        HitResult hit = mc.hitResult;
        if (!(hit instanceof EntityHitResult entityHit)) return;
        
        Entity entity = entityHit.getEntity();
        if (!(entity instanceof LivingEntity living)) return;
        if (living == mc.player || !living.isAlive()) return;
        
        // Проверяем тип цели
        if (!isValidTarget(living)) return;

        // Проверяем можем ли атаковать (с предсказанием на 1 тик)
        if (canAttack(mc.player, 1)) {
            sprintStopTicks = 1;
            mc.player.setSprinting(false);
        }

        // Проверяем финальное условие атаки
        if (!canAttack(mc.player, 0)) return;
        
        // Дополнительная проверка спринта
        if (mc.player.isSprinting()) return;

        // Атакуем
        mc.gameMode.attack(mc.player, entity);
        mc.player.swing(InteractionHand.MAIN_HAND);
        attackCooldown = ATTACK_DELAY;
        
        // Добавляем таймер отката с разбросом 1-10мс
        if (tinkerTimings.isEnabled()) {
            attackCooldown += ThreadLocalRandom.current().nextInt(MIN_TINKER_MS, MAX_TINKER_MS + 1);
        }
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity instanceof Player player) {
            // Всегда проверяем друзей
            if (FriendManager.isFriend(player.getName().getString())) {
                return false;
            }
            return targets.isEnabled("Игроки");
        }
        if (entity instanceof Monster) return targets.isEnabled("Мобы");
        if (entity instanceof Animal) return targets.isEnabled("Животные");
        return false;
    }

    private boolean canAttack(Player player, int ticksAhead) {
        // Проверяем не в инвентаре ли
        if (mc.gui.screen() instanceof AbstractContainerScreen) {
            return false;
        }

        // Проверяем можем ли сделать крит (с предсказанием)
        return canCrit(player, ticksAhead);
    }

    private boolean canCrit(Player player, int ticksAhead) {
        // Проверяем cooldown
        float cooldown = player.getAttackStrengthScale(((float) ticksAhead) + 0.5f);
        if (!(cooldown > 0.9f && attackCooldown - ticksAhead <= 0)) {
            return false;
        }

        // Если не включен режим "только криты" - бьём всегда
        if (!onlyCriticalHit.isEnabled()) {
            return true;
        }

        // Если включен "криты с пробелом" и мы на земле и пробел НЕ зажат - можно бить
        if (critsWithSpace.isEnabled() && player.onGround() && !mc.options.keyJump.isDown()) {
            return true;
        }

        // Проверяем условия для крита
        return hasCritCondition(player);
    }

    private boolean hasCritCondition(Player player) {
        // Проверка эффектов (с ними можно бить на земле)
        if (player.hasEffect(MobEffects.LEVITATION)) return true;
        if (player.hasEffect(MobEffects.BLINDNESS)) return true;
        if (player.hasEffect(MobEffects.SLOW_FALLING)) return true;

        // Проверка жидкостей и блоков
        BlockPos pos = player.blockPosition();
        FluidState fluid = mc.level.getFluidState(pos);
        
        if (fluid.is(FluidTags.WATER)) return true;
        if (fluid.is(FluidTags.LAVA)) return true;
        if (player.onClimbable()) return true;
        if (player.getAbilities().flying) return true;

        // Главное условие: не на земле И падаем
        if (!player.onGround() && player.fallDistance > 0.0f) {
            return true;
        }

        return false;
    }
}
