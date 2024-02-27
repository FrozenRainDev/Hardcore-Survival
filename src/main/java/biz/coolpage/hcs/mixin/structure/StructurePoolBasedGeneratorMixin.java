package biz.coolpage.hcs.mixin.structure;

import biz.coolpage.hcs.util.CommUtil;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(StructurePoolBasedGenerator.class)
public class StructurePoolBasedGeneratorMixin {
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
    @Inject(method = "generate(Lnet/minecraft/world/gen/structure/Structure$Context;Lnet/minecraft/registry/entry/RegistryEntry;Ljava/util/Optional;ILnet/minecraft/util/math/BlockPos;ZLjava/util/Optional;I)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    // Still has another public generate method not mixed in
    private static void generate(Structure.@NotNull Context context, @NotNull RegistryEntry<StructurePool> structurePool, Optional<Identifier> id, int size, BlockPos pos, boolean useExpansionHack, Optional<Heightmap.Type> projectStartToHeightmap, int maxDistanceFromCenter, CallbackInfoReturnable<Optional<Structure.StructurePosition>> cir) {
        if (CommUtil.regEntryContains(structurePool, "village") && !WorldHelper.shouldGenerateVillages())
            cir.setReturnValue(Optional.empty());
    }

    @Inject(method = "generate(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/util/Identifier;ILnet/minecraft/util/math/BlockPos;Z)Z", at = @At("HEAD"), cancellable = true)
    private static void generate(ServerWorld world, RegistryEntry<StructurePool> structurePool, Identifier id, int size, BlockPos pos, boolean keepJigsaws, CallbackInfoReturnable<Boolean> cir) {
        if (CommUtil.regEntryContains(structurePool, "village") && !WorldHelper.shouldGenerateVillages())
            cir.setReturnValue(false);
    }

    @SuppressWarnings("unused")
    @Mixin(StructurePoolBasedGenerator.StructurePoolGenerator.class)
    private static class StructurePoolGeneratorMixin {
        @Inject(method = "generatePiece", at = @At("HEAD"), cancellable = true)
        void generatePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int minY, boolean modifyBoundingBox, HeightLimitView world, NoiseConfig noiseConfig, CallbackInfo ci) {
            if (piece == null) return;
            StructurePoolElement element = piece.getPoolElement();
            if (element == null) return;
            StructurePoolElementType<?> type = element.getType();
            if (type == null) return;
            if (type.toString().contains("village") && !WorldHelper.shouldGenerateVillages())
                ci.cancel();
        }
    }
}
