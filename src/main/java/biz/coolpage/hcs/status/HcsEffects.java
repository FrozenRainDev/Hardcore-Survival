package biz.coolpage.hcs.status;

import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.status.accessor.IDamageSources;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.InjuryManager;
import biz.coolpage.hcs.status.manager.StatusManager;
import biz.coolpage.hcs.status.manager.ThirstManager;
import biz.coolpage.hcs.util.EntityHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class HcsEffects {

    public static final MobEffect RETURN = new MobEffect(MobEffectCategory.NEUTRAL, 0x22d3f6) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
            if (entity instanceof ServerPlayer player && !entity.isSpectator())
                EntityHelper.msgById(player, "hcs.tip.return_wait");
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !player.isSpectator()) {
                StatusManager statusManager = ((StatAccessor) player).getStatusManager();
                if (statusManager.getReturnEffectAwaitTicks() > 100) {
                    statusManager.setReturnEffectAwaitTicks(0);
                    player.getFoodData().eat(-6, 0.0F);
                    ((StatAccessor) player).getSanityManager().add(-0.3);
                    ThirstManager thirstManager = ((StatAccessor) player).getThirstManager();
                    thirstManager.setSaturation(0.0F);
                    thirstManager.add(-0.3);
                    player.hurt(player.damageSources().fall(), 5.0f);
                    EntityHelper.teleportPlayerToSpawn(player.level(), player, false);
                }
            }
        }
    };

    public static final MobEffect THIRST = new MobEffect(MobEffectCategory.HARMFUL, 0xb0dff4) {//Drink saltwater
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer && !entity.isSpectator())
                ((StatAccessor) entity).getThirstManager().add(-0.00045 * (amplifier + 1));
        }
    };

    public static final MobEffect DIARRHEA = new MobEffect(MobEffectCategory.HARMFUL, 0xdbc44c) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !entity.isSpectator()) {
                player.getFoodData().addExhaustion(0.01F * (amplifier + 1));
                ((StatAccessor) player).getThirstManager().add(-0.00015 * (amplifier + 1));
            }
        }
    };

    public static final MobEffect DEHYDRATED = new MobEffect(MobEffectCategory.HARMFUL, 0xe7e7e7) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !entity.isInvisible()) {
                entity.setSprinting(false);
                ((StatAccessor) player).getSanityManager().add(-0.00001 * (amplifier + 1));
            }
        }
    }.addAttributeModifier(Attributes.MOVEMENT_SPEED, "DFAE009E-7F40-4EC2-BF1F-D6F0B5CA77B5", -0.15f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, "DF97F5A2-0133-4F21-AED4-D3F51227624C", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, "726C2159-5D61-4656-8B1E-A594BC7C3E84", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "F03F53E5-DC9E-4716-8248-7B13FCAFE753", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final MobEffect STARVING = new MobEffect(MobEffectCategory.HARMFUL, 0x646464) {
        //Extremely hungry
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer && !entity.isSpectator()) entity.setSprinting(false);
        }
    }.addAttributeModifier(Attributes.MOVEMENT_SPEED, "14A47B9E-D4A3-4964-BDA2-CAFB774D60D6", -0.15f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, "008C8E27-DE78-4072-BF58-AC0B3CFBF2AF", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, "EEBB2A0F-C4E6-4E60-9BC3-B4D730C1F1F7", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "D9F3A91B-915C-4E69-A03E-558D0744C7AA", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final MobEffect EXHAUSTED = new MobEffect(MobEffectCategory.HARMFUL, 0xe3e3e3) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer && amplifier > 0) entity.setSprinting(false);
        }
    }.addAttributeModifier(Attributes.ATTACK_DAMAGE, "DC32D347-22EC-4B12-9E50-035302B760F0", -0.5F, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "3FE53989-7FA5-4D88-8060-D774E67796FE", -0.35F, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, "92C8EC57-582C-43C2-A8C7-F164774349D6", -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "6C27DCA8-9388-45EE-B6A8-33197B686DE4", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final MobEffect HYPOTHERMIA = new MobEffect(MobEffectCategory.HARMFUL, 0x0658ff) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !entity.isSpectator() && IS_SURVIVAL_AND_SERVER.test(player)) {
                ((StatAccessor) player).getDiseaseManager().addCold(0.0005);
                if (amplifier > 0) {
                    ((StatAccessor) player).getSanityManager().add(-0.00001 * (amplifier + 1));
                    player.setSprinting(false);
                    player.setTicksFrozen(entity.getTicksRequiredToFreeze() + 3);
                }
            }
        }
    }.addAttributeModifier(Attributes.ATTACK_DAMAGE, "20F0693B-DF7C-4E9F-A970-82F12AE54B01", -0.1f, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "CF7F7560-AE19-4C37-BF17-DB898A9E62ED", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, "52520C27-F947-43A5-9E30-9FEDB3BB44DA", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "8E2A9034-7593-46BD-96B1-139203DEC1A6", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final MobEffect HEATSTROKE = new MobEffect(MobEffectCategory.HARMFUL, 0xff6113) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !entity.isInvulnerable()) {
                //Accelerate water losing and decrease sanity
                ((StatAccessor) player).getThirstManager().add(-0.0001 * (amplifier + 1));
                ((StatAccessor) player).getSanityManager().add(-0.00003 * (amplifier + 1));
                if (amplifier > 0) {
                    player.setSprinting(false);
                    if (player.level() != null && player.level().getGameTime() % 60 == 0) {
                        DamageSource damageSource = ((IDamageSources) player.level().damageSources()).heatstroke();
                        if (damageSource != null) player.hurt(damageSource, 1.0F);
                    }
                }
            }
        }
    }.addAttributeModifier(Attributes.ATTACK_DAMAGE, "6AEDC8BD-5071-4941-8396-E37CBDD6FF23", -0.1f, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "F98685EE-BA72-47C4-B0F3-B23835FD443D", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, "A8ED4453-B9F0-4BD5-A9E6-52F122FB07CD", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "3BBDACD0-A218-4872-BC9A-C17C90E6B57D", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final MobEffect STRONG_SUN = new MobEffect(MobEffectCategory.HARMFUL, 0xff6c00) {
    };

    public static final MobEffect CHILLY_WIND = new MobEffect(MobEffectCategory.HARMFUL, 0xf0f0f0) {
    };

    public static final MobEffect OVEREATEN = new MobEffect(MobEffectCategory.HARMFUL, 0x90514f) {
    }.addAttributeModifier(Attributes.MOVEMENT_SPEED, "28AFE91C-13C7-4E2F-BC29-7F747282B53C", -0.07F, AttributeModifier.Operation.MULTIPLY_TOTAL);

    //Cause hallucinations
    public static final MobEffect INSANITY = new MobEffect(MobEffectCategory.HARMFUL, 0xff6113) {
    };

    public static final MobEffect MALNUTRITION = new MobEffect(MobEffectCategory.HARMFUL, 0xe8e5d2) {
    };

    public static final MobEffect WET = new MobEffect(MobEffectCategory.HARMFUL, 0x99a9d7) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !player.isInWater() && amplifier > 0 && ((StatAccessor) player).getTemperatureManager().getEnvTempCache() < 0.6)
                ((StatAccessor) player).getDiseaseManager().addCold(0.00002 * (amplifier + 1) * net.minecraft.util.Mth.clamp(2 * (1 - ((StatAccessor) player).getTemperatureManager().getEnvTempCache()), 0.01, 2.0));
        }
    };

    public static final MobEffect CONSTANT_TEMPERATURE = new MobEffect(MobEffectCategory.BENEFICIAL, 0x00aa00) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !player.isSpectator())
                ((StatAccessor) player).getTemperatureManager().reset();
        }
    };

    public static final MobEffect SOUL_IMPAIRED = new MobEffect(MobEffectCategory.HARMFUL, 0xd7e4eb) {
    }.addAttributeModifier(Attributes.MAX_HEALTH, "90BF8511-9818-41DC-BE0C-C7262EE79960", -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final MobEffect INJURY = new MobEffect(MobEffectCategory.HARMFUL, 0x8c1000) {
        final Multimap<Attribute, AttributeModifier> customAttributeModifiers = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

        @Override
        public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
            if (entity == null) return;
            switch (amplifier) {
                default -> { //Minor Injuries: speed -5%, attack speed -5%, knockback -10%
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getDescriptionId, -0.05F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getDescriptionId, -0.05F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getDescriptionId, -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 1 -> { //Moderate Injuries: speed -10%, attack damage -10%, attack speed -15%, knockback -20%
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getDescriptionId, -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getDescriptionId, -0.15F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getDescriptionId, -0.2F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("B53DBD42-F249-4BFD-8A41-A9A8A2FB1C1C"), this::getDescriptionId, -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 2 -> { //Severe Injuries: speed -30%, attack damage -25%, attack speed -25%, knockback -30%
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getDescriptionId, -0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getDescriptionId, -0.25F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getDescriptionId, -0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("B53DBD42-F249-4BFD-8A41-A9A8A2FB1C1C"), this::getDescriptionId, -0.25F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 3 -> { //Critical Injuries: speed -40%, attack damage -40%, attack speed -40%, knockback -40%, blindness, cannot jump
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("B53DBD42-F249-4BFD-8A41-A9A8A2FB1C1C"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false, false));
                }
            }
            attributes.addTransientAttributeModifiers(this.customAttributeModifiers);
            super.addAttributeModifiers(entity, attributes, amplifier);
        }

        @Override
        public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
            if (entity == null) return;
            removeTempAttributes(attributes, this.customAttributeModifiers);
            super.removeAttributeModifiers(entity, attributes, amplifier);
        }
    };

    public static final MobEffect PAIN = new MobEffect(MobEffectCategory.HARMFUL, 0x421d0a) {
        final Multimap<Attribute, AttributeModifier> customAttributeModifiers = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

        @Override
        public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
            if (entity == null) return;
            switch (amplifier) {
                default -> { //Minor Pain: speed -5%, attack speed -5%, knockback -10%
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("873BA6F8-398D-432C-B8EE-2601D0363F8E"), this::getDescriptionId, -0.05F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("FD1185A0-A575-4096-8E17-97A03E2EB922"), this::getDescriptionId, -0.05F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("792D1884-1418-4A06-A03E-756A7B609CD0"), this::getDescriptionId, -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 1 -> { //Moderate Pain: speed -10%, attack damage -10%, attack speed -15%, knockback -20%
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("881B4777-7AC6-43F0-9785-FA6C467C0133"), this::getDescriptionId, -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("B1B204EE-92C8-4934-A008-F82077D6CD1B"), this::getDescriptionId, -0.15F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("A72773FC-98E9-4206-B26D-FC6FAEAEB2E4"), this::getDescriptionId, -0.2F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("283A6E73-E2E2-4D42-A262-6CCF459704A2"), this::getDescriptionId, -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 2 -> { //Severe Pain: speed -25%, attack damage -25%, attack speed -25%, knockback -30%
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("292CBB4C-9599-4538-97CF-DC6F924E1BB2"), this::getDescriptionId, -0.25F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("32F29F5C-CD1E-47E2-B42C-D34F3B9FDDC3"), this::getDescriptionId, -0.25F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("DF28B6B7-7175-4D42-856D-D52EF647A056"), this::getDescriptionId, -0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("BC402FEC-1445-4C12-8D10-62D7182B8781"), this::getDescriptionId, -0.25F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 3 -> { //Critical Pain: speed -40%, attack damage -40%, attack speed -40%, knockback -40%
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("8FB78320-AB19-4528-9D46-E7C1DBA7430D"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("5070DD4F-BBA9-4400-B2DF-E0260BA77A98"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("A5448EE0-84AB-4BE4-A745-49B8EE8CC6FE"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("4DD760AE-07D8-412A-852E-6990AD106D94"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }
            attributes.addTransientAttributeModifiers(this.customAttributeModifiers);
            super.addAttributeModifiers(entity, attributes, amplifier);
        }

        @Override
        public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
            if (entity == null) return;
            removeTempAttributes(attributes, this.customAttributeModifiers);
            super.removeAttributeModifiers(entity, attributes, amplifier);
        }

    };

    public static final MobEffect PANIC = new MobEffect(MobEffectCategory.HARMFUL, 0xffffff) {
    };

    public static final MobEffect BLEEDING = new MobEffect(MobEffectCategory.HARMFUL, 0xcf0303) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity != null && entity.level() != null && !entity.isInvulnerable() && amplifier > 0)
                if (entity.level().getGameTime() % (switch (amplifier) {
                    case 1 -> 300L;
                    case 2 -> 75L;
                    default -> 20L;
                } * HcsDifficulty.chooseVal(toPlayer(entity), 2.0F, 1.0F, 0.5F)) == 0)
                    entity.hurt(((IDamageSources) entity.level().damageSources()).bleeding(), 0.5F);
        }
    };

    public static final MobEffect DARKNESS_ENVELOPED = new MobEffect(MobEffectCategory.HARMFUL, 0x000000) {
    }.addAttributeModifier(Attributes.MOVEMENT_SPEED, "75AD9D60-968B-4788-8B9F-3A545D3534E7", -0.6F, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final MobEffect FRACTURE = new MobEffect(MobEffectCategory.HARMFUL, 0xe8e5d2) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && IS_SURVIVAL_LIKE.test(player) && amplifier > 0) {
                entity.setSprinting(false);
                InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
                if (injuryManager.getRawPain() < 3) injuryManager.setRawPain(3);
            }
        }
    }.addAttributeModifier(Attributes.MOVEMENT_SPEED, "FFEFDCF8-49B1-4CC7-B6D7-4E07D7F936CA", -0.7F, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final MobEffect PARASITE_INFECTION = new MobEffect(MobEffectCategory.HARMFUL, 0xe2bc8a) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && IS_SURVIVAL_LIKE.test(player)) {
                ((StatAccessor) player).getThirstManager().add(-0.0001 * (amplifier + 1));
                player.getFoodData().addExhaustion(0.007F * (amplifier + 1));
                if (amplifier > 0) {
                    InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
                    if (injuryManager.getRawPain() < amplifier) injuryManager.setRawPain(amplifier);
                    if (amplifier > 1) {
                        ((StatAccessor) player).getSanityManager().add(-0.00005);
                        if (player.level().getGameTime() % 100 == 0) {
                            player.hurt(((IDamageSources) player.level().damageSources()).parasiteInfection(), 1.0F);
                            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false, false));
                        }
                    }
                }
            }
        }
    };

    public static final MobEffect UNHAPPY = new MobEffect(MobEffectCategory.HARMFUL, 0x71c5db) {
    };

    public static final MobEffect COLD = new MobEffect(MobEffectCategory.HARMFUL, 0xf0c1ba) {
    }       .addAttributeModifier(Attributes.ATTACK_DAMAGE, "008C8E27-DE78-4072-BF58-AC0B3CFBF2AF", -0.07F, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, "EEBB2A0F-C4E6-4E60-9BC3-B4D730C1F1F7", -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "D9F3A91B-915C-4E69-A03E-558D0744C7AA", -0.2F, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final MobEffect HEAVY_LOAD = new MobEffect(MobEffectCategory.HARMFUL, 0xfed93f) {
    };

    public static final MobEffect PAIN_KILLING = new MobEffect(MobEffectCategory.BENEFICIAL, 0x858585) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player) ((StatAccessor) player).getInjuryManager().setRawPain(0.0);
        }
    };

    public static final MobEffect IRONSKIN = new MobEffect(MobEffectCategory.BENEFICIAL, 0xe6de0a) {
    };

    public static final MobEffect FOOD_POISONING = new MobEffect(MobEffectCategory.HARMFUL, 0xb3c17b) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && IS_SURVIVAL_LIKE.test(player)) {
                ((StatAccessor) player).getThirstManager().add(-0.0003 * (amplifier + 1));
                player.getFoodData().addExhaustion(0.025F * (amplifier + 1));
                ((StatAccessor) player).getSanityManager().add(-0.00002 * (amplifier + 1));
                player.getFoodData().setSaturation(0.0F);
            }
        }
    };

    public static final MobEffect FEARLESSNESS = new MobEffect(MobEffectCategory.BENEFICIAL, 0x7e7e7e) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player) ((StatAccessor) player).getMoodManager().setPanic(0.0);
        }
    };

    private static final HashSet<MobEffect> VARIABLE_EFFECTS = Util.make(new HashSet<>(), set -> {
        set.add(PAIN);
        set.add(INJURY);
        set.add(PANIC);
        set.add(BLEEDING);
        set.add(WET);
    });

    public static final Predicate<MobEffect> IS_EFFECT_NAME_VARIABLE = VARIABLE_EFFECTS::contains; // A predicate determines whether an effect should be appended by Roman numerals to express level

    public static String getEffectVarName(String key, int amplifier) {
        return switch (amplifier) {
            case 0, 1, 2, 3 -> key + "." + (amplifier + 1);
            default -> key;
        };
    }

    private static void removeTempAttributes(AttributeMap attributes, @NotNull Multimap<Attribute, AttributeModifier> customAttributeModifiers) {
        for (Map.Entry<Attribute, AttributeModifier> entry : customAttributeModifiers.entries()) {
            AttributeInstance entityAttributeInstance = attributes.getInstance(entry.getKey());
            if (entityAttributeInstance == null) continue;
            AttributeModifier entityAttributeModifier = entry.getValue();
            entityAttributeInstance.removeModifier(entityAttributeModifier);
        }
        customAttributeModifiers.clear();
    }

    // 需要在你的代码中定义这些谓词
    private static Predicate<ServerPlayer> IS_SURVIVAL_AND_SERVER = player -> !player.isSpectator() && !player.isCreative();
    private static Predicate<ServerPlayer> IS_SURVIVAL_LIKE = player -> !player.isSpectator() && !player.isCreative();

    private static ServerPlayer toPlayer(LivingEntity entity) {
        return entity instanceof ServerPlayer ? (ServerPlayer) entity : null;
    }
}
