package biz.coolpage.hcs.mixin.entity.player;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.item.HotWaterBottleItem;
import biz.coolpage.hcs.status.HcsPersistentState;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StatusManager;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.block.TorchBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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

    @SuppressWarnings("CommentedOutCode")
    @Inject(method = "updateItems", at = @At("HEAD"))
    public void updateItems(CallbackInfo ci) {
        PlayerInventory inv = this.player.getInventory();
        RotHelper.update(this.player.world, inv);
        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        HotWaterBottleItem.update(this.player.world, inv, temperatureManager.getTrendType());
        boolean isSubmerged = player.isSubmergedInWater(), soundHasNotPlayedYet = true;
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
            // Torches in players' inventories will extinguish when they submerge in water
            boolean isTorch = item == Items.TORCH, isBurningCrudeTorch = item == Reg.BURNING_CRUDE_TORCH_ITEM;
            if (isSubmerged && (isTorch || isBurningCrudeTorch)) {
                inv.setStack(i, new ItemStack(isTorch ? Reg.UNLIT_TORCH_ITEM : Reg.CRUDE_TORCH_ITEM, stack.getCount()));
                if (soundHasNotPlayedYet) {
                    player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS);
                    soundHasNotPlayedYet = false;
                }
            }
        }
        statusManager.setHasHeavyLoadDebuff(IS_SURVIVAL_AND_SERVER.test(this.player) && blocksCount > 128);
    }
}
