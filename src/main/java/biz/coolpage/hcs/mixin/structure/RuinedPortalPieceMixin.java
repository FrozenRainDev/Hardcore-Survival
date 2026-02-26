package biz.coolpage.hcs.mixin.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RuinedPortalPiece.class)
public abstract class RuinedPortalPieceMixin extends TemplateStructurePiece {
    public RuinedPortalPieceMixin(StructurePieceType type, int length, StructureTemplateManager structureTemplateManager, ResourceLocation id, String template, StructurePlaceSettings placementData, BlockPos pos) {
        super(type, length, structureTemplateManager, id, template, placementData, pos);
    }

    @Inject(method = "canFillNetherrack", at = @At("HEAD"), cancellable = true)
    private void canFillNetherrack(LevelAccessor world, BlockPos pos, @NotNull CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "placeNetherrackBase", at = @At("HEAD"), cancellable = true)
    private void placeNetherrackBase(RandomSource random, LevelAccessor world, @NotNull CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "updateNetherracksInBound", at = @At("HEAD"), cancellable = true)
    private void updateNetherracksInBound(RandomSource random, LevelAccessor world, @NotNull CallbackInfo ci) {
        ci.cancel();
    }
}