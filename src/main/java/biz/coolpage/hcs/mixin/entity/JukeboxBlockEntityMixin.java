package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_LIKE;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity {
    @Mutable
    @Final
    @Shadow
    private final NonNullList<ItemStack> items;

    public JukeboxBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state, NonNullList<ItemStack> items) {
        super(type, pos, state);
        this.items = items;
    }

    @Inject(at = @At("RETURN"), method = "isRecordPlaying")
    public void isRecordPlaying(@NotNull CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            if (this.getLevel() instanceof ServerLevel serverWorld && this.worldPosition != null) {
                serverWorld.players().forEach(player -> {
                    if (EntityHelper.isExistent(player) && IS_SURVIVAL_LIKE.test(player)) {
                        double distance = Math.sqrt(player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5));
                        ItemStack stack = this.items.get(0); // todo test here
                        if (distance < 24 && stack != null) {
                            // Listening to music restores or reduces sanity
                            double sanChange = stack.is(Items.MUSIC_DISC_5) || stack.is(Items.MUSIC_DISC_11) || stack.is(Items.MUSIC_DISC_13) ? -0.00005 : 0.0001;
                            ((StatAccessor) player).getSanityManager().add(sanChange * Math.max(1 - distance / 24, 0.0));
                        }
                    }
                });
            }
        }
    }
}
