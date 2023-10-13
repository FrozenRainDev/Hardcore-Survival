package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlassBottleItem.class)
public class GlassBottleItemMixin {
    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        BlockHitResult hitResult = EntityHelper.rayCast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY, 2.5);
        ItemStack itemStack = user.getStackInHand(hand);
        if (((HitResult) hitResult).getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = hitResult.getBlockPos();
            if (WorldHelper.IS_SALTY_WATER_BIOME.test(world.getBiome(blockPos)) && world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
                world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                world.emitGameEvent(user, GameEvent.FLUID_PICKUP, blockPos);
                cir.setReturnValue(TypedActionResult.success(this.fill(itemStack, user, Reg.SALTWATER_BOTTLE.getDefaultStack()), world.isClient()));
                cir.cancel();
            }
        }
    }

    @Shadow
    protected ItemStack fill(ItemStack stack, @NotNull PlayerEntity player, ItemStack outputStack) {
        player.incrementStat(Stats.USED.getOrCreateStat((GlassBottleItem) (Object) this));
        return ItemUsage.exchangeStack(stack, player, outputStack);
    }

}
