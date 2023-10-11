package com.hcs.status;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.hcs.status.accessor.DamageSourcesAccessor;
import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.manager.InjuryManager;
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
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.hcs.util.EntityHelper.IS_SURVIVAL_LIKE;

public class HcsEffects {

    public static final StatusEffect RETURN = new StatusEffect(StatusEffectCategory.NEUTRAL, 0x22d3f6) {
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
            if (entity instanceof ServerPlayerEntity player && !entity.isSpectator())
                EntityHelper.teleportPlayerToSpawn(player.getWorld(), player, true);
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
                player.getHungerManager().addExhaustion(0.01F * (amplifier + 1));
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
    }.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "DFAE009E-7F40-4EC2-BF1F-D6F0B5CA77B5", -0.15f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
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
            if (entity instanceof ServerPlayerEntity player && !entity.isInvulnerable()) {
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
    }.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "28AFE91C-13C7-4E2F-BC29-7F747282B53C", -0.07F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

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
    }.addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, "90BF8511-9818-41DC-BE0C-C7262EE79960", -0.1F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final StatusEffect INJURY = new StatusEffect(StatusEffectCategory.HARMFUL, 0x8c1000) {
        final Multimap<EntityAttribute, EntityAttributeModifier> customAttributeModifiers = Multimaps.synchronizedMultimap(ArrayListMultimap.create());
        int lastAmplifier = 0;

        @Override
        protected String loadTranslationKey() {
            return GET_AMP4_KEY.apply("injury", this.lastAmplifier);
        }

        @Override
        public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
            this.lastAmplifier = amplifier;
            if (entity == null) return;
            switch (amplifier) {
                default -> { //Minor Injuries: speed -5%, attack speed -5%, knockback -10%
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getTranslationKey, -0.05F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getTranslationKey, -0.05F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getTranslationKey, -0.1F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 1 -> { //Moderate Injuries: speed -10%, attack damage -10%, attack speed -15%, knockback -20%
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getTranslationKey, -0.1F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getTranslationKey, -0.15F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getTranslationKey, -0.2F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(UUID.fromString("B53DBD42-F249-4BFD-8A41-A9A8A2FB1C1C"), this::getTranslationKey, -0.1F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 2 -> { //Severe Injuries: speed -30%, attack damage -25%, attack speed -25%, knockback -30%
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getTranslationKey, -0.3F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getTranslationKey, -0.25F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getTranslationKey, -0.3F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(UUID.fromString("B53DBD42-F249-4BFD-8A41-A9A8A2FB1C1C"), this::getTranslationKey, -0.25F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 3 -> { //Critical Injuries: speed -40%, attack damage -40%, attack speed -40%, knockback -40%, blindness, cannot jump
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getTranslationKey, -0.4F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getTranslationKey, -0.4F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getTranslationKey, -0.4F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(UUID.fromString("B53DBD42-F249-4BFD-8A41-A9A8A2FB1C1C"), this::getTranslationKey, -0.4F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 50, 0, false, false, false));
                }
            }
            attributes.addTemporaryModifiers(this.customAttributeModifiers);
            super.onApplied(entity, attributes, amplifier);
        }

        @Override
        public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
            if (entity == null) return;
            removeTempAttributes(attributes, this.customAttributeModifiers);
            super.onRemoved(entity, attributes, amplifier);
        }
    };

    public static final StatusEffect PAIN = new StatusEffect(StatusEffectCategory.HARMFUL, 0x421d0a) {
        final Multimap<EntityAttribute, EntityAttributeModifier> customAttributeModifiers = Multimaps.synchronizedMultimap(ArrayListMultimap.create());
        int lastAmplifier = 0;

        @Override
        protected String loadTranslationKey() {
            return GET_AMP4_KEY.apply("pain", this.lastAmplifier);
        }

        @Override
        public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
            this.lastAmplifier = amplifier;
            if (entity == null) return;
            switch (amplifier) {
                default -> { //Minor Pain: speed -5%, attack speed -5%, knockback -10%
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("873BA6F8-398D-432C-B8EE-2601D0363F8E"), this::getTranslationKey, -0.05F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(UUID.fromString("FD1185A0-A575-4096-8E17-97A03E2EB922"), this::getTranslationKey, -0.05F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(UUID.fromString("792D1884-1418-4A06-A03E-756A7B609CD0"), this::getTranslationKey, -0.1F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 1 -> { //Moderate Pain: speed -10%, attack damage -10%, attack speed -15%, knockback -20%
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("881B4777-7AC6-43F0-9785-FA6C467C0133"), this::getTranslationKey, -0.1F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(UUID.fromString("B1B204EE-92C8-4934-A008-F82077D6CD1B"), this::getTranslationKey, -0.15F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(UUID.fromString("A72773FC-98E9-4206-B26D-FC6FAEAEB2E4"), this::getTranslationKey, -0.2F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(UUID.fromString("283A6E73-E2E2-4D42-A262-6CCF459704A2"), this::getTranslationKey, -0.1F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 2 -> { //Severe Pain: speed -25%, attack damage -25%, attack speed -25%, knockback -30%
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("292CBB4C-9599-4538-97CF-DC6F924E1BB2"), this::getTranslationKey, -0.25F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(UUID.fromString("32F29F5C-CD1E-47E2-B42C-D34F3B9FDDC3"), this::getTranslationKey, -0.25F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(UUID.fromString("DF28B6B7-7175-4D42-856D-D52EF647A056"), this::getTranslationKey, -0.3F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(UUID.fromString("BC402FEC-1445-4C12-8D10-62D7182B8781"), this::getTranslationKey, -0.25F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 3 -> { //Critical Pain: speed -40%, attack damage -40%, attack speed -40%, knockback -40%
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(UUID.fromString("8FB78320-AB19-4528-9D46-E7C1DBA7430D"), this::getTranslationKey, -0.4F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(UUID.fromString("5070DD4F-BBA9-4400-B2DF-E0260BA77A98"), this::getTranslationKey, -0.4F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(UUID.fromString("A5448EE0-84AB-4BE4-A745-49B8EE8CC6FE"), this::getTranslationKey, -0.4F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(UUID.fromString("4DD760AE-07D8-412A-852E-6990AD106D94"), this::getTranslationKey, -0.4F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }
            attributes.addTemporaryModifiers(this.customAttributeModifiers);
            super.onApplied(entity, attributes, amplifier);
        }

        @Override
        public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
            if (entity == null) return;
            removeTempAttributes(attributes, this.customAttributeModifiers);
            super.onRemoved(entity, attributes, amplifier);
        }

    };

    public static final StatusEffect PANIC = new StatusEffect(StatusEffectCategory.HARMFUL, 0xffffff) {
        int lastAmplifier = 0;

        @Override
        protected String loadTranslationKey() {
            return GET_AMP4_KEY.apply("panic", this.lastAmplifier);
        }

        @Override
        public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
            this.lastAmplifier = amplifier;
        }

    };

    public static final StatusEffect BLEEDING = new StatusEffect(StatusEffectCategory.HARMFUL, 0xcf0303) {
        int lastAmplifier = 0;

        @Override
        protected String loadTranslationKey() {
            return GET_AMP4_KEY.apply("bleeding", this.lastAmplifier);
        }

        @Override
        public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
            this.lastAmplifier = amplifier;
        }

        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity != null && entity.world != null && !entity.isInvulnerable() && amplifier > 0)
                if (entity.world.getTime() % (900 / (amplifier == 1 ? 1.5 : (amplifier * 6.0))) == 0)
                    entity.damage(((DamageSourcesAccessor) entity.world.getDamageSources()).bleeding(), 1.0F);
        }
    };

    public static final StatusEffect DARKNESS_ENVELOPED = new StatusEffect(StatusEffectCategory.HARMFUL, 0x000000) {
    }.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "75AD9D60-968B-4788-8B9F-3A545D3534E7", -0.4F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final StatusEffect FRACTURE = new StatusEffect(StatusEffectCategory.HARMFUL, 0xe8e5d2) {
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayerEntity player && IS_SURVIVAL_LIKE.test(player) && amplifier > 0) {
                entity.setSprinting(false);
                InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
                if (injuryManager.getRawPain() < 3) injuryManager.setRawPain(3);
            }
        }
    }.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "FFEFDCF8-49B1-4CC7-B6D7-4E07D7F936CA", -0.7F, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final StatusEffect PARASITE_INFECTION = new StatusEffect(StatusEffectCategory.HARMFUL, 0xe2bc8a) {
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayerEntity player && IS_SURVIVAL_LIKE.test(player)) {
                ((StatAccessor) player).getThirstManager().add(-0.0001 * (amplifier + 1));
                player.getHungerManager().addExhaustion(0.007F * (amplifier + 1));
                if (amplifier > 0) {
                    InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
                    if (injuryManager.getRawPain() < amplifier) injuryManager.setRawPain(amplifier);
                    if (amplifier > 1) {
                        ((StatAccessor) player).getSanityManager().add(-0.00005);
                        if (player.world.getTime() % 100 == 0) {
                            player.damage(((DamageSourcesAccessor) player.world.getDamageSources()).parasiteInfection(), 1.0F);
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 50, 0, false, false, false));
                        }
                    }
                }
            }
        }
    };

    public static final StatusEffect UNHAPPY = new StatusEffect(StatusEffectCategory.HARMFUL, 0x71c5db) {
    };

    public static final StatusEffect COLD = new StatusEffect(StatusEffectCategory.HARMFUL, 0xf0c1ba) {
    };

    public static final StatusEffect HEAVY_LOAD = new StatusEffect(StatusEffectCategory.HARMFUL, 0xfed93f) {
    };

    public static final BiFunction<String, Integer, String> GET_AMP4_KEY = (name, amplifier) -> switch (amplifier) {
        case 0, 1, 2, 3 -> "effect.hcs." + name + "." + (amplifier + 1);
        default -> "effect.hcs." + name;
    };

    @Deprecated
    public static final BiFunction<String, Integer, String> GET_AMP3_KEY = (name, amplifier) -> GET_AMP4_KEY.apply(name, amplifier == 3 ? 2 : amplifier);

    public static final Predicate<StatusEffect> IS_EFFECT_NAME_VARIABLE = effect -> effect == PAIN || effect == INJURY || effect == PANIC || effect == BLEEDING; // A predicate determines whether an effect should be appended by Roman numerals to express level

    public static void removeTempAttributes(AttributeContainer attributes, @NotNull Multimap<EntityAttribute, EntityAttributeModifier> customAttributeModifiers) {
        for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : customAttributeModifiers.entries()) {
            EntityAttributeInstance entityAttributeInstance = attributes.getCustomInstance(entry.getKey());
            if (entityAttributeInstance == null) continue;
            EntityAttributeModifier entityAttributeModifier = entry.getValue();
            entityAttributeInstance.removeModifier(entityAttributeModifier);
        }
        customAttributeModifiers.clear();
    }

}
