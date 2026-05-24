package biz.coolpage.hcs.mixin.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreFeature.class)
public class OreFeatureMixin {
    // Prevent infinite recursion when manually triggering extra vein generation
    @Unique
    private static final ThreadLocal<Boolean> HCS_IS_EXTRA_VEIN = ThreadLocal.withInitial(() -> false);

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void hcsurvival$modifyOreGeneration(@NotNull FeaturePlaceContext<OreConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        OreConfiguration config = context.config();
        BlockPos pos = context.origin();
        int y = pos.getY();

        boolean isIron = false;
        boolean isCopper = false;
        boolean isCoal = false;

        // Identify the target blocks for this ore feature
        for (OreConfiguration.TargetBlockState target : config.targetStates) {
            BlockState state = target.state;
            if (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)) {
                isIron = true;
            } else if (state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE)) {
                isCopper = true;
            } else if (state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)) {
                isCoal = true;
            }
        }

        var random = context.random();

        // Reduce iron ore generation probability to 20% at Y > 30
        if (isIron && y > 30) {
            // 80% chance to cancel the generation, effectively leaving 20%
            if (random.nextFloat() >= 0.20F) {
                cir.setReturnValue(false);
                return;
            }
        }

        // Determine if this placement should trigger an extra generation chance
        float extraChance = 0.0F;

        // All ores at Y < -40 have a 1.4x generation rate (40% chance for an extra vein)
        if (y < -40) {
            extraChance = 0.4F;
        }
        // Copper and Coal between Y=35 and Y=50 have a 1.5x generation rate (50% chance for an extra vein)
        else if ((isCopper || isCoal) && y >= 35 && y <= 50) {
            extraChance = 0.5F;
        }

        // Trigger the extra vein placement if applicable and not already in an extra placement cycle
        if (extraChance > 0.0F && !HCS_IS_EXTRA_VEIN.get()) {
            if (random.nextFloat() < extraChance) {
                HCS_IS_EXTRA_VEIN.set(true);

                // Offset the origin by random blocks within the chunk range
                int dx = random.nextInt(16) - 8;
                int dy = random.nextInt(20) - 10;
                int dz = random.nextInt(16) - 8;
                BlockPos extraPos = pos.offset(dx, dy, dz);

                // Create a new context for the extra vein
                FeaturePlaceContext<OreConfiguration> extraContext = new FeaturePlaceContext<>(
                        context.topFeature(), context.level(), context.chunkGenerator(),
                        random, extraPos, config
                );

                // Call the original place method manually for the extra vein
                ((OreFeature) (Object) this).place(extraContext);

                HCS_IS_EXTRA_VEIN.set(false);
            }
        }
    }
}