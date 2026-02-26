package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.entity.goal.SpiderEscapeDangerGoal;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Spider.class)
public abstract class SpiderEntityMixin extends Monster {
    protected SpiderEntityMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    @Unique
    private int webbingCooldown = 0;

    @Inject(method = "registerGoals", at = @At("HEAD"))
    protected void registerGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(1, new SpiderEscapeDangerGoal(this));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        LivingEntity target = this.getTarget();
        if (this.webbingCooldown < 1) {
            if (EntityHelper.isExistent(target, this) && this.distanceTo(target) < 1.5F) {
                this.webbingCooldown = 200;
                BlockState state = this.level().getBlockState(this.blockPosition());
                Block block = state.getBlock();
                if (state.canBeReplaced() || block == Blocks.AIR)
                    if (target != null) this.level().setBlockAndUpdate(target.blockPosition(), Blocks.COBWEB.defaultBlockState());
            }
        } else --this.webbingCooldown;
    }
}
