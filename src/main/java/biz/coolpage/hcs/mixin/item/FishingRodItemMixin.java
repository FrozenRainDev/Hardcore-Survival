package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
public abstract class FishingRodItemMixin extends Item {
    public FishingRodItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(World world, @NotNull PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack itemStack = user.getStackInHand(hand);
        ItemStack offStack = user.getOffHandStack();
        Item offItem = offStack.getItem();
        if (user.fishHook == null && EntityHelper.IS_SURVIVAL_LIKE.test(user)) {
            if (offItem == Reg.WORM || offItem == Reg.ROASTED_WORM
                    || offItem == Reg.ANIMAL_VISCERA || offItem == Reg.COOKED_ANIMAL_VISCERA
                    || offItem == Items.WHEAT || offItem == Reg.ROASTED_SEEDS
                    || offItem == Reg.RAW_MEAT || offItem == Reg.COOKED_MEAT
                    || offItem == Items.CHICKEN || offItem == Items.COOKED_CHICKEN
                    || offItem == Items.BEEF || offItem == Items.COOKED_BEEF
                    || offItem == Items.PORKCHOP || offItem == Items.COOKED_PORKCHOP
                    || offItem == Items.MUTTON || offItem == Items.COOKED_MUTTON
                    || offItem == Items.RABBIT || offItem == Items.COOKED_RABBIT || offItem == Items.ROTTEN_FLESH
            ) {
                offStack.decrement(1);
            } else {
                EntityHelper.msgById(user, "hcs.tip.need_bait");
                cir.setReturnValue(TypedActionResult.fail(itemStack));
            }
        }
    }
}
