package biz.coolpage.hcs.mixin.entity.damage;

import biz.coolpage.hcs.status.accessor.IDamageSources;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(DamageSources.class)
public abstract class DamageSourcesMixin implements IDamageSources {
    @Shadow
    @Final
    public Registry<DamageType> damageTypes;

    @Unique
    private DamageSource dehydrate, heatstroke, oxygenDeficiency, darkness, bleeding, parasiteInfection;

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public DamageSource dehydrate() {
        return this.dehydrate;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public DamageSource heatstroke() {
        return this.heatstroke;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public DamageSource oxygenDeficiency() {
        return this.oxygenDeficiency;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public DamageSource darkness() {
        return this.darkness;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public DamageSource bleeding() {
        return this.bleeding;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public DamageSource parasiteInfection() {
        return this.parasiteInfection;
    }

    @Inject(method = "source(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/world/damagesource/DamageSource;", at = @At("HEAD"))
    private void source(ResourceKey<DamageType> key, CallbackInfoReturnable<DamageSource> cir) {
        if (this.damageTypes == null) return;
        if (key == DamageTypes.STARVE) {
            /* Registering new damage types is frustratingly tough, which needs mixin of a static method called bootstrap in an interface "DamageTypes" as Registry.register cannot register that and there's no relevant API
            However, interfaces cannot be mixed in, so I have to abandon that way and onInteract anonymous inner class.
            View VanillaDamageTypeTagProvider.class to check the attributes of different damage types */
            this.dehydrate = new DamageSource(this.damageTypes.getHolderOrThrow(key)) {
                @Override
                public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity killed) {
                    return Component.translatable("death.attack.hcsurvival.dehydrate", killed.getDisplayName());
                }
            };
            this.heatstroke = new DamageSource(this.damageTypes.getHolderOrThrow(key)) {
                @Override
                public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity killed) {
                    return Component.translatable("death.attack.hcsurvival.heatstroke", killed.getDisplayName());
                }
            };
            this.oxygenDeficiency = new DamageSource(this.damageTypes.getHolderOrThrow(key)) {
                @Override
                public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity killed) {
                    return Component.translatable("death.attack.hcsurvival.oxygenDeficiency", killed.getDisplayName());
                }
            };
            this.bleeding = new DamageSource(this.damageTypes.getHolderOrThrow(key)) {
                @Override
                public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity killed) {
                    return Component.translatable("death.attack.hcsurvival.bleeding", killed.getDisplayName());
                }
            };
            this.parasiteInfection = new DamageSource(this.damageTypes.getHolderOrThrow(key)) {
                @Override
                public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity killed) {
                    return Component.translatable("death.attack.hcsurvival.parasiteInfection", killed.getDisplayName());
                }
            };
        } else if (key == DamageTypes.CACTUS) {
            this.darkness = new DamageSource(this.damageTypes.getHolderOrThrow(key)) {
                @Override
                public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity killed) {
                    return Component.translatable("death.attack.hcsurvival.darkness", killed.getDisplayName());
                }
            };
        }
    }

}
