package biz.coolpage.hcs.status.accessor;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.damagesource.DamageSource;

public interface IDamageSources {
    DamageSource dehydrate();

    DamageSource heatstroke();

    DamageSource oxygenDeficiency();

    DamageSource darkness();

    DamageSource bleeding();

    DamageSource parasiteInfection();
}
