package com.hcs.main;

import com.hcs.block.DryingRackBlock;
import com.hcs.block.IceboxBlock;
import com.hcs.entity.DryingRackBlockEntity;
import com.hcs.entity.IceboxBlockEntity;
import com.hcs.entity.RockProjectileEntity;
import com.hcs.item.*;
import com.hcs.item.tools.*;
import com.hcs.main.event.*;
import com.hcs.main.manager.TemperatureManager;
import com.hcs.misc.HcsEffects;
import com.hcs.misc.accessor.StatAccessor;
import com.hcs.misc.network.ServerC2S;
import com.hcs.misc.recipes.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Do not implement ModInitializer to abstract classes as it will crash
//See customized damage sources in DamageSourcesMixin
public class Reg implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("hcs");
    public static final Potion IRONSKIN_POTION = new Potion("hcs_ironskin", new StatusEffectInstance(StatusEffects.RESISTANCE, 3600));
    public static final Potion LONG_IRONSKIN_POTION = new Potion("hcs_long_ironskin", new StatusEffectInstance(StatusEffects.RESISTANCE, 9600));
    public static final Potion STRONG_IRONSKIN_POTION = new Potion("hcs_strong_ironskin", new StatusEffectInstance(StatusEffects.RESISTANCE, 1800, 1));
    public static final Potion RETURN_POTION = new Potion("hcs_return", new StatusEffectInstance(HcsEffects.RETURN, 60, 0, false, false, false));
    public static final Potion MINING_POTION = new Potion("hcs_mining", new StatusEffectInstance(StatusEffects.HASTE, 3600));
    public static final Potion LONG_MINING_POTION = new Potion("hcs_long_mining", new StatusEffectInstance(StatusEffects.HASTE, 9600));
    public static final Potion STRONG_MINING_POTION = new Potion("hcs_strong_mining", new StatusEffectInstance(StatusEffects.HASTE, 1800, 1));
    public static final Item FIBER_STRING = new ItemWithTip(new Item.Settings(), "item.hcs.fiber_string.tooltip");
    public static final Item GRASS_FIBER = new Item(new Item.Settings());
    public static final Item ROASTED_SEEDS = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(0).snack().saturationModifier(0.5f).build()));
    public static final Item ROCK = new RockItem(new Item.Settings(), "item.hcs.rock.tooltip");
    public static final Item SHARP_ROCK = new Item(new Item.Settings());
    public static final Item SHARP_FLINT = new Item(new Item.Settings());
    public static final Item FIREWOOD = new Item(new Item.Settings());
    public static final Item FRIED_EGG = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(3).saturationModifier(2f).build()));
    public static final Item EXTINGUISHED_CAMPFIRE = new Item(new Item.Settings());
    public static final Item FIRE_BOW = new Item(new Item.Settings().maxCount(1));
    public static final Item TINDER = new Item(new Item.Settings());
    public static final Item WORM = new EffectiveFoodItem(new Item.Settings().food(new FoodComponent.Builder().hunger(2).saturationModifier(1.0f).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200), 1).build()), 0.0F, -0.08);
    public static final Item PUMPKIN_SLICE = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(2).saturationModifier(1f).build()));
    public static final Item POTHERB = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(0).saturationModifier(1f).build()));
    public static final Item STONE_KNIFE = new SwordItem(StoneKnifeToolMaterial.INSTANCE, 1, 1.6F - 4.0F, new Item.Settings().maxCount(1));
    public static final Item STONE_SPEAR = new SwordItem(StoneKnifeToolMaterial.INSTANCE, 2, 1.6F - 4.0F, new Item.Settings().maxCount(1));
    public static final Item STONE_CONE = new ShovelItem(StoneConeToolMaterial.INSTANCE, 1.0F, 1.6F - 4.0F, new Item.Settings().maxCount(1));
    public static final Item FLINT_KNIFE = new SwordItem(FlintKnifeToolMaterial.INSTANCE, 2, 1.6F - 4.0F, new Item.Settings().maxCount(1));
    public static final Item FLINT_SPEAR = new SwordItem(FlintKnifeToolMaterial.INSTANCE, 3, 1.6F - 4.0F, new Item.Settings().maxCount(1));
    public static final Item FLINT_CONE = new ShovelItem(FlintConeToolMaterial.INSTANCE, 1.5F, 1.6F - 4.0F, new Item.Settings().maxCount(1));
    public static final Item FLINT_HATCHET = new AxeItem(FlintHatchetToolMaterial.INSTANCE, 4.0F, 0.8F - 4.0F, new Item.Settings().maxCount(1));
    public static final Item RAW_COPPER_POWDER = new Item(new Item.Settings());
    public static final Item COPPER_SWORD = new SwordItem(CopperToolMaterial.INSTANCE, 5, 1.4F - 4.0F, new Item.Settings().maxCount(1));
    public static final Item COPPER_AXE = new AxeItem(CopperToolMaterial.INSTANCE, 7, 0.8F - 4.0F, new Item.Settings().maxCount(1));
    public static final Item COPPER_PICKAXE = new PickaxeItem(CopperToolMaterial.INSTANCE, 3, 1.1F - 4.0F, new Item.Settings().maxCount(1));
    public static final Item COPPER_HOE = new HoeItem(CopperToolMaterial.INSTANCE, 0, 2.5F - 4.0F, new Item.Settings().maxCount(1));
    public static final Item COPPER_SHOVEL = new ShovelItem(CopperToolMaterial.INSTANCE, 3, 1.0F - 4.0F, new Item.Settings().maxCount(1));
    public static final ArmorMaterial COPPER_ARMOR_MATERIAL = new CopperArmorMaterial();
    public static final Item COPPER_HELMET = new ArmorItem(COPPER_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings());
    public static final Item COPPER_CHESTPLATE = new ArmorItem(COPPER_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings());
    public static final Item COPPER_LEGGINGS = new ArmorItem(COPPER_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Settings());
    public static final Item COPPER_BOOTS = new ArmorItem(COPPER_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Settings());
    public static final Item SPIDER_GLAND = new SpiderGlandItem(new Item.Settings(), "item.hcs.spider_gland.tooltip", 8);
    public static final Item SELAGINELLA = new SpiderGlandItem(new Item.Settings(), "item.hcs.spider_gland.tooltip", 20);
    public static final Item RAW_MEAT = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(2).saturationModifier(1f).meat().build()));
    public static final Item COOKED_MEAT = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(5).saturationModifier(3f).meat().build()));
    public static final Item CACTUS_FLESH = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(1).saturationModifier(1f).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 160), 1).statusEffect(new StatusEffectInstance(HcsEffects.DIARRHEA, 300), 1).build()));
    public static final Item COOKED_CACTUS_FLESH = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(2).saturationModifier(2f).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 80), 1).build()));
    public static final Item CACTUS_JUICE = new BottleItem(new Item.Settings().maxCount(16), new StatusEffectInstance(StatusEffects.POISON, 160));
    public static final Item PURIFIED_WATER_BOTTLE = new BottleItem(new Item.Settings().maxCount(16));
    public static final Item SALTWATER_BOTTLE = new BottleItem(new Item.Settings().maxCount(16), new StatusEffectInstance(HcsEffects.THIRST, 1200, 0, false, false, true));
    public static final Item ROASTED_WORM = new EffectiveFoodItem(new Item.Settings().food(new FoodComponent.Builder().hunger(3).saturationModifier(1.0f).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60), 1).build()), 0.0F, -0.01);
    public static final Item ANIMAL_VISCERA = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(3).saturationModifier(2.0f).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 40), 1).meat().build()));
    public static final Item COOKED_ANIMAL_VISCERA = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(8).saturationModifier(0.8f).meat().build()));
    public static final Item SHARP_BROKEN_BONE = new ShovelItem(SharpBrokenBoneMaterial.INSTANCE, 3F, 1.6F - 4.0F, new Item.Settings().maxCount(1));
    public static final Item BAMBOO_SHOOT = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(1).saturationModifier(1f).build()));
    public static final Item COOKED_BAMBOO_SHOOT = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(3).saturationModifier(1f).build()));
    public static final Item COOKED_CARROT = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(4).saturationModifier(2f).build()));
    public static final Item COOKED_PUMPKIN_SLICE = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(3).saturationModifier(1f).build()));
    public static final Item COOKED_SWEET_BERRIES = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(1).saturationModifier(1f).build()));
    public static final Item BERRY_BUSH = new Item(new Item.Settings());
    public static final Item PETALS_SALAD = new BowlOfFoodItem(new Item.Settings().food(new FoodComponent.Builder().hunger(1).saturationModifier(1f).build()));
    public static final Item ORANGE = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(4).saturationModifier(3f).build()));
    public static final Item ROT = new BoneMealItem(new Item.Settings().food(new FoodComponent.Builder().hunger(0).saturationModifier(0.0f).build())) {
        @Override
        public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
            if (context.getWorld().getBlockState(context.getBlockPos()).isOf(Blocks.GRASS_BLOCK)) {
                return ActionResult.PASS;
            }
            return super.useOnBlock(context);
        }
    };
    public static final IceboxBlock ICEBOX = new IceboxBlock(AbstractBlock.Settings.of(Material.STONE).mapColor(MapColor.WHITE_GRAY).strength(2.0F, 3.0F).requiresTool().nonOpaque());
    public static final Item ICEBOX_ITEM = new BlockItem(ICEBOX, new Item.Settings());
    public static final DryingRackBlock DRYING_RACK = new DryingRackBlock(AbstractBlock.Settings.of(Material.WOOD).mapColor(MapColor.BROWN).strength(1.5F, 2.0F).requiresTool().nonOpaque());
    public static final Item DRYING_RACK_ITEM = new BlockItem(DRYING_RACK, new Item.Settings());
    public static final Item SHORT_STICK = new Item(new Item.Settings());
    public static final Item JERKY = new EffectiveFoodItem(new Item.Settings().food(new FoodComponent.Builder().hunger(8).saturationModifier(8.0F).meat().build()), 3.0F, 0.15);
    public static final Item SMALL_JERKY = new EffectiveFoodItem(new Item.Settings().food(new FoodComponent.Builder().hunger(5).saturationModifier(5.0F).meat().build()), 1.5F, 0.08);
    public static final Item RAW_JERKY = new EffectiveFoodItem(new Item.Settings().food(new FoodComponent.Builder().hunger(4).saturationModifier(4.0F).meat().build()), 1.5F, 0.0);
    public static final Item RAW_SMALL_JERKY = new EffectiveFoodItem(new Item.Settings().food(new FoodComponent.Builder().hunger(3).saturationModifier(3.0F).meat().build()), 1.0F, 0.0);
    public static final Item SPIKED_CLUB = new SwordItem(ToolMaterials.WOOD, 4, -2.4f, new Item.Settings());
    public static final Item COLD_WATER_BOTTLE = new BottleItem(new Item.Settings().maxCount(16)) {
        @Override
        public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
            if (user instanceof ServerPlayerEntity player) {
                TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
                if (temperatureManager.get() > 0.4) temperatureManager.add(-0.3);
                player.addStatusEffect(new StatusEffectInstance(HcsEffects.DIARRHEA, 600, 0, false, false, true));
            }
            return super.finishUsing(stack, world, user);
        }
    };
    public static final Item HOT_WATER_BOTTLE = new HotWaterBottleItem();
    public static final ArmorMaterial WOOLEN_ARMOR_MATERIAL = new WoolClothingMaterial();
    public static final Item WOOLEN_HOOD = new ArmorItem(WOOLEN_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings());
    public static final Item WOOLEN_COAT = new ArmorItem(WOOLEN_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings());
    public static final Item WOOLEN_TROUSERS = new ArmorItem(WOOLEN_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Settings());
    public static final Item WOOLEN_BOOTS = new ArmorItem(WOOLEN_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Settings());
    //See cooking output modification at AbstractCookingRecipeMixin/getOutPut
    public static final Item COOKED_KELP = new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(1).saturationModifier(1.0F).build()));

    public static final EntityType<RockProjectileEntity> ROCK_PROJECTILE_ENTITY = FabricEntityTypeBuilder.<RockProjectileEntity>create(SpawnGroup.MISC, RockProjectileEntity::new).dimensions(new EntityDimensions(0.25F, 0.25F, true)).build();
    public static final BlockEntityType<IceboxBlockEntity> ICEBOX_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(IceboxBlockEntity::new, ICEBOX).build();
    public static final BlockEntityType<DryingRackBlockEntity> DRYING_RACK_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(DryingRackBlockEntity::new, DRYING_RACK).build();

    public static final RecipeSerializer<ExtractWaterFromBambooRecipe> EXTRACT_WATER_FROM_BAMBOO_RECIPE = new SpecialRecipeSerializer<>(ExtractWaterFromBambooRecipe::new);
    public static final RecipeSerializer<ExtractWaterFromSnowRecipe> EXTRACT_WATER_FROM_SNOW_RECIPE = new SpecialRecipeSerializer<>(ExtractWaterFromSnowRecipe::new);
    public static final RecipeSerializer<PetalsSaladRecipe> PETALS_SALAD_RECIPE = new SpecialRecipeSerializer<>(PetalsSaladRecipe::new);
    public static final RecipeSerializer<SpikedClubRecipe> SPIKED_CLUB_RECIPE = new SpecialRecipeSerializer<>(SpikedClubRecipe::new);
    public static final RecipeSerializer<ColdWaterBottleRecipe> COLD_WATER_BOTTLE_RECIPE = new SpecialRecipeSerializer<>(ColdWaterBottleRecipe::new);
    public static final RecipeSerializer<HotWaterBottleRecipe> HOT_WATER_BOTTLE_RECIPE = new SpecialRecipeSerializer<>(HotWaterBottleRecipe::new);
    public static final ItemGroup HCS_ITEM_GROUP = FabricItemGroup.builder(new Identifier("hcs", "main")).icon(() -> new ItemStack(FLINT_HATCHET)).build();


    @Override
    public void onInitialize() {
        ServerC2S.init();
        AttackBlockEvent.init();
        AttackEntityEvent.init();
        BreakBlockEvent.init();
        EntitySleepEvent.init();
        ServerEntityEvent.init();
        ServerPlayerEvent.init();
        UseBlockEvent.init();

        ItemGroupEvents.modifyEntriesEvent(HCS_ITEM_GROUP).register(content -> {
            content.add(new ItemStack(GRASS_FIBER));
            content.add(new ItemStack(FIBER_STRING));
            content.add(new ItemStack(SHORT_STICK));
            content.add(new ItemStack(TINDER));
            content.add(new ItemStack(FIREWOOD));
            content.add(new ItemStack(FIRE_BOW));
            content.add(new ItemStack(EXTINGUISHED_CAMPFIRE));
            content.add(new ItemStack(ROCK));
            content.add(new ItemStack(SHARP_ROCK));
            content.add(new ItemStack(SHARP_FLINT));
            content.add(new ItemStack(RAW_COPPER_POWDER));
            content.add(new ItemStack(Items.WOODEN_SWORD));
            content.add(new ItemStack(SPIKED_CLUB));
            content.add(new ItemStack(SHARP_BROKEN_BONE));
            content.add(new ItemStack(STONE_CONE));
            content.add(new ItemStack(STONE_KNIFE));
            content.add(new ItemStack(STONE_SPEAR));
            content.add(new ItemStack(FLINT_CONE));
            content.add(new ItemStack(FLINT_KNIFE));
            content.add(new ItemStack(FLINT_SPEAR));
            content.add(new ItemStack(FLINT_HATCHET));
            content.add(new ItemStack(COPPER_SWORD));
            content.add(new ItemStack(COPPER_AXE));
            content.add(new ItemStack(COPPER_PICKAXE));
            content.add(new ItemStack(COPPER_HOE));
            content.add(new ItemStack(COPPER_SHOVEL));
            content.add(new ItemStack(COPPER_HELMET));
            content.add(new ItemStack(COPPER_CHESTPLATE));
            content.add(new ItemStack(COPPER_LEGGINGS));
            content.add(new ItemStack(COPPER_BOOTS));
            content.add(new ItemStack(WOOLEN_HOOD));
            content.add(new ItemStack(WOOLEN_COAT));
            content.add(new ItemStack(WOOLEN_TROUSERS));
            content.add(new ItemStack(WOOLEN_BOOTS));
            content.add(HOT_WATER_BOTTLE.getDefaultStack());
            content.add(new ItemStack(WORM));
            content.add(new ItemStack(ROASTED_WORM));
            content.add(new ItemStack(RAW_MEAT));
            content.add(new ItemStack(COOKED_MEAT));
            content.add(new ItemStack(ANIMAL_VISCERA));
            content.add(new ItemStack(COOKED_ANIMAL_VISCERA));
            content.add(new ItemStack(CACTUS_FLESH));
            content.add(new ItemStack(COOKED_CACTUS_FLESH));
            content.add(new ItemStack(BAMBOO_SHOOT));
            content.add(new ItemStack(COOKED_BAMBOO_SHOOT));
            content.add(new ItemStack(PUMPKIN_SLICE));
            content.add(new ItemStack(COOKED_PUMPKIN_SLICE));
            content.add(new ItemStack(ROASTED_SEEDS));
            content.add(new ItemStack(COOKED_CARROT));
            content.add(new ItemStack(COOKED_SWEET_BERRIES));
            content.add(new ItemStack(FRIED_EGG));
            content.add(new ItemStack(COOKED_KELP));
            content.add(new ItemStack(POTHERB));
            content.add(new ItemStack(ORANGE));
            content.add(new ItemStack(PETALS_SALAD));
            content.add(new ItemStack(JERKY));
            content.add(new ItemStack(SMALL_JERKY));
            content.add(new ItemStack(RAW_JERKY));
            content.add(new ItemStack(RAW_SMALL_JERKY));
            content.add(new ItemStack(ROT));
            content.add(new ItemStack(SELAGINELLA));
            content.add(new ItemStack(SPIDER_GLAND));
            content.add(new ItemStack(BERRY_BUSH));
            content.add(new ItemStack(PURIFIED_WATER_BOTTLE));//.getDefaultStack()
            content.add(new ItemStack(SALTWATER_BOTTLE));
            content.add(new ItemStack(COLD_WATER_BOTTLE));
            content.add(new ItemStack(CACTUS_JUICE));
            content.add(PotionUtil.setPotion(new ItemStack(Items.POTION), IRONSKIN_POTION));
            content.add(PotionUtil.setPotion(new ItemStack(Items.POTION), RETURN_POTION));
            content.add(PotionUtil.setPotion(new ItemStack(Items.POTION), MINING_POTION));
            content.add(new ItemStack(ICEBOX_ITEM));
            content.add(new ItemStack(DRYING_RACK_ITEM));
        });


        Registry.register(Registries.ITEM, new Identifier("hcs", "grass_fiber"), GRASS_FIBER);
        Registry.register(Registries.ITEM, new Identifier("hcs", "fiber_string"), FIBER_STRING);
        Registry.register(Registries.ITEM, new Identifier("hcs", "rock"), ROCK);
        Registry.register(Registries.ITEM, new Identifier("hcs", "sharp_rock"), SHARP_ROCK);
        Registry.register(Registries.ITEM, new Identifier("hcs", "sharp_flint"), SHARP_FLINT);
        Registry.register(Registries.ITEM, new Identifier("hcs", "firewood"), FIREWOOD);
        Registry.register(Registries.ITEM, new Identifier("hcs", "extinguished_campfire"), EXTINGUISHED_CAMPFIRE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "fire_bow"), FIRE_BOW);
        Registry.register(Registries.ITEM, new Identifier("hcs", "tinder"), TINDER);
        Registry.register(Registries.ITEM, new Identifier("hcs", "roasted_seeds"), ROASTED_SEEDS);
        Registry.register(Registries.ITEM, new Identifier("hcs", "fried_egg"), FRIED_EGG);
        Registry.register(Registries.ITEM, new Identifier("hcs", "worm"), WORM);
        Registry.register(Registries.ITEM, new Identifier("hcs", "pumpkin_slice"), PUMPKIN_SLICE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "potherb"), POTHERB);
        Registry.register(Registries.ITEM, new Identifier("hcs", "stone_knife"), STONE_KNIFE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "stone_cone"), STONE_CONE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "stone_spear"), STONE_SPEAR);
        Registry.register(Registries.ITEM, new Identifier("hcs", "flint_knife"), FLINT_KNIFE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "flint_cone"), FLINT_CONE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "flint_hatchet"), FLINT_HATCHET);
        Registry.register(Registries.ITEM, new Identifier("hcs", "flint_spear"), FLINT_SPEAR);
        Registry.register(Registries.ITEM, new Identifier("hcs", "raw_copper_powder"), RAW_COPPER_POWDER);
        Registry.register(Registries.ITEM, new Identifier("hcs", "copper_sword"), COPPER_SWORD);
        Registry.register(Registries.ITEM, new Identifier("hcs", "copper_axe"), COPPER_AXE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "copper_pickaxe"), COPPER_PICKAXE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "copper_hoe"), COPPER_HOE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "copper_shovel"), COPPER_SHOVEL);
        Registry.register(Registries.ITEM, new Identifier("hcs", "copper_helmet"), COPPER_HELMET);
        Registry.register(Registries.ITEM, new Identifier("hcs", "copper_chestplate"), COPPER_CHESTPLATE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "copper_leggings"), COPPER_LEGGINGS);
        Registry.register(Registries.ITEM, new Identifier("hcs", "copper_boots"), COPPER_BOOTS);
        Registry.register(Registries.ITEM, new Identifier("hcs", "spider_gland"), SPIDER_GLAND);
        Registry.register(Registries.ITEM, new Identifier("hcs", "selaginella"), SELAGINELLA);
        Registry.register(Registries.ITEM, new Identifier("hcs", "raw_meat"), RAW_MEAT);
        Registry.register(Registries.ITEM, new Identifier("hcs", "cooked_meat"), COOKED_MEAT);
        Registry.register(Registries.ITEM, new Identifier("hcs", "cactus_flesh"), CACTUS_FLESH);
        Registry.register(Registries.ITEM, new Identifier("hcs", "cooked_cactus_flesh"), COOKED_CACTUS_FLESH);
        Registry.register(Registries.ITEM, new Identifier("hcs", "cactus_juice"), CACTUS_JUICE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "purified_water_bottle"), PURIFIED_WATER_BOTTLE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "saltwater_bottle"), SALTWATER_BOTTLE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "roasted_worm"), ROASTED_WORM);
        Registry.register(Registries.ITEM, new Identifier("hcs", "animal_viscera"), ANIMAL_VISCERA);
        Registry.register(Registries.ITEM, new Identifier("hcs", "cooked_animal_viscera"), COOKED_ANIMAL_VISCERA);
        Registry.register(Registries.ITEM, new Identifier("hcs", "sharp_broken_bone"), SHARP_BROKEN_BONE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "bamboo_shoot"), BAMBOO_SHOOT);
        Registry.register(Registries.ITEM, new Identifier("hcs", "cooked_bamboo_shoot"), COOKED_BAMBOO_SHOOT);
        Registry.register(Registries.ITEM, new Identifier("hcs", "cooked_carrot"), COOKED_CARROT);
        Registry.register(Registries.ITEM, new Identifier("hcs", "cooked_pumpkin_slice"), COOKED_PUMPKIN_SLICE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "cooked_sweet_berries"), COOKED_SWEET_BERRIES);
        Registry.register(Registries.ITEM, new Identifier("hcs", "berry_bush"), BERRY_BUSH);
        Registry.register(Registries.ITEM, new Identifier("hcs", "petals_salad"), PETALS_SALAD);
        Registry.register(Registries.ITEM, new Identifier("hcs", "orange"), ORANGE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "rot"), ROT);
        Registry.register(Registries.ITEM, new Identifier("hcs", "short_stick"), SHORT_STICK);
        Registry.register(Registries.ITEM, new Identifier("hcs", "jerky"), JERKY);
        Registry.register(Registries.ITEM, new Identifier("hcs", "small_jerky"), SMALL_JERKY);
        Registry.register(Registries.ITEM, new Identifier("hcs", "raw_jerky"), RAW_JERKY);
        Registry.register(Registries.ITEM, new Identifier("hcs", "raw_small_jerky"), RAW_SMALL_JERKY);
        Registry.register(Registries.ITEM, new Identifier("hcs", "spiked_club"), SPIKED_CLUB);
        Registry.register(Registries.ITEM, new Identifier("hcs", "cold_water_bottle"), COLD_WATER_BOTTLE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "woolen_hood"), WOOLEN_HOOD);
        Registry.register(Registries.ITEM, new Identifier("hcs", "woolen_coat"), WOOLEN_COAT);
        Registry.register(Registries.ITEM, new Identifier("hcs", "woolen_trousers"), WOOLEN_TROUSERS);
        Registry.register(Registries.ITEM, new Identifier("hcs", "woolen_boots"), WOOLEN_BOOTS);
        Registry.register(Registries.ITEM, new Identifier("hcs", "hot_water_bottle"), HOT_WATER_BOTTLE);
        Registry.register(Registries.ITEM, new Identifier("hcs", "cooked_kelp"), COOKED_KELP);

        Registry.register(Registries.BLOCK, new Identifier("hcs", "icebox"), ICEBOX);
        Registry.register(Registries.ITEM, new Identifier("hcs", "icebox"), ICEBOX_ITEM);
        Registry.register(Registries.BLOCK, new Identifier("hcs", "drying_rack"), DRYING_RACK);
        Registry.register(Registries.ITEM, new Identifier("hcs", "drying_rack"), DRYING_RACK_ITEM);

        Registry.register(Registries.POTION, "hcs_ironskin", IRONSKIN_POTION);
        Registry.register(Registries.POTION, "hcs_long_ironskin", LONG_IRONSKIN_POTION);
        Registry.register(Registries.POTION, "hcs_strong_ironskin", STRONG_IRONSKIN_POTION);
        Registry.register(Registries.POTION, "hcs_return", RETURN_POTION);
        Registry.register(Registries.POTION, "hcs_mining", MINING_POTION);
        Registry.register(Registries.POTION, "hcs_long_mining", LONG_MINING_POTION);
        Registry.register(Registries.POTION, "hcs_strong_mining", STRONG_MINING_POTION);

        Registry.register(Registries.ENTITY_TYPE, new Identifier("hcs", "rock_projectile_entity"), ROCK_PROJECTILE_ENTITY);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("hcs", "icebox_block_entity"), ICEBOX_BLOCK_ENTITY);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("hcs", "drying_rack_block_entity"), DRYING_RACK_BLOCK_ENTITY);

        Registry.register(Registries.STATUS_EFFECT, new Identifier("hcs", "thirst"), HcsEffects.THIRST);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("hcs", "diarrhea"), HcsEffects.DIARRHEA);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("hcs", "dehydrated"), HcsEffects.DEHYDRATED);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("hcs", "starving"), HcsEffects.STARVING);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("hcs", "return"), HcsEffects.RETURN);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("hcs", "exhausted"), HcsEffects.EXHAUSTED);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("hcs", "hypothermia"), HcsEffects.HYPOTHERMIA);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("hcs", "heatstroke"), HcsEffects.HEATSTROKE);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("hcs", "strong_sun"), HcsEffects.STRONG_SUN);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("hcs", "chilly_wind"), HcsEffects.CHILLY_WIND);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("hcs", "overeaten"), HcsEffects.OVEREATEN);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("hcs", "insanity"), HcsEffects.INSANITY);
        Registry.register(Registries.STATUS_EFFECT, new Identifier("hcs", "malnutrition"), HcsEffects.MALNUTRITION);

        RecipeSerializer.register("hcs_extract_water_from_bamboo", EXTRACT_WATER_FROM_BAMBOO_RECIPE);
        RecipeSerializer.register("hcs_extract_water_from_snow", EXTRACT_WATER_FROM_SNOW_RECIPE);
        RecipeSerializer.register("hcs_petals_salad", PETALS_SALAD_RECIPE);
        RecipeSerializer.register("hcs_spiked_club_recipe", SPIKED_CLUB_RECIPE);
        RecipeSerializer.register("hcs_cold_water_bottle_recipe", COLD_WATER_BOTTLE_RECIPE);
        RecipeSerializer.register("hcs_hot_water_bottle_recipe", HOT_WATER_BOTTLE_RECIPE);

        FuelRegistry.INSTANCE.add(GRASS_FIBER, 50);
        FuelRegistry.INSTANCE.add(SHORT_STICK, 80);
        FuelRegistry.INSTANCE.add(FIREWOOD, 300);
        FuelRegistry.INSTANCE.add(TINDER, 30);
        FuelRegistry.INSTANCE.add(ROT, 100);

        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(BERRY_BUSH, 0.3F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(ROASTED_SEEDS, 0.3F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(POTHERB, 0.3F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(PUMPKIN_SLICE, 0.5F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(COOKED_PUMPKIN_SLICE, 0.5F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(PUMPKIN_SLICE, 0.5F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(COOKED_PUMPKIN_SLICE, 0.5F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(CACTUS_FLESH, 0.5F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(COOKED_CACTUS_FLESH, 0.5F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(BAMBOO_SHOOT, 0.5F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(COOKED_BAMBOO_SHOOT, 0.5F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(COOKED_CARROT, 0.5F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(COOKED_SWEET_BERRIES, 0.5F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(ORANGE, 0.65F);
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(ROT, 1.0F);
    }
}
