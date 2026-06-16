package biz.coolpage.hcs.item;

import biz.coolpage.hcs.entity.ThrownSpearEntity;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

// Import your custom sound registry here, for example:
// import biz.coolpage.hcs.registry.HcsSounds;

public class SpearItem extends TridentItem {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    // Added thrown damage parameter based on TFC's implementation
    private final float thrownDamage;

    public SpearItem(@NotNull Tier tier, float attackDamageModifier, float thrownDamage, float attackSpeedModifier, @NotNull Properties properties) {
        // Crucial fix: Manually apply Tier's durability to item properties
        super(properties.defaultDurability(tier.getUses()));

        this.thrownDamage = thrownDamage;

        // Dynamically calculate damage for this material
        float attackDamage = attackDamageModifier + tier.getAttackDamageBonus();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", attackSpeedModifier, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot slot) {
        // Apply custom melee damage and attack speed
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    // Prevents players from breaking blocks with the spear in survival mode
    @Override
    public boolean canAttackBlock(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
        return !player.isCreative();
    }

    // Explicitly disables sword sweep logic for the spear
    @Override
    public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ToolAction toolAction) {
        return super.canPerformAction(stack, toolAction) && toolAction != ToolActions.SWORD_SWEEP;
    }

    // 在 SpearItem.java 内部添加：
    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                // 单例延迟加载，节省性能
                if (this.renderer == null) {
                    this.renderer = new biz.coolpage.hcs.client.renderer.SpearItemRenderer();
                }
                return this.renderer;
            }
        });
    }

    public float getThrownDamage() {
        return this.thrownDamage;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player) {
            int drawDuration = this.getUseDuration(stack) - timeLeft;
            if (drawDuration >= 10) {
                if (!level.isClientSide) {
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(entityLiving.getUsedItemHand()));

                    // Spawn custom thrown spear entity
                    ThrownSpearEntity spearEntity = new ThrownSpearEntity(level, player, stack);

                    // Modify the 5th parameter (Velocity) from previous 2.5F to reduce throwing distance
                    spearEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.8F, 1.0F);

                    if (player.getAbilities().instabuild) {
                        spearEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    }

                    level.addFreshEntity(spearEntity);

                    // Replace SoundEvents.TRIDENT_THROW with your custom sound event
                    // Example: HcsSounds.SPEAR_THROW.get()
                    level.playSound(null, spearEntity, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

                    if (!player.getAbilities().instabuild) {
                        player.getInventory().removeItem(stack);
                    }
                }
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }
}