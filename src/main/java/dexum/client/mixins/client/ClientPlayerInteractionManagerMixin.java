package dexum.client.mixins.client;

import dexum.client.interfaces.IClientPlayerInteractionManager;
import dexum.client.system.events.EventBus;
import dexum.client.system.events.impl.AttackEvent;
import dexum.client.system.features.combat.Aura;
import dexum.client.system.features.player.NoInteract;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin implements IClientPlayerInteractionManager {

    // "destroyDelay" in MC 26.2 (confirmed from Lenrete FastBreakModule)
    private static final Field DESTROY_DELAY = resolveField();
    private static Field resolveField() {
        for (String name : new String[]{"destroyDelay", "blockBreakingCooldown", "breakCooldown"}) {
            try {
                Field f = MultiPlayerGameMode.class.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }

    @Override
    public void setBlockBreakingCooldown(int val) {
        if (DESTROY_DELAY == null) return;
        try { DESTROY_DELAY.setInt(this, val); } catch (IllegalAccessException ignored) {}
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true, require = 0)
    private void onInteractBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult,
                                  CallbackInfoReturnable<InteractionResult> cir) {
        NoInteract module = NoInteract.Instance;
        if (module == null || !module.isEnabled() || !module.blocks.isEnabled()) return;
        if (module.onlyDuringAura.isEnabled()) {
            Aura aura = Aura.Instance;
            if (aura == null || !aura.isEnabled() || !aura.hasTarget()) return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var block = mc.level.getBlockState(hitResult.getBlockPos()).getBlock();
        if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.FURNACE
                || block == Blocks.BLAST_FURNACE || block == Blocks.SMOKER
                || block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL
                || block == Blocks.CRAFTING_TABLE || block == Blocks.HOPPER
                || block == Blocks.ENDER_CHEST || block == Blocks.DISPENSER || block == Blocks.DROPPER
                || block == Blocks.BARREL || block == Blocks.BREWING_STAND || block == Blocks.ENCHANTING_TABLE
                || block instanceof net.minecraft.world.level.block.ShulkerBoxBlock
                || block instanceof FenceBlock || block instanceof FenceGateBlock
                || block instanceof TrapDoorBlock || block instanceof DoorBlock) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true, require = 0)
    private void onInteractEntity(Player player, Entity entity,
                                   net.minecraft.world.phys.EntityHitResult hitResult,
                                   InteractionHand hand,
                                   CallbackInfoReturnable<InteractionResult> cir) {
        NoInteract module = NoInteract.Instance;
        if (module == null || !module.isEnabled() || !module.entities.isEnabled()) return;
        if (module.onlyDuringAura.isEnabled()) {
            Aura aura = Aura.Instance;
            if (aura == null || !aura.isEnabled() || !aura.hasTarget()) return;
        }
        if (entity instanceof VehicleEntity || entity instanceof ArmorStand) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true, require = 0)
    private void onAttackEntity(Player player, Entity target, CallbackInfo ci) {
        if (!(target instanceof LivingEntity living)) return;
        AttackEvent event = new AttackEvent(living);
        EventBus.post(event);
        if (event.isCancelled()) ci.cancel();
    }
}
