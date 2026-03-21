package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.status.accessor.IKickCoolDown;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static biz.coolpage.hcs.config.Configs.HOSTILE_COW;
import static biz.coolpage.hcs.util.EntityHelper.isInLeather;

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

    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private static void createCowAttributes(@NotNull CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue().add(Attributes.MAX_HEALTH, 20.0));
    }

//    @Inject(method = "initGoals", at = @At("TAIL"))
//    protected void initGoals(CallbackInfo ci) {
//        this.goalSelector.add(1 /* priority 0 causes entity immobility when ending escaping danger goal*/, new CowKickRevengeGoal(this));
//    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    public void interactMob(@NotNull Player player, InteractionHand hand, @NotNull CallbackInfoReturnable<InteractionResult> cir) {
        //  NOT SOLELY `ServerPlayerEntity` -- Both server and client side need the interaction
        boolean isMilking = player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BUCKET) && !this.isBaby();
        if (isMilking && EntityHelper.IS_SURVIVAL_AND_SERVER.test(player)) {
            if (isInLeather(player) || !Configs.isEnabled(player, HOSTILE_COW)) {
                long time = this.level().getGameTime();
                long milkedTime = this.entityData.get(EntityHelper.MILKED_TIME);
                if (Math.abs(milkedTime - time) < 24000L) {
                    // Prevent multiple milking in one day
                    player.playSound(SoundEvents.COW_AMBIENT, 1.0F, 1.0F);
                    cir.setReturnValue(InteractionResult.FAIL);
                } else this.entityData.set(EntityHelper.MILKED_TIME, time);
            } else { // Rebel and become panic when someone milking without any leather clothing
                this.setLastHurtByMob(player); // It will automatically set this.lastHurtByMobTimestamp
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }

}