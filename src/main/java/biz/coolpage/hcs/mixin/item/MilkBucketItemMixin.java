package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public class MilkBucketItemMixin {
    @Unique
    private ItemStack oriStack = new ItemStack(Items.BUCKET);

    @Unique
    private @NotNull ItemStack judgeMilkStack(@NotNull ItemStack mainHand, ItemStack offHand) {
        if (mainHand.isOf(Items.MILK_BUCKET)) return mainHand;
        if (offHand.isOf(Items.MILK_BUCKET)) return offHand;
        Reg.LOGGER.error("MilkBucketItemMixin/judgeMilkStack/mainHand,offHand!=MilkBucket");
        return new ItemStack(Items.AIR);
    }

    @Inject(method = "use", at = @At("HEAD"))
    private void use(World world, @NotNull PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        oriStack = judgeMilkStack(user.getMainHandStack(), user.getOffHandStack()).copy();
    }

    @Inject(method = "finishUsing", at = @At("RETURN"))
    public void finishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof ServerPlayerEntity player) {
            HungerManager hm = player.getHungerManager();
            hm.setExhaustion(0.0F);
            int freshLevel = RotHelper.getFreshLevel(RotHelper.getFresh(world, oriStack));
            hm.setFoodLevel(Math.min(20, hm.getFoodLevel() + Math.min(freshLevel * (freshLevel > 1 ? 2 : 1), 5)));
            ((StatAccessor) user).getThirstManager().add(1.0);
            RotHelper.addDebuff(world, player, oriStack);
            EntityHelper.checkOvereaten(player, true);
        }
    }

}
