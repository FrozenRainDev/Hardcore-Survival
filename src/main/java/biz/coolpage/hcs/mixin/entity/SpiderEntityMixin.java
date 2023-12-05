package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.entity.goal.SpiderEscapeDangerGoal;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpiderEntity.class)
public abstract class SpiderEntityMixin extends HostileEntity {
    protected SpiderEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    private int webbingCooldown = 0;

    @Inject(method = "initGoals", at = @At("HEAD"))
    protected void initGoals(CallbackInfo ci) {
        this.goalSelector.add(1, new SpiderEscapeDangerGoal(this));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        LivingEntity target = this.getTarget();
        if (this.webbingCooldown < 1) {
            if (EntityHelper.isExistent(target, this) && this.distanceTo(target) < 1.5F) {
                this.webbingCooldown = 200;
                BlockState state = this.world.getBlockState(this.getBlockPos());
                Block block = state.getBlock();
                Material material = state.getMaterial();
                if (material == Material.REPLACEABLE_PLANT || block == Blocks.AIR)
                    if (target != null) this.world.setBlockState(target.getBlockPos(), Blocks.COBWEB.getDefaultState());
            }
        } else --this.webbingCooldown;
    }
}
