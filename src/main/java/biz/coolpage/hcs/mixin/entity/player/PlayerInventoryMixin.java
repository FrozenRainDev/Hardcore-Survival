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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.TorchBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;
import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_AND_SERVER;

@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin {
    @Final
    @Shadow
    public Player player;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        Inventory inv = this.player.getInventory();
        RotHelper.update(this.player.level(), inv);
        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        HotWaterBottleItem.update(this.player.level(), inv, temperatureManager.getTrendType());
        int blocksCount = 0;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            Item item = stack.getItem();

            if (item instanceof BlockItem blockItem && !(blockItem.getBlock() instanceof TorchBlock))
                blocksCount += stack.getCount();
            else if ((stack.is(Reg.COPPER_PICKAXE) || stack.is(Items.IRON_PICKAXE)) && player.level() instanceof ServerLevel serverWorld)
                applyNullable(HcsPersistentState.getServerState(serverWorld), state -> {
                    state.setHasObtainedCopperPickaxe(true);
                    state.setDirty();
                });

            if (stack.getDamageValue() > stack.getMaxDamage()) stack.setDamageValue(stack.getMaxDamage() - 1);

            boolean isBurningCrudeTorch = item == Reg.BURNING_CRUDE_TORCH_ITEM, isFuelableCampfire = CombustionHelper.isFuelableCampfire(item);
            if ((isBurningCrudeTorch || isFuelableCampfire) && !stack.getOrCreateTag().contains(BurningCrudeTorchItem.EXTINGUISH_NBT))
                BurningCrudeTorchItem.initDurData(player.level(), stack);
            // Also see CombustionHelper::inventoryTick
        }

        // VERY CRITICAL!!! MUST call AFTER BurningCrudeTorchItem::initDurData, otherwise the stack is invalid, and then extinguish
        CombustionHelper.inventoryTick(player.isUnderWater(), inv, player);

        statusManager.setHasHeavyLoadDebuff(IS_SURVIVAL_AND_SERVER.test(this.player) && blocksCount > 128);
    }
}
