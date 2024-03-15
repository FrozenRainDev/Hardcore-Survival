package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.item.HotWaterBottleItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {
    @Inject(method = "litServerTick", at = @At("HEAD"))
    private static void litServerTickInjected1(@NotNull World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        if (world.getRandom().nextFloat() < 0.001F) // Lit inflammable blocks nearby
            Fluids.LAVA.onRandomTick(world, pos, Fluids.LAVA.getDefaultState(), world.getRandom());
    }

    @Inject(method = "litServerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ItemScatterer;spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void litServerTickInjected2(World world, BlockPos pos, @NotNull BlockState state, CampfireBlockEntity campfire, CallbackInfo ci, boolean bl, int i, ItemStack itemStack, Inventory inventory, ItemStack itemStack2) {
        if (state.isOf(Blocks.SOUL_CAMPFIRE) && itemStack2.isOf(Reg.HOT_WATER_BOTTLE))
            HotWaterBottleItem.setStatus(itemStack2, -1); // Soul campfire will cool temp down
    }
}
