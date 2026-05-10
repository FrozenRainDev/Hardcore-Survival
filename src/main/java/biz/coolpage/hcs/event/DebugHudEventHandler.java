package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.*;
import biz.coolpage.hcs.util.TemperatureHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static biz.coolpage.hcs.util.CommUtil.retain5;

// Register the event to the Forge Event Bus on the Physical Client side
@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DebugHudEventHandler {

    @SubscribeEvent
    public static void onRenderDebugText(CustomizeGuiOverlayEvent.@NotNull DebugText event) {
        Minecraft minecraft = Minecraft.getInstance();
        List<String> list = event.getLeft();

        // Respect the original condition checks
        if (!minecraft.options.renderDebugCharts || minecraft.player == null || list == null) return;

        Player player = minecraft.player;
        FoodData hungerManager = player.getFoodData();

        // Retrieve managers from player via StatAccessor
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

        // Blank line for spacing
        list.add("");

        // Header
        list.add(ChatFormatting.GOLD + "" + ChatFormatting.BOLD + "[ HCS Debug ]");

        // Sub-categories formatted with colors for better readability
        list.add(ChatFormatting.YELLOW + "Item: " + ChatFormatting.WHITE + "NBT=" + player.getMainHandItem().getOrCreateTag().toString());
        list.add(ChatFormatting.YELLOW + "World: " + ChatFormatting.WHITE + "tick=" + world.getGameTime() + ", lunar(of_day)=" + world.getDayTime() + ", difficulty=" + statusManager.getHcsDifficulty().name());

        list.add(ChatFormatting.AQUA + "Thirst: " + ChatFormatting.WHITE + "val=" + thirstManager.get() + ", satu=" + thirstManager.getSaturation() + ", rate=" + thirstManager.getThirstRateAffectedByTemp());
        list.add(ChatFormatting.AQUA + "Hunger: " + ChatFormatting.WHITE + "level=" + hungerManager.getFoodLevel() + ", satu=" + hungerManager.getSaturationLevel() + ", exh=" + statusManager.getExhaustion());
        list.add(ChatFormatting.AQUA + "Stamina: " + ChatFormatting.WHITE + staminaManager.get() + ChatFormatting.AQUA + "  Sanity: " + ChatFormatting.WHITE + sanityManager.get() + ", diff=" + sanityManager.getDifference());

        // Temperature logic kept exactly as original
        list.add(ChatFormatting.RED + "Temp: " + ChatFormatting.WHITE + "biome=" + biome.getBaseTemperature() +
                ", env=[real:" + retain5(TemperatureHelper.getTemp(player)) +
                ", feel:" + retain5(TemperatureHelper.getFeelingTemp(player, TemperatureHelper.getTemp(player), biomeName, player.level().getBrightness(LightLayer.SKY, player.blockPosition()))) + "]" +
                ", val=" + retain5(temperatureManager.get()) + ", satu=" + retain5(temperatureManager.getSaturation()) + ", trend=" + temperatureManager.getTrendType());

        list.add(ChatFormatting.GREEN + "Nutrition: " + ChatFormatting.WHITE + "veg=" + nutritionManager.getVegetable());
        list.add(ChatFormatting.GRAY + "Oxygen: " + ChatFormatting.WHITE + "lack=" + oxygenManager.getOxygenLackLevel() + ", gen=" + oxygenManager.getOxygenGenLevel());

        list.add(ChatFormatting.DARK_RED + "Injury: " + ChatFormatting.WHITE + "pain[real=" + retain5(injuryManager.getRealPain()) +
                ", raw=" + retain5(injuryManager.getRawPain()) + ", alle=" + retain5(injuryManager.getPainkillerAlleviation()) +
                "], bleeding=" + retain5(injuryManager.getBleeding()) + ", fracture=" + retain5(injuryManager.getFracture()));

        list.add(ChatFormatting.BLUE + "Wetness: " + ChatFormatting.WHITE + ((StatAccessor) player).getWetnessManager().get());

        list.add(ChatFormatting.LIGHT_PURPLE + "Mood: " + ChatFormatting.WHITE + "panic=[raw=" + retain5(moodManager.getRawPanic()) +
                ", real=" + retain5(moodManager.getRealPanic()) + ", alle=" + retain5(moodManager.getPanicAlleCache()) +
                "], happy=" + retain5(moodManager.getHappiness()));

        list.add(ChatFormatting.DARK_GREEN + "Disease: " + ChatFormatting.WHITE + "parasite=" + retain5(diseaseManager.getParasite()) + ", cold=" + retain5(diseaseManager.getCold()));
    }
}