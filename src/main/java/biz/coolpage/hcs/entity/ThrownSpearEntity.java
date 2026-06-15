package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Hcs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ThrownSpearEntity extends ThrownTrident {
    // 独立保存物品堆叠栈，覆盖原版三叉戟的私有属性
    private ItemStack spearItem = new ItemStack(Hcs.STONE_SPEAR.get());

    public ThrownSpearEntity(EntityType<? extends ThrownSpearEntity> type, Level level) {
        super(type, level);
    }

    public ThrownSpearEntity(Level level, LivingEntity shooter, @NotNull ItemStack stack) {
        // 必须调用 EntityType, Level 构造，防止渲染成原版三叉戟实体
        super(Hcs.THROWN_SPEAR.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
        this.spearItem = stack.copy();
    }

    @Override
    public @NotNull ItemStack getPickupItem() {
        return this.spearItem.copy();
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Spear", 10)) {
            this.spearItem = ItemStack.of(tag.getCompound("Spear"));
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Spear", this.spearItem.save(new CompoundTag()));
    }
}