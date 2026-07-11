package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.block.GroundPickableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
@SuppressWarnings("ConstantValue")
public abstract class EntityMixin {

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract BlockPos blockPosition();

    // Shadow the bounding box method to check for collision intersections
    @Shadow
    public abstract AABB getBoundingBox();

    @Inject(method = "isInvulnerableTo", at = @At("RETURN"), cancellable = true)
    public void isInvulnerableTo(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        Object ent = this;
        if (ent instanceof WitherSkeleton) // Wither skeletons are immune to wither boss
            cir.setReturnValue(cir.getReturnValueZ() || damageSource.getEntity() instanceof WitherBoss);
        else if (ent instanceof EnderMan) // Endermen are immune to ender dragon boss
            cir.setReturnValue(cir.getReturnValueZ() || damageSource.getEntity() instanceof EnderDragon);
        else if (ent instanceof EnderDragon || ent instanceof WitherBoss) // Bosses are immune to explosion damage and the damage caused by any boss
            cir.setReturnValue(cir.getReturnValueZ() || damageSource.getEntity() instanceof EnderDragon || damageSource.getEntity() instanceof WitherBoss || damageSource.is(DamageTypeTags.IS_EXPLOSION));
    }

    // Intercept the item spawning process to prevent specific drops
    @Inject(
            method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hcsurvival$cancelZombieMetalDrops(ItemStack stack, float offsetY, CallbackInfoReturnable<ItemEntity> cir) {
        // Check if the current entity instance inherits from Zombie
        if ((Object) this instanceof Zombie) {
            // Check if the item being dropped is classified as an ingot in Forge tags
            if (stack.is(Tags.Items.INGOTS)) {
                // Cancel the drop by returning null (preventing the ItemEntity from being spawned)
                cir.setReturnValue(null);
            }
        }
    }

    // Helper method to check if any part of the entity's bounding box is touching leaves
    @Unique
    private boolean hcsurvival$isTouchingLeaves() {
        // Deflate by 1.0E-6 to avoid floating point precision issues on block boundaries (Vanilla standard practice)
        return this.level().getBlockStatesIfLoaded(this.getBoundingBox().deflate(1.0E-6))
                .anyMatch(state -> state.is(BlockTags.LEAVES));
    }

    // Halve the velocity preservation (original is hardcoded to 1.0)
    @ModifyArg(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"
            ),
            index = 0
    )
    private double hcsurvival$modifyVerticalFrictionInLeavesX(double originalYFriction) {
        if (this.hcsurvival$isTouchingLeaves()) {
            return 0.5D;
        }
        return originalYFriction;
    }

    @ModifyArg(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"
            ),
            index = 1
    )
    private double hcsurvival$modifyVerticalFrictionInLeavesY(double originalYFriction) {
        if (this.hcsurvival$isTouchingLeaves()) {
            return 0.5D;
        }
        return originalYFriction;
    }

    @ModifyArg(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"
            ),
            index = 2
    )
    private double hcsurvival$modifyVerticalFrictionInLeavesZ(double originalYFriction) {
        if (this.hcsurvival$isTouchingLeaves()) {
            return 0.5D;
        }
        return originalYFriction;
    }


    // Handle the X and Z axis friction by halving the block speed factor
    @Inject(method = "getBlockSpeedFactor", at = @At("RETURN"), cancellable = true)
    private void hcs$applyVegetationSpeedFactor(CallbackInfoReturnable<Float> cir) {
        // Check the block exactly at the feet for other vegetation types
        BlockState state = this.level().getBlockState(this.blockPosition());
        Block block = state.getBlock();
        if (!(block instanceof GroundPickableBlock)) {
            if (block instanceof DoublePlantBlock) cir.setReturnValue(0.5F);
            else if (block instanceof BushBlock) cir.setReturnValue(0.8F);
        }
    }
}