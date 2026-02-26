package biz.coolpage.hcs.mixin.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {

    public BlockItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(at = @At("HEAD"), method = "useOn", cancellable = true)
    public void useOn(@NotNull UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        // Yarn: getStack() -> Mojang: getItemInHand()
        if (context.getItemInHand().getItem() == Items.SWEET_BERRIES) {
            // Yarn: getWorld() -> Mojang: getLevel()
            // InteractionResultHolder.getResult() 保持不变
            InteractionResult interactionResult = this.use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();

            // Yarn: CONSUME_PARTIAL 逻辑转换
            // 在 1.20.1 Mojang 映射中，CONSUME_PARTIAL 的行为最接近 InteractionResult.SUCCESS
            if (interactionResult == InteractionResult.CONSUME) {
                cir.setReturnValue(InteractionResult.SUCCESS);
            } else {
                cir.setReturnValue(interactionResult);
            }
        }
    }
}