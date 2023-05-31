package com.hcs.misc.accessor;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;

public interface DamageSourcesAccessor {
    DamageSource dehydrate();
    DamageSource heatstroke();
}
