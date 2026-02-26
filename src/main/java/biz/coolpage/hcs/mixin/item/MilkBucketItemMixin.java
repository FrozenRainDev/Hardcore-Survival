package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.level.Level;
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
        if (mainHand.is(Items.MILK_BUCKET)) return mainHand;
        if (offHand.is(Items.MILK_BUCKET)) return offHand;
        Reg.LOGGER.error("MilkBucketItemMixin/judgeMilkStack/mainHand,offHand!=MilkBucket");
        return new ItemStack(Items.AIR);
    }

    @Inject(method = "use", at = @At("HEAD"))
    private void use(Level world, @NotNull Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        oriStack = judgeMilkStack(user.getMainHandItem(), user.getOffhandItem()).copy();
    }

    @Inject(method = "finishUsingItem", at = @At("RETURN"))
    public void finishUsing(ItemStack stack, Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof ServerPlayer player) {
            FoodData foodData = player.getFoodData();
            foodData.setExhaustion(0.0F);
            int freshLevel = RotHelper.getFreshLevel(RotHelper.getFresh(world, oriStack));
            foodData.setFoodLevel(Math.min(20, foodData.getFoodLevel() + Math.min(freshLevel * (freshLevel > 1 ? 2 : 1), 5)));
            ((StatAccessor) user).getThirstManager().add(1.0);
            RotHelper.addDebuff(world, player, oriStack);
            EntityHelper.checkOvereaten(player, true);
        }
    }
}