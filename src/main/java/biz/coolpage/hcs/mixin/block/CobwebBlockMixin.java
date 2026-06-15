package biz.coolpage.hcs.mixin.block;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WebBlock.class)
public abstract class CobwebBlockMixin {
    @Inject(method = "entityInside", at = @At("TAIL"))
    private void hcs$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        // Apply vanilla cobweb stuck behavior to projectiles
        if (entity instanceof Projectile projectile) {
            System.out.println(projectile.toString());
            projectile.makeStuckInBlock(state, new Vec3(0.001D, 0.001D, 0.001D));
        }
    }
}