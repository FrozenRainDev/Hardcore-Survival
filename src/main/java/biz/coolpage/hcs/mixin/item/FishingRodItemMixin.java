package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
public abstract class FishingRodItemMixin extends Item {
    public FishingRodItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(Level level, @NotNull Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack itemStack = user.getItemInHand(hand);
        ItemStack offStack = user.getOffhandItem();
        Item offItem = offStack.getItem();

        // fishHook -> fishing (Mojang)
        if (user.fishing == null && EntityHelper.IS_SURVIVAL_LIKE.test(user)) {
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
                // decrement -> shrink (Mojang)
                offStack.shrink(1);
            } else {
                EntityHelper.msgById(user, "hcs.tip.need_bait");
                cir.setReturnValue(InteractionResultHolder.fail(itemStack));
            }
        }
    }
}