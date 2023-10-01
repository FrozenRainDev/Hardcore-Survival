package com.hcs.status;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.hcs.status.accessor.DamageSourcesAccessor;
import com.hcs.status.accessor.StatAccessor;
import com.hcs.util.EntityHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

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
        //UUID.randomUUID() was used to generate different uuids
    }.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "AB33D21F-6A8B-42DF-AD5C-830503EB71DB", 0.0, EntityAttributeModifier.Operation.ADDITION)
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "DFAE009E-7F40-4EC2-BF1F-D6F0B5CA77B5", -0.15f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "726C2159-5D61-4656-8B1E-A594BC7C3E84", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, "F03F53E5-DC9E-4716-8248-7B13FCAFE753", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

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
    }.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "8DD81631-9D25-477D-BD49-AC3608D64A63", 0.0, EntityAttributeModifier.Operation.ADDITION)
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "FB211E69-EB0E-411C-A0CF-C766028B2931", -0.15f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL).addAttributeModifier(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, "29EDAC81-3142-47BD-883B-360920634AEF", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "8D397C83-05C8-4236-88F3-05392A81A63B", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final StatusEffect EXHAUSTED = new StatusEffect(StatusEffectCategory.HARMFUL, 0xe3e3e3) {
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayerEntity && amplifier > 0) entity.setSprinting(false);
        }
    }.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "DC32D347-22EC-4B12-9E50-035302B760F0", -0.45F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "3FE53989-7FA5-4D88-8060-D774E67796FE", -0.35F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "92C8EC57-582C-43C2-A8C7-F164774349D6", -0.4F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, "6C27DCA8-9388-45EE-B6A8-33197B686DE4", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

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
    }.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "20F0693B-DF7C-4E9F-A970-82F12AE54B01", -0.1f, EntityAttributeModifier.Operation.ADDITION)
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "CF7F7560-AE19-4C37-BF17-DB898A9E62ED", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "52520C27-F947-43A5-9E30-9FEDB3BB44DA", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, "8E2A9034-7593-46BD-96B1-139203DEC1A6", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

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
    }.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "6AEDC8BD-5071-4941-8396-E37CBDD6FF23", -0.1f, EntityAttributeModifier.Operation.ADDITION)
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "F98685EE-BA72-47C4-B0F3-B23835FD443D", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "A8ED4453-B9F0-4BD5-A9E6-52F122FB07CD", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, "3BBDACD0-A218-4872-BC9A-C17C90E6B57D", -0.1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final StatusEffect STRONG_SUN = new StatusEffect(StatusEffectCategory.HARMFUL, 0xff6c00) {
    };

    public static final StatusEffect CHILLY_WIND = new StatusEffect(StatusEffectCategory.HARMFUL, 0xf0f0f0) {
    };

    public static final StatusEffect OVEREATEN = new StatusEffect(StatusEffectCategory.HARMFUL, 0x90514f) {
    }.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "28AFE91C-13C7-4E2F-BC29-7F747282B53C", -0.15f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

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
            if (entity instanceof ServerPlayerEntity player && (!(player.isWet() || ((StatAccessor) player).getStatusManager().getRecentWetTicks() > 0) || player.isBeingRainedOn()))
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
    }.addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, "90BF8511-9818-41DC-BE0C-C7262EE79960", -0.2F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final StatusEffect INJURY = new StatusEffect(StatusEffectCategory.HARMFUL, 0x8c1000) {
        final Multimap<EntityAttribute, EntityAttributeModifier> customAttributeModifiers = Multimaps.synchronizedMultimap(ArrayListMultimap.create());
        int lastAmplifier = 0;

        @Override
        protected String loadTranslationKey() {
            return switch (this.lastAmplifier) {
                case 0, 1, 2, 3 -> "effect.hcs.injury." + (lastAmplifier + 1);
                default -> "effect.hcs.injury";
            };
        }


        @Override
        public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
            this.lastAmplifier = amplifier;
            if (entity == null) return;
            switch (amplifier) {
                default -> { //Minor Injuries: speed -10%, attack speed -10%, knockback -20%
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getTranslationKey, -0.1F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getTranslationKey, -0.1F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getTranslationKey, -0.2F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 1 -> { //Moderate Injuries: speed -20%, attack damage -20%, attack speed -30%, knockback -40%
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getTranslationKey, -0.2F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getTranslationKey, -0.3, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getTranslationKey, -0.4F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(UUID.fromString("B53DBD42-F249-4BFD-8A41-A9A8A2FB1C1C"), this::getTranslationKey, -0.2, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 2 -> { //Severe Injuries: speed -40%, attack damage -50%, attack speed -50%, knockback -60%
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getTranslationKey, -0.4F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getTranslationKey, -0.6F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getTranslationKey, -0.6F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(UUID.fromString("B53DBD42-F249-4BFD-8A41-A9A8A2FB1C1C"), this::getTranslationKey, -0.5F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 3 -> { //Critical Injuries: speed -80%, attack damage -80%, attack speed -80%, knockback -80%
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getTranslationKey, -0.8F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getTranslationKey, -0.8, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getTranslationKey, -0.8F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(UUID.fromString("B53DBD42-F249-4BFD-8A41-A9A8A2FB1C1C"), this::getTranslationKey, -0.8F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 50, 0, false, false, false));
                }
            }
            attributes.addTemporaryModifiers(this.customAttributeModifiers);
            super.onApplied(entity, attributes, amplifier);
        }

        @Override
        public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
            this.lastAmplifier = amplifier;
            if (entity == null) return;
            removeTempAttributes(attributes, this.customAttributeModifiers);
            super.onRemoved(entity, attributes, amplifier);
        }


    };

    public static final StatusEffect PAIN = new StatusEffect(StatusEffectCategory.HARMFUL, 0x421d0a) {
        @Deprecated
        final Multimap<EntityAttribute, EntityAttributeModifier> customAttributeModifiers = Multimaps.synchronizedMultimap(ArrayListMultimap.create());
        int lastAmplifier = 0;

        @Override
        protected String loadTranslationKey() {
            return switch (this.lastAmplifier) {
                case 0, 1, 2, 3 -> "effect.hcs.pain." + (lastAmplifier + 1);
                default -> "effect.hcs.pain";
            };
        }

        @Override
        public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
            this.lastAmplifier = amplifier;
        }

    };

    public static final Predicate<StatusEffect> IS_NAME_VARIABLE = effect -> effect == PAIN || effect == INJURY; // A predicate determines whether an effect should be appended by Roman numerals to express level

    public static void removeTempAttributes(AttributeContainer attributes, @NotNull Multimap<EntityAttribute, EntityAttributeModifier> customAttributeModifiers) {
        for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : customAttributeModifiers.entries()) {
            EntityAttributeInstance entityAttributeInstance = attributes.getCustomInstance(entry.getKey());
            if (entityAttributeInstance == null) continue;
            EntityAttributeModifier entityAttributeModifier = entry.getValue();
            entityAttributeInstance.removeModifier(entityAttributeModifier);
        }
    }

}
