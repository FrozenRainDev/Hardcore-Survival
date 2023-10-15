package biz.coolpage.hcs.mixin.entity.player;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.item.HotWaterBottleItem;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.HcsPersistentState;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
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

    @SuppressWarnings("CommentedOutCode")
    @Inject(method = "updateItems", at = @At("HEAD"))
    public void updateItems(CallbackInfo ci) {
        PlayerInventory inv = this.player.getInventory();
        RotHelper.update(this.player.world, inv);
        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
        HotWaterBottleItem.update(this.player.world, inv, temperatureManager.getTrendType());
        int blocksCount = 0;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getStack(i);
            if (stack.getItem() instanceof BlockItem) blocksCount += stack.getCount();
            if (stack.isOf(Reg.COPPER_PICKAXE) && player.world instanceof ServerWorld serverWorld)
                applyNullable(HcsPersistentState.getServerState(serverWorld), state -> state.hasObtainedCopperPickaxe = true);
        }
        if (blocksCount > 128 && IS_SURVIVAL_AND_SERVER.test(this.player))
            EntityHelper.addHcsDebuff(this.player, HcsEffects.HEAVY_LOAD);
    }
}
