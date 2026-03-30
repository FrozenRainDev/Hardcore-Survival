package biz.coolpage.hcs.mixin.client.gui;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.*;
import biz.coolpage.hcs.util.TemperatureHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static biz.coolpage.hcs.util.CommUtil.retain5;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugHudMixin {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method = "getGameInformation", at = @At("RETURN"))
    public void getLeftText(@NotNull CallbackInfoReturnable<List<String>> cir) {
        List<String> list = cir.getReturnValue();
        if (!this.minecraft.options.renderDebugCharts || this.minecraft.player == null || list == null) return;
        Player player = this.minecraft.player;
        FoodData hungerManager = player.getFoodData();
        ThirstManager thirstManager = ((StatAccessor) player).getThirstManager();
        StaminaManager staminaManager = ((StatAccessor) player).getStaminaManager();
        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
        NutritionManager nutritionManager = ((StatAccessor) player).getNutritionManager();
        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
        OxygenManager oxygenManager = ((StatAccessor) player).getOxygenManager();
        MoodManager moodManager = ((StatAccessor) player).getMoodManager();
        DiseaseManager diseaseManager = ((StatAccessor) player).getDiseaseManager();
        Level world = player.level();
        BlockPos pos = player.blockPosition();
        Holder<Biome> biomeEntry = world.getBiome(pos);
        Biome biome = biomeEntry.value();
        String biomeName = TemperatureHelper.getBiomeName(biomeEntry);
        list.add("[ HCS Debug ]");
        list.add("MainHandStackNbt: " + player.getMainHandItem().getOrCreateTag().toString());
        list.add("Time: tick=" + world.getGameTime() + ", lunar(of_day)=" + world.getDayTime() + ", difficulty=" + statusManager.getHcsDifficulty().name());
        list.add("Thirst: value=" + thirstManager.get() + ", saturation=" + thirstManager.getSaturation() + ", rate=" + thirstManager.getThirstRateAffectedByTemp());
        list.add("Hunger: level=" + hungerManager.getFoodLevel() + ", saturation=" + hungerManager.getSaturationLevel() + ", exhaustion=" + ((StatAccessor) player).getStatusManager().getExhaustion());
        list.add("Stamina: " + staminaManager.get());
        list.add("Sanity: " + sanityManager.get() + ", diff=" + sanityManager.getDifference());
        list.add("Temperature: biome=" + biome.getBaseTemperature() + ", env=[real: " + retain5(TemperatureHelper.getTemp(player)) + " ,feel:" + retain5(TemperatureHelper.getFeelingTemp(player, TemperatureHelper.getTemp(player), biomeName, player.level().getBrightness(LightLayer.SKY, player.blockPosition()))) + "]" + ", value=" + retain5(temperatureManager.get()) + ", satu=" + retain5(temperatureManager.getSaturation()) + ", trend=" + temperatureManager.getTrendType());
        list.add("Nutrition: vegetable=" + nutritionManager.getVegetable());
        list.add("Oxygen: lack=" + oxygenManager.getOxygenLackLevel() + ", gen=" + oxygenManager.getOxygenGenLevel());
        list.add("Injury: pain[real=" + retain5(injuryManager.getRealPain()) + ", raw=" + retain5(injuryManager.getRawPain()) + ", alle=" + retain5(injuryManager.getPainkillerAlleviation()) + "], bleeding=" + retain5(injuryManager.getBleeding()) + ", fracture=" + retain5(injuryManager.getFracture()));
        list.add("Wetness: " + ((StatAccessor) player).getWetnessManager().get());
        list.add("Mood: panic=[raw=" + retain5(moodManager.getRawPanic()) + ", real=" + retain5(moodManager.getRealPanic()) + ", alle=" + retain5(moodManager.getPanicAlleCache()) + "], happiness=" + retain5(moodManager.getHappiness()));
        list.add("Disease: parasite=" + retain5(diseaseManager.getParasite()) + ", cold=" + retain5(diseaseManager.getCold()));
    }
}