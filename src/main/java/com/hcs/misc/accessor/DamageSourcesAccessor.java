package com.hcs.misc.accessor;

import net.minecraft.entity.damage.DamageSource;

public interface DamageSourcesAccessor {
    DamageSource dehydrate();

    DamageSource heatstroke();
}
