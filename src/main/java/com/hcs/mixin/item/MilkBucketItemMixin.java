package com.hcs.mixin.item;

import com.hcs.main.Reg;
import com.hcs.main.helper.EntityHelper;
import com.hcs.main.helper.RotHelper;
import com.hcs.main.manager.StatusManager;
import com.hcs.misc.HcsEffects;
import com.hcs.misc.accessor.StatAccessor;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public class MilkBucketItemMixin {
    private ItemStack oriStack = new ItemStack(Items.BUCKET);

    private @NotNull ItemStack judgeMilkStack(@NotNull ItemStack mainHand, ItemStack offHand) {
        if (mainHand.isOf(Items.MILK_BUCKET)) return mainHand;
        if (offHand.isOf(Items.MILK_BUCKET)) return offHand;
        Reg.LOGGER.error("MilkBucketItemMixin/judgeMilkStack/mainHand,offHand!=MilkBucket");
        return new ItemStack(Items.AIR);
    }

    @Inject(at = @At("HEAD"), method = "use")
    private void use(World world, @NotNull PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        oriStack = judgeMilkStack(user.getMainHandStack(), user.getOffHandStack()).copy();
    }

    @Inject(at = @At("RETURN"), method = "finishUsing")
    public void finishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof ServerPlayerEntity player) {
            HungerManager hm = player.getHungerManager();
            hm.setExhaustion(0.0F);
            hm.setFoodLevel(Math.min(20, hm.getFoodLevel() + Math.min(RotHelper.getFreshLevel(RotHelper.getFresh(world, oriStack)), 3)));
            ((StatAccessor) user).getThirstManager().add(1.0F);
            RotHelper.addDebuff(world, player, oriStack);
            EntityHelper.checkOvereaten(player, true);
        }
    }

}
