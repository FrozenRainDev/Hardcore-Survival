package biz.coolpage.hcs.mixin.item;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {

    public BlockItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
    public void useOnBlock(@NotNull ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (context.getStack().getItem() == Items.SWEET_BERRIES) {
            ActionResult useActionResult = this.use(context.getWorld(), context.getPlayer(), context.getHand()).getResult();
            cir.setReturnValue(useActionResult == ActionResult.CONSUME ? ActionResult.CONSUME_PARTIAL : useActionResult);
        }
    }


}
