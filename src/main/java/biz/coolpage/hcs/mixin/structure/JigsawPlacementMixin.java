package biz.coolpage.hcs.mixin.structure;

import biz.coolpage.hcs.util.CommUtil;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(JigsawPlacement.class)
public class JigsawPlacementMixin {

    @Inject(method = "addPieces(Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;Lnet/minecraft/core/Holder;Ljava/util/Optional;ILnet/minecraft/core/BlockPos;ZLjava/util/Optional;I)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void hcs$addPieces(Structure.GenerationContext context, @NotNull Holder<StructureTemplatePool> structurePool, Optional<ResourceLocation> id, int size, BlockPos pos, boolean useExpansionHack, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter, CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
        if (CommUtil.regEntryContains(structurePool, "village") && !WorldHelper.shouldGenerateVillages()) {
            cir.setReturnValue(Optional.empty());
        }
    }

    @Inject(method = "generateJigsaw(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/Holder;Lnet/minecraft/resources/ResourceLocation;ILnet/minecraft/core/BlockPos;Z)Z", at = @At("HEAD"), cancellable = true)
    private static void hcs$generateJigsaw(ServerLevel world, Holder<StructureTemplatePool> structurePool, ResourceLocation id, int size, BlockPos pos, boolean keepJigsaws, CallbackInfoReturnable<Boolean> cir) {
        if (CommUtil.regEntryContains(structurePool, "village") && !WorldHelper.shouldGenerateVillages()) {
            cir.setReturnValue(false);
        }
    }

    // 内部类 Placer 的 Mixin
    @Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
    private static class PlacerMixin {
        // 修正点：返回类型 void 改为 V
        @Inject(
                method = "tryPlacingChildren(Lnet/minecraft/world/level/levelgen/structure/PoolElementStructurePiece;Lorg/apache/commons/lang3/mutable/MutableObject;IZLnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/world/level/levelgen/RandomState;)V",
                at = @At("HEAD"),
                cancellable = true
        )
        private void hcs$tryPlacingChildren(PoolElementStructurePiece piece, MutableObject<VoxelShape> pieceShape, int depth, boolean useExpansionHack, LevelHeightAccessor world, RandomState randomState, CallbackInfo ci) {
            if (piece == null) return;
            StructurePoolElement element = piece.getElement();
            if (element == null) return;
            StructurePoolElementType<?> type = element.getType();
            if (type != null && type.toString().contains("village") && !WorldHelper.shouldGenerateVillages()) {
                ci.cancel();
            }
        }
    }
}