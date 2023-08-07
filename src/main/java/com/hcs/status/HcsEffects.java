package com.hcs.status;

import com.hcs.status.accessor.DamageSourcesAccessor;
import com.hcs.status.accessor.StatAccessor;
import com.hcs.util.EntityHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;

public class HcsEffects {
    public static final StatusEffect RETURN = new StatusEffect(StatusEffectCategory.NEUTRAL, 0x96f9ff) {
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
            if (entity instanceof ServerPlayerEntity player && !entity.isSpectator()) {
                EntityHelper.teleportPlayerToSpawn(player.getWorld(), player, true);
            }
        }
    };

    public static final StatusEffect THIRST = new StatusEffect(StatusEffectCategory.HARMFUL, 0xb0dff4) {//Drink saltwater
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayerEntity && !entity.isSpectator())
                ((StatAccessor) entity).getThirstManager().add(-0.00045 * (amplifier + 1));
        }
    };

    public static final StatusEffect DIARRHEA = new StatusEffect(StatusEffectCategory.HARMFUL, 0xdbc44c) {
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayerEntity player && !entity.isSpectator()) {
                ((ServerPlayerEntity) entity).getHungerManager().addExhaustion(0.01F * (amplifier + 1));
                ((StatAccessor) player).getThirstManager().add(-0.00015 * (amplifier + 1));
            }
        }
    };

    public static final StatusEffect DEHYDRATED = new StatusEffect(StatusEffectCategory.HARMFUL, 0xe7e7e7) {
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayerEntity player && !entity.isInvisible()) {
                entity.setSprinting(false);
                ((StatAccessor) player).getSanityManager().add(-0.00001 * (amplifier + 1));
            }
        }
    }.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", 0.0, EntityAttributeModifier.Operation.ADDITION)
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.15f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final StatusEffect STARVING = new StatusEffect(StatusEffectCategory.HARMFUL, 0x646464) {
        //Extremely hungry
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayerEntity && !entity.isSpectator()) entity.setSprinting(false);
        }
    }.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", 0.0, EntityAttributeModifier.Operation.ADDITION)
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.15f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final StatusEffect EXHAUSTED = new StatusEffect(StatusEffectCategory.HARMFUL, 0xe3e3e3) {
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayerEntity && amplifier > 0) entity.setSprinting(false);
        }
    }.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", -0.45F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.35F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -0.4F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final StatusEffect HYPOTHERMIA = new StatusEffect(StatusEffectCategory.HARMFUL, 0x0658ff) {
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayerEntity player && !entity.isSpectator() && amplifier > 0 && !player.getAbilities().invulnerable) {
                ((StatAccessor) player).getSanityManager().add(-0.00001 * (amplifier + 1));
                player.setSprinting(false);
                player.setFrozenTicks(entity.getMinFreezeDamageTicks() + 3);
            }
        }
    }.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", -0.1f, EntityAttributeModifier.Operation.ADDITION)
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final StatusEffect HEATSTROKE = new StatusEffect(StatusEffectCategory.HARMFUL, 0xff6113) {
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayerEntity player && !entity.isSpectator() && !entity.isInvulnerable()) {
                //Accelerate water losing and decrease sanity
                ((StatAccessor) player).getThirstManager().add(-0.0001 * (amplifier + 1));
                ((StatAccessor) player).getSanityManager().add(-0.00003 * (amplifier + 1));
                if (amplifier > 0) {
                    player.setSprinting(false);
                    if (player.world != null && player.world.getTime() % 60 == 0) {
                        DamageSource damageSource = ((DamageSourcesAccessor) player.world.getDamageSources()).heatstroke();
                        if (damageSource != null) player.damage(damageSource, 1.0F);
                    }
                }
            }
        }
    }.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", -0.1f, EntityAttributeModifier.Operation.ADDITION)
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final StatusEffect STRONG_SUN = new StatusEffect(StatusEffectCategory.HARMFUL, 0xff6c00) {
    };

    public static final StatusEffect CHILLY_WIND = new StatusEffect(StatusEffectCategory.HARMFUL, 0xf0f0f0) {
    };

    public static final StatusEffect OVEREATEN = new StatusEffect(StatusEffectCategory.HARMFUL, 0x90514f) {
    }.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.15f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    //Cause hallucinations
    public static final StatusEffect INSANITY = new StatusEffect(StatusEffectCategory.HARMFUL, 0xff6113) {
    };

    public static final StatusEffect MALNUTRITION = new StatusEffect(StatusEffectCategory.HARMFUL, 0xe8e5d2) {
    };

    public static final StatusEffect WET = new StatusEffect(StatusEffectCategory.HARMFUL, 0x99a9d7) {
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayerEntity player && (!player.isWet() || player.isBeingRainedOn()))
                ((StatAccessor) player).getSanityManager().add(-0.00001 * (amplifier + 1));
        }
    };

    public static final StatusEffect CONSTANT_TEMPERATURE = new StatusEffect(StatusEffectCategory.BENEFICIAL, 0x00aa00) {
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayerEntity player && !player.isSpectator())
                ((StatAccessor) player).getTemperatureManager().reset();
        }
    };

    public static final StatusEffect SOUL_IMPAIRED = new StatusEffect(StatusEffectCategory.HARMFUL, 0xd7e4eb) {
    }.addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC", -0.25F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);


}
