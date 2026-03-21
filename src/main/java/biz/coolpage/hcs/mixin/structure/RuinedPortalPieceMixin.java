package biz.coolpage.hcs.mixin.structure;

import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RuinedPortalPiece.class)
public abstract class RuinedPortalPieceMixin extends TemplateStructurePiece {
    // 冗余构造函数，保持原有结构
    public RuinedPortalPieceMixin(StructurePieceType type, int length, StructureTemplateManager structureTemplateManager, ResourceLocation id, String template, StructurePlaceSettings placementData, BlockPos pos) {
        super(type, length, structureTemplateManager, id, template, placementData, pos);
    }

    /**
     * 对应 1.20.1 Mojang 映射中的 canBlockBeReplacedByNetherrackOrMagma
     */
    @Inject(method = "canBlockBeReplacedByNetherrackOrMagma", at = @At("HEAD"), cancellable = true)
    private void canFillNetherrack(LevelAccessor world, BlockPos pos, @NotNull CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    /**
     * 对应 1.20.1 Mojang 映射中的 addNetherrackDripColumnsBelowPortal
     */
    @Inject(method = "addNetherrackDripColumnsBelowPortal", at = @At("HEAD"), cancellable = true)
    private void placeNetherrackBase(RandomSource random, LevelAccessor world, @NotNull CallbackInfo ci) {
        ci.cancel();
    }

    /**
     * 对应 1.20.1 Mojang 映射中的 spreadNetherrack
     */
    @Inject(method = "spreadNetherrack", at = @At("HEAD"), cancellable = true)
    private void updateNetherracksInBound(RandomSource random, LevelAccessor world, @NotNull CallbackInfo ci) {
        ci.cancel();
    }

}