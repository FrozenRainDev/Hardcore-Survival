package biz.coolpage.hcs.mixin.structure;

import net.minecraft.structure.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(RuinedPortalStructurePiece.class)
public abstract class RuinedPortalStructurePieceMixin extends SimpleStructurePiece {
    public RuinedPortalStructurePieceMixin(StructurePieceType type, int length, StructureTemplateManager structureTemplateManager, Identifier id, String template, StructurePlacementData placementData, BlockPos pos) {
        super(type, length, structureTemplateManager, id, template, placementData, pos);
    }

    @Inject(method = "canFillNetherrack", at = @At("HEAD"), cancellable = true)
    private void canFillNetherrack(WorldAccess world, BlockPos pos, @NotNull CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "placeNetherrackBase", at = @At("HEAD"), cancellable = true)
    private void placeNetherrackBase(Random random, WorldAccess world, @NotNull CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "updateNetherracksInBound", at = @At("HEAD"), cancellable = true)
    private void updateNetherracksInBound(Random random, WorldAccess world, @NotNull CallbackInfo ci) {
        ci.cancel();
    }

}
