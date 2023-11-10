package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_LIKE;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity implements SingleStackInventory {
    @Shadow
    public abstract ItemStack getStack(int slot);


    public JukeboxBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(at = @At("RETURN"), method = "isPlayingRecord")
    public void isPlayingRecord(@NotNull CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            if (this.world instanceof ServerWorld serverWorld && this.pos != null) {
                serverWorld.getPlayers().forEach(player -> {
                    if (EntityHelper.isExistent(player) && IS_SURVIVAL_LIKE.test(player)) {
                        double distance = Math.sqrt(player.squaredDistanceTo(this.pos.toCenterPos()));
                        var stack = this.getStack();
                        if (distance < 24 && stack != null) {
                            double sanChange = stack.isOf(Items.MUSIC_DISC_5) || stack.isOf(Items.MUSIC_DISC_11) || stack.isOf(Items.MUSIC_DISC_13) ? -0.00005 : 0.0001;
                            ((StatAccessor) player).getSanityManager().add(sanChange * Math.max(1 - distance / 24, 0.0));
                        }
                    }
                });
            }
        }
    }
}
