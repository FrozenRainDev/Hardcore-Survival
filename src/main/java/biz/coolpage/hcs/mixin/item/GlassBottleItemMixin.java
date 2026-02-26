package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
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

// GlassBottleItem -> BottleItem (Mojang)
@Mixin(BottleItem.class)
public abstract class GlassBottleItemMixin {

    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    public void use(Level level, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        // RaycastContext -> ClipContext (Mojang)
        BlockHitResult hitResult = EntityHelper.rayCast(level, user, ClipContext.Fluid.SOURCE_ONLY, 2.5);
        ItemStack itemStack = user.getItemInHand(hand);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = hitResult.getBlockPos();
            if (WorldHelper.IS_SALTY_WATER_BIOME.test(level.getBiome(blockPos)) && level.getFluidState(blockPos).is(FluidTags.WATER)) {
                // SoundCategory -> SoundSource (Mojang)
                level.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0f, 1.0f);
                level.gameEvent(user, GameEvent.FLUID_PICKUP, blockPos);
                cir.setReturnValue(InteractionResultHolder.success(this.fill(itemStack, user, Reg.SALTWATER_BOTTLE.getDefaultInstance()), level.isClientSide()));
                cir.cancel();
            }
        }
    }

    @Shadow
    protected abstract ItemStack fill(ItemStack stack, @NotNull Player player, ItemStack outputStack);

    // 注意：Shadow 方法内部逻辑如果需要转换，ItemUsage.exchangeStack -> ItemUtils.createFilledResult
    // 但因为是Shadow，原类已有实现，这里仅在需要覆盖逻辑时修改。
}