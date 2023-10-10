package com.hcs.status.accessor;

import net.minecraft.entity.damage.DamageSource;

public interface DamageSourcesAccessor {
    DamageSource dehydrate();

    DamageSource heatstroke();

    DamageSource oxygenDeficiency();

    DamageSource darkness();

    DamageSource bleeding();
}
