package biz.coolpage.hcs.mixin.entity.player;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.item.BurningCrudeTorchItem;
import biz.coolpage.hcs.item.HotWaterBottleItem;
import biz.coolpage.hcs.status.HcsPersistentState;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StatusManager;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import biz.coolpage.hcs.util.CombustionHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.block.TorchBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;
import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_AND_SERVER;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Final
    @Shadow
    public PlayerEntity player;

    @Inject(method = "updateItems", at = @At("HEAD"))
    public void updateItems(CallbackInfo ci) {
        PlayerInventory inv = this.player.getInventory();
        RotHelper.update(this.player.world, inv);
        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        HotWaterBottleItem.update(this.player.world, inv, temperatureManager.getTrendType());
        int blocksCount = 0;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getStack(i);
            Item item = stack.getItem();

            if (item instanceof BlockItem blockItem && !(blockItem.getBlock() instanceof TorchBlock))
                blocksCount += stack.getCount();
            else if ((stack.isOf(Reg.COPPER_PICKAXE) || stack.isOf(Items.IRON_PICKAXE)) && player.world instanceof ServerWorld serverWorld)
                applyNullable(HcsPersistentState.getServerState(serverWorld), state -> {
                    state.setHasObtainedCopperPickaxe(true);
                    state.markDirty();
                });

            if (stack.getDamage() > stack.getMaxDamage()) stack.setDamage(stack.getMaxDamage() - 1);

            boolean isBurningCrudeTorch = item == Reg.BURNING_CRUDE_TORCH_ITEM, isFuelableCampfire = CombustionHelper.isFuelableCampfire(item);
            if ((isBurningCrudeTorch || isFuelableCampfire) && !stack.getOrCreateNbt().contains(BurningCrudeTorchItem.EXTINGUISH_NBT))
                BurningCrudeTorchItem.initDurData(player.world, stack);
            // Also see CombustionHelper::inventoryTick
        }

        // VERY CRITICAL!!! MUST call AFTER BurningCrudeTorchItem::initDurData, otherwise the stack is invalid, and then extinguish
        CombustionHelper.inventoryTick(player.isSubmergedInWater(), inv, player);

        statusManager.setHasHeavyLoadDebuff(IS_SURVIVAL_AND_SERVER.test(this.player) && blocksCount > 128);
    }
}
