package biz.coolpage.hcs.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static biz.coolpage.hcs.util.EntityHelper.ZOMBIE_AND_SKELETON_SENSING_RANGE;

// Also see RangedBowAttackGoal
@Mixin(AbstractSkeleton.class)
public class AbstractSkeletonMixin {
    // Modify the default attributes of the skeleton (e.g., Max Health)
    // Using MixinExtras @ModifyReturnValue to replace the traditional @Inject RETURN
    @ModifyReturnValue(method = "createAttributes", at = @At("RETURN"))
    private static AttributeSupplier.@NotNull Builder hcs$modifyAttributes(AttributeSupplier.@NotNull Builder original) {
        // Set the skeleton's base max health to 4.0
        return original.add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.FOLLOW_RANGE, ZOMBIE_AND_SKELETON_SENSING_RANGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    // Also see RangedBowAttackGoalMixin
    // Modify the draw bow damage modifier when getting the arrow
    @WrapOperation(
            method = "performRangedAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/AbstractSkeleton;getArrow(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/projectile/AbstractArrow;"
            )
    )
    private AbstractArrow hcs$wrapArrowDamageModifier(AbstractSkeleton instance, ItemStack stack, float damageModifier, @NotNull Operation<AbstractArrow> original) {
        // Reduce the bow draw power multiplier to 80% of vanilla
        // We pass the modified damageModifier to the original getArrow method
        return original.call(instance, stack, damageModifier * 0.8F);
    }
}