package com.hcs.mixin.entity.damage;

import com.hcs.status.accessor.DamageSourcesAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSources.class)
public abstract class DamageSourcesMixin implements DamageSourcesAccessor {
    private DamageSource dehydrate;
    private DamageSource heatstroke;
    private DamageSource oxygenDeficiency;
    @Shadow
    @Final
    public Registry<DamageType> registry;

    @Override
    public DamageSource dehydrate() {
        return this.dehydrate;
    }

    @Override
    public DamageSource heatstroke() {
        return this.heatstroke;
    }

    @Override
    public DamageSource oxygenDeficiency() {
        return this.oxygenDeficiency;
    }


    @Inject(method = "create(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/entity/damage/DamageSource;", at = @At("HEAD"))
    private void create(RegistryKey<DamageType> key, CallbackInfoReturnable<DamageSource> cir) {
        if (key == DamageTypes.STARVE && this.registry != null) {
            /* Registering new damage types is frustratingly tough, which needs mixin of a static method called bootstrap in an interface "DamageTypes" as Registry.register cannot register that and there's no relevant API
            However, interfaces cannot be mixed in, so I have to abandon that way and use anonymous inner class.
            View VanillaDamageTypeTagProvider.class to check the attributes of different damage types */
            this.dehydrate = new DamageSource(this.registry.entryOf(key)) {
                @Override
                public Text getDeathMessage(LivingEntity killed) {
                    return Text.translatable("death.attack.hcs.dehydrate", killed.getDisplayName());
                }
            };
            this.heatstroke = new DamageSource(this.registry.entryOf(key)) {
                @Override
                public Text getDeathMessage(LivingEntity killed) {
                    return Text.translatable("death.attack.hcs.heatstroke", killed.getDisplayName());
                }
            };
            this.oxygenDeficiency = new DamageSource(this.registry.entryOf(key)) {
                @Override
                public Text getDeathMessage(LivingEntity killed) {
                    return Text.translatable("death.attack.hcs.oxygenDeficiency", killed.getDisplayName());
                }
            };
        }
    }

}
