package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BottleItem.class)
public abstract class GlassBottleItemMixin {

    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    public void use(Level level, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        BlockHitResult hitResult = EntityHelper.rayCast(level, user, ClipContext.Fluid.SOURCE_ONLY, 2.5);
        ItemStack itemStack = user.getItemInHand(hand);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = hitResult.getBlockPos();
            if (WorldHelper.IS_SALTY_WATER_BIOME.test(level.getBiome(blockPos)) && level.getFluidState(blockPos).is(FluidTags.WATER)) {
                level.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0f, 1.0f);
                level.gameEvent(user, GameEvent.FLUID_PICKUP, blockPos);
                // 修复点：将 InteractionResultHolder.success 改为 InteractionResultHolder.sidedSuccess
                cir.setReturnValue(InteractionResultHolder.sidedSuccess(this.turnBottleIntoItem(itemStack, user, Hcs.SALTWATER_BOTTLE.getDefaultInstance()), level.isClientSide()));
                cir.cancel();
            }
        }
    }

    // Mojang 映射中对应的 Shadow 方法名
    @Shadow
    protected abstract ItemStack turnBottleIntoItem(ItemStack stack, @NotNull Player player, ItemStack outputStack);
}