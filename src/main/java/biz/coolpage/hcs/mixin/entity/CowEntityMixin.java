package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.status.accessor.IKickCoolDown;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import static biz.coolpage.hcs.config.Configs.HOSTILE_COW;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(Cow.class)
public abstract class CowEntityMixin extends Animal implements IKickCoolDown {
    /* See data tracker init in `PassiveEntityMixin`
     * Needs a data tracker to sync `Server` and `Client`
     * Data trackers have NO auto saving -- It still rely on NBTs
     * > Reference: SheepEntity
     * */

    @Unique
    private int kickCoolDown = 20;

    @Unique
    @Override
    public boolean canKick() {
        if (Configs.isEnabled(HOSTILE_COW)) return this.kickCoolDown <= 0;
        return false;
    }

    @Unique
    @Override
    public void notifyKick() {
        this.kickCoolDown = 100;
    }

    @Unique
    @Override
    public void updateCooldown() {
        if (this.kickCoolDown > 0) this.kickCoolDown--;
    }

    protected CowEntityMixin(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

}