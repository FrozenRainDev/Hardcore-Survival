package biz.coolpage.hcs;

import biz.coolpage.hcs.block.BurntCampfireBlock;
import biz.coolpage.hcs.block.DryingRackBlock;
import biz.coolpage.hcs.block.IceboxBlock;
import biz.coolpage.hcs.block.SmolderingCampfireBlock;
import biz.coolpage.hcs.block.torches.*;
import biz.coolpage.hcs.config.Config__;
import biz.coolpage.hcs.entity.*;
import biz.coolpage.hcs.item.*;
import biz.coolpage.hcs.item.BottleItem;
import biz.coolpage.hcs.recipe.*;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.ServerC2S;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import biz.coolpage.hcs.util.CombustionHelper;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.WorldHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.function.Predicate;

import static biz.coolpage.hcs.config.Configs.FOOD_SPOIL;
import static net.minecraft.commands.Commands.literal;

@Mod(Hcs.MOD_ID)
public final class Hcs {
    public static final String MOD_ID = "hcsurvival", MOD_NAME = "Hardcore Survival";
    private static final Logger LOGGER = LogUtils.getLogger();

    // Registers
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MOD_ID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    // --- Blocks ---
    public static final RegistryObject<IceboxBlock> ICEBOX = BLOCKS.register("icebox", () -> new IceboxBlock(BlockBehaviour.Properties.copy(Blocks.COBBLESTONE).mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0F).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistryObject<DryingRackBlock> DRYING_RACK = BLOCKS.register("drying_rack", () -> new DryingRackBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE).mapColor(MapColor.WOOD).strength(1.5F, 2.0F).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistryObject<CrudeTorchBlock> CRUDE_TORCH_BLOCK = BLOCKS.register("crude_torch", () -> new CrudeTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).noCollission().instabreak().lightLevel(state -> 0).sound(SoundType.WOOD)));
    public static final RegistryObject<WallCrudeTorchBlock> WALL_CRUDE_TORCH_BLOCK = BLOCKS.register("wall_crude_torch", () -> new WallCrudeTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noCollission().instabreak().lightLevel(state -> 0).sound(SoundType.WOOD).dropsLike(CRUDE_TORCH_BLOCK.get())));
    public static final RegistryObject<BurningCrudeTorchBlock> BURNING_CRUDE_TORCH_BLOCK = BLOCKS.register("burning_crude_torch", () -> new BurningCrudeTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).noCollission().instabreak().lightLevel(state -> 13).sound(SoundType.WOOD)));
    public static final RegistryObject<WallBurningCrudeTorchBlock> WALL_BURNING_CRUDE_TORCH_BLOCK = BLOCKS.register("wall_burning_crude_torch", () -> new WallBurningCrudeTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noCollission().instabreak().lightLevel(state -> 13).sound(SoundType.WOOD).dropsLike(BURNING_CRUDE_TORCH_BLOCK.get())));
    public static final RegistryObject<UnlitTorchBlock> UNLIT_TORCH_BLOCK = BLOCKS.register("unlit_torch", () -> new UnlitTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).lightLevel(state -> 0).noCollission().instabreak().sound(SoundType.WOOD)));
    public static final RegistryObject<WallUnlitTorchBlock> WALL_UNLIT_TORCH_BLOCK = BLOCKS.register("wall_unlit_torch", () -> new WallUnlitTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noCollission().instabreak().sound(SoundType.WOOD).dropsLike(UNLIT_TORCH_BLOCK.get())));
    public static final RegistryObject<BurntTorchBlock> BURNT_TORCH_BLOCK = BLOCKS.register("burnt_torch", () -> new BurntTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).lightLevel(state -> 0).noCollission().instabreak().sound(SoundType.WOOD)));
    public static final RegistryObject<WallBurntTorchBlock> WALL_BURNT_TORCH_BLOCK = BLOCKS.register("wall_burnt_torch", () -> new WallBurntTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).lightLevel(state -> 0).noCollission().instabreak().sound(SoundType.WOOD).dropsLike(BURNT_TORCH_BLOCK.get())));
    public static final RegistryObject<GlowstoneTorchBlock> GLOWSTONE_TORCH_BLOCK = BLOCKS.register("glowstone_torch", () -> new GlowstoneTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).noCollission().instabreak().lightLevel(state -> 15).sound(SoundType.WOOD)));
    public static final RegistryObject<WallGlowstoneTorchBlock> WALL_GLOWSTONE_TORCH_BLOCK = BLOCKS.register("wall_glowstone_torch", () -> new WallGlowstoneTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noCollission().instabreak().lightLevel(state -> 14).sound(SoundType.WOOD).dropsLike(GLOWSTONE_TORCH_BLOCK.get())));
    public static final RegistryObject<Block> SMOLDERING_CAMPFIRE_BLOCK = BLOCKS.register("smoldering_campfire", SmolderingCampfireBlock::new);
    public static final RegistryObject<Block> BURNT_CAMPFIRE_BLOCK = BLOCKS.register("burnt_campfire", BurntCampfireBlock::new);


    // --- Items ---
    public static final RegistryObject<Item>
            FIBER_STRING = ITEMS.register("fiber_string", () -> new Item(new Item.Properties())),
            GRASS_FIBER = ITEMS.register("grass_fiber", () -> new Item(new Item.Properties())),
            ROASTED_SEEDS = ITEMS.register("roasted_seeds", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0f).fast().build()))),
            ROCK = ITEMS.register("rock", () -> new RockItem(new Item.Properties())),
            SHARP_ROCK = ITEMS.register("sharp_rock", () -> new Item(new Item.Properties())),
            SHARP_FLINT = ITEMS.register("sharp_flint", () -> new Item(new Item.Properties())),
            FIREWOOD = ITEMS.register("firewood", () -> new Item(new Item.Properties())),
            FRIED_EGG = ITEMS.register("fried_egg", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationMod(2f).build()))),
            EXTINGUISHED_CAMPFIRE = ITEMS.register("extinguished_campfire", () -> new HCSCampfireItem(Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, false).setValue(CombustionHelper.COMBUST_LUMINANCE, 15))),
            FIRE_BOW = ITEMS.register("fire_bow", () -> new FireBowItem(new Item.Properties().stacksTo(1).durability(96), 1)),
            FIRE_PLOUGH = ITEMS.register("fire_plough", () -> new FireBowItem(new Item.Properties().stacksTo(1).durability(64), 3)),
            TINDER = ITEMS.register("tinder", () -> new Item(new Item.Properties())),
            WORM = ITEMS.register("worm", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200), 1).build()), 0.0F, -0.08)),
            PUMPKIN_SLICE = ITEMS.register("pumpkin_slice", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(0f).build()))),
            POTHERB = ITEMS.register("potherb", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(1f).build()))),
            STONE_KNIFE = ITEMS.register("stone_knife", () -> new KnifeItem(HcsTiers.STONE_WEAPON, 1, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            STONE_SPEAR = ITEMS.register("stone_spear", () -> new SwordItem(HcsTiers.STONE_WEAPON, 2, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            STONE_CONE = ITEMS.register("stone_cone", () -> new ShovelItem(HcsTiers.STONE_CONE, 1.0F, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            FLINT_KNIFE = ITEMS.register("flint_knife", () -> new KnifeItem(HcsTiers.FLINT_WEAPON, 2, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            FLINT_SPEAR = ITEMS.register("flint_spear", () -> new SwordItem(HcsTiers.FLINT_WEAPON, 3, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            FLINT_CONE = ITEMS.register("flint_cone", () -> new ShovelItem(HcsTiers.FLINT_CONE, 1.5F, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            FLINT_HATCHET = ITEMS.register("flint_hatchet", () -> new AxeItem(HcsTiers.FLINT_HATCHET, 6.0F, -3.1F, new Item.Properties())),
            RAW_COPPER_POWDER = ITEMS.register("raw_copper_powder", () -> new Item(new Item.Properties())),
            COPPER_SWORD = ITEMS.register("copper_sword", () -> new SwordItem(HcsTiers.COPPER, 5, 1.4F - 4.0F, new Item.Properties().stacksTo(1))),
            COPPER_AXE = ITEMS.register("copper_axe", () -> new AxeItem(HcsTiers.COPPER, 7, 0.8F - 4.0F, new Item.Properties().stacksTo(1))),
            COPPER_PICKAXE = ITEMS.register("copper_pickaxe", () -> new PickaxeItem(HcsTiers.COPPER, 3, 1.1F - 4.0F, new Item.Properties().stacksTo(1))),
            COPPER_HOE = ITEMS.register("copper_hoe", () -> new HoeItem(HcsTiers.COPPER, 0, 2.5F - 4.0F, new Item.Properties().stacksTo(1))),
            COPPER_SHOVEL = ITEMS.register("copper_shovel", () -> new ShovelItem(HcsTiers.COPPER, 3, 1.0F - 4.0F, new Item.Properties().stacksTo(1))),
            COPPER_HELMET = ITEMS.register("copper_helmet", () -> new ArmorItem(HcsArmorMaterials.COPPER, ArmorItem.Type.HELMET, new Item.Properties())),
            COPPER_CHESTPLATE = ITEMS.register("copper_chestplate", () -> new ArmorItem(HcsArmorMaterials.COPPER, ArmorItem.Type.CHESTPLATE, new Item.Properties())),
            COPPER_LEGGINGS = ITEMS.register("copper_leggings", () -> new ArmorItem(HcsArmorMaterials.COPPER, ArmorItem.Type.LEGGINGS, new Item.Properties())),
            COPPER_BOOTS = ITEMS.register("copper_boots", () -> new ArmorItem(HcsArmorMaterials.COPPER, ArmorItem.Type.BOOTS, new Item.Properties())),
            SPIDER_GLAND = ITEMS.register("spider_gland", () -> new SalveItem(8, 0.5)),
            SELAGINELLA = ITEMS.register("selaginella", () -> new SalveItem(20, 1.5) {
                @Override
                public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user) {
                    if (user instanceof ServerPlayer player) {
                        ((StatAccessor) player).getStatusManager().setSoulImpairedStat(0);
                        ((StatAccessor) player).getInjuryManager().applyPainkiller();
                    }
                    return super.finishUsingItem(stack, world, user);
                }
            }),
            RAW_MEAT = ITEMS.register("raw_meat", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(1f).meat().build()))),
            COOKED_MEAT = ITEMS.register("cooked_meat", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationMod(3f).meat().build()))),
            CACTUS_FLESH = ITEMS.register("cactus_flesh", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1f).effect(() -> new MobEffectInstance(MobEffects.POISON, 160), 1).effect(() -> new MobEffectInstance(HcsEffects.DIARRHEA.get(), 300), 1).build()))),
            COOKED_CACTUS_FLESH = ITEMS.register("cooked_cactus_flesh", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(2f).effect(() -> new MobEffectInstance(MobEffects.POISON, 80), 1).build()))),
            CACTUS_JUICE = ITEMS.register("cactus_juice", () -> new BottleItem(new Item.Properties().stacksTo(16), new MobEffectInstance(MobEffects.POISON, 160))),
            PURIFIED_WATER_BOTTLE = ITEMS.register("purified_water_bottle", () -> new BottleItem(new Item.Properties().stacksTo(16))),
            SALTWATER_BOTTLE = ITEMS.register("saltwater_bottle", () -> new BottleItem(new Item.Properties().stacksTo(16), new MobEffectInstance(HcsEffects.THIRST.get(), 1200, 0, false, false, true))),
            ROASTED_WORM = ITEMS.register("roasted_worm", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(1.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 60), 1).build()), 0.0F, -0.01)),
            ANIMAL_VISCERA = ITEMS.register("animal_viscera", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationMod(2.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 40), 1).meat().build()))),
            COOKED_ANIMAL_VISCERA = ITEMS.register("cooked_animal_viscera", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationMod(4F).meat().build()))),
            SHARP_BROKEN_BONE = ITEMS.register("sharp_broken_bone", () -> new ShovelItem(HcsTiers.SHARP_BROKEN_BONE, 3F, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            BAMBOO_SHOOT = ITEMS.register("bamboo_shoot", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1f).build()))),
            COOKED_BAMBOO_SHOOT = ITEMS.register("cooked_bamboo_shoot", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(1f).build()))),
            COOKED_CARROT = ITEMS.register("cooked_carrot", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationMod(2f).build()))),
            COOKED_PUMPKIN_SLICE = ITEMS.register("cooked_pumpkin_slice", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(0f).build()))),
            COOKED_SWEET_BERRIES = ITEMS.register("cooked_sweet_berries", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1f).build()))),
            BERRY_BUSH = ITEMS.register("berry_bush", () -> new Item(new Item.Properties())),
            PETALS_SALAD = ITEMS.register("petals_salad", () -> new BowlOfFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0f).build()))),
            ORANGE = ITEMS.register("orange", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationMod(3f).build()))),
            ROT = ITEMS.register("rot", () -> new BoneMealItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0.0f).build())) {
                @Override
                public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
                    if (context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.GRASS_BLOCK))
                        return InteractionResult.PASS;
                    return super.useOn(context);
                }
            }),
            ICEBOX_ITEM = ITEMS.register("icebox", () -> new BlockItem(ICEBOX.get(), new Item.Properties())),
            DRYING_RACK_ITEM = ITEMS.register("drying_rack", () -> new BlockItem(DRYING_RACK.get(), new Item.Properties())),
            SHORT_STICK = ITEMS.register("short_stick", () -> new Item(new Item.Properties())),
            JERKY = ITEMS.register("jerky", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationMod(8.0F).meat().build()), 3.0F, 0.15)),
            SMALL_JERKY = ITEMS.register("small_jerky", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationMod(5.0F).meat().build()), 1.5F, 0.08)),
            RAW_JERKY = ITEMS.register("raw_jerky", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationMod(4.0F).meat().build()), 1.5F, 0.0)),
            RAW_SMALL_JERKY = ITEMS.register("raw_small_jerky", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationMod(3.0F).meat().build()), 1.0F, 0.0)),
            SPIKED_CLUB = ITEMS.register("spiked_club", () -> new SwordItem(Tiers.WOOD, 4, -2.4f, new Item.Properties())),
            COLD_WATER_BOTTLE = ITEMS.register("cold_water_bottle", () -> new BottleItem(new Item.Properties().stacksTo(16)) {
                @Override
                public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user) {
                    if (user instanceof ServerPlayer player) {
                        TemperatureManager tm = ((StatAccessor) player).getTemperatureManager();
                        if (tm.get() > 0.4) tm.add(-0.3);
                        player.addEffect(new MobEffectInstance(HcsEffects.DIARRHEA.get(), 600, 0, false, false, true));
                    }
                    return super.finishUsingItem(stack, world, user);
                }
            }),
            HOT_WATER_BOTTLE = ITEMS.register("hot_water_bottle", HotWaterBottleItem::new),
            WOOLEN_HOOD = ITEMS.register("woolen_hood", () -> new ArmorItem(HcsArmorMaterials.WOOL, ArmorItem.Type.HELMET, new Item.Properties())),
            WOOLEN_COAT = ITEMS.register("woolen_coat", () -> new ArmorItem(HcsArmorMaterials.WOOL, ArmorItem.Type.CHESTPLATE, new Item.Properties())),
            WOOLEN_TROUSERS = ITEMS.register("woolen_trousers", () -> new ArmorItem(HcsArmorMaterials.WOOL, ArmorItem.Type.LEGGINGS, new Item.Properties())),
            WOOLEN_BOOTS = ITEMS.register("woolen_boots", () -> new ArmorItem(HcsArmorMaterials.WOOL, ArmorItem.Type.BOOTS, new Item.Properties())),
            COOKED_KELP = ITEMS.register("cooked_kelp", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0F).build()))),
            BARK = ITEMS.register("bark", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0.0F).build()))),
            WILLOW_BARK = ITEMS.register("willow_bark", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0.0F).build()))),
            FEARLESSNESS_HERB = ITEMS.register("fearlessness_herb", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0.0F).build()))),
            BANDAGE = ITEMS.register("bandage", () -> new BandageItem(2.0, 40, 200)),
            IMPROVISED_BANDAGE = ITEMS.register("improvised_bandage", () -> new BandageItem(0.8, 60, 120)),
            SPLINT = ITEMS.register("splint", () -> new BandageItem(0.5, 140) {
                @Override
                public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user) {
                    if (user instanceof ServerPlayer player)
                        ((StatAccessor) player).getInjuryManager().setFracture(0.0);
                    return super.finishUsingItem(stack, world, user);
                }
            }),
            GINGER = ITEMS.register("ginger", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0F).build()))),
            WOODEN_HELMET = ITEMS.register("wooden_helmet", () -> new ArmorItem(HcsArmorMaterials.WOOD, ArmorItem.Type.HELMET, new Item.Properties())),
            WOODEN_CHESTPLATE = ITEMS.register("wooden_chestplate", () -> new ArmorItem(HcsArmorMaterials.WOOD, ArmorItem.Type.CHESTPLATE, new Item.Properties())),
            WOODEN_LEGGINGS = ITEMS.register("wooden_leggings", () -> new ArmorItem(HcsArmorMaterials.WOOD, ArmorItem.Type.LEGGINGS, new Item.Properties())),
            WOODEN_BOOTS = ITEMS.register("wooden_boots", () -> new ArmorItem(HcsArmorMaterials.WOOD, ArmorItem.Type.BOOTS, new Item.Properties())),
            IMPROVISED_SHIELD = ITEMS.register("improvised_shield", () -> new ShieldItem(new Item.Properties().durability(48)) {
                @Contract(pure = true)
                @Override
                public @NotNull String getDescriptionId(@NotNull ItemStack stack) {
                    return "item.hcs.improvised_shield";
                }
            }),
            BAT_WINGS = ITEMS.register("bat_wings", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200), 1).effect(() -> new MobEffectInstance(HcsEffects.DIARRHEA.get(), 200), 0.7F).build()), 0.0F, -0.06)),
            ROASTED_BAT_WINGS = ITEMS.register("roasted_bat_wings", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 60), 1).build()), 0.0F, -0.01)),
            HEALING_SALVE = ITEMS.register("healing_salve", () -> new SalveItem(14, 1.5, 20) {
                @Override
                public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user) {
                    if (user instanceof ServerPlayer player) EntityHelper.dropItem(player, Items.BOWL);
                    return super.finishUsingItem(stack, world, user);
                }
            }),
            ASHES = ITEMS.register("ashes", () -> new SalveItem(0, 0.2, 50)),
            CRUDE_TORCH_ITEM = ITEMS.register("crude_torch", () -> new StandingAndWallBlockItem(CRUDE_TORCH_BLOCK.get(), WALL_CRUDE_TORCH_BLOCK.get(), new Item.Properties(), Direction.DOWN) {
                @Override
                public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
                    InteractionResult performed = CombustionHelper.preLitHoldingTorch(context);
                    return performed == null ? super.useOn(context) : performed;
                }
            }),
            BURNING_CRUDE_TORCH_ITEM = ITEMS.register("burning_crude_torch", BurningCrudeTorchItem::new),
            UNLIT_TORCH_ITEM = ITEMS.register("unlit_torch", () -> new StandingAndWallBlockItem(UNLIT_TORCH_BLOCK.get(), WALL_UNLIT_TORCH_BLOCK.get(), new Item.Properties(), Direction.DOWN) {
                @Override
                public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
                    InteractionResult performed = CombustionHelper.preLitHoldingTorch(context);
                    return performed == null ? super.useOn(context) : performed;
                }
            }),
            GLOWSTONE_TORCH_ITEM = ITEMS.register("glowstone_torch", () -> new StandingAndWallBlockItem(GLOWSTONE_TORCH_BLOCK.get(), WALL_GLOWSTONE_TORCH_BLOCK.get(), new Item.Properties(), Direction.DOWN)),
            BOOSTER_SHOT = ITEMS.register("booster_shot", BoosterShotItem::new),
            SMOLDERING_CAMPFIRE = ITEMS.register("smoldering_campfire", () -> new HCSCampfireItem(SMOLDERING_CAMPFIRE_BLOCK.get().defaultBlockState())),
            BURNT_CAMPFIRE = ITEMS.register("burnt_campfire", () -> new HCSCampfireItem(BURNT_CAMPFIRE_BLOCK.get().defaultBlockState())),
            GARLAND = ITEMS.register("garland", () -> new ArmorItem(HcsArmorMaterials.GARLAND, ArmorItem.Type.HELMET, new Item.Properties()));

    // --- Potions ---
    public static final RegistryObject<Potion>
            IRONSKIN_POTION = POTIONS.register("hcs_ironskin", () -> new Potion(new MobEffectInstance(HcsEffects.IRONSKIN.get(), 3600, 0))),
            LONG_IRONSKIN_POTION = POTIONS.register("hcs_long_ironskin", () -> new Potion(new MobEffectInstance(HcsEffects.IRONSKIN.get(), 9600, 0))),
            STRONG_IRONSKIN_POTION = POTIONS.register("hcs_strong_ironskin", () -> new Potion(new MobEffectInstance(HcsEffects.IRONSKIN.get(), 1800, 1))),
            RETURN_POTION = POTIONS.register("hcs_return", () -> new Potion(new MobEffectInstance(HcsEffects.RETURN.get(), 120, 0, false, true, false))),
            MINING_POTION = POTIONS.register("hcs_mining", () -> new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 3600))),
            LONG_MINING_POTION = POTIONS.register("hcs_long_mining", () -> new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 9600))),
            STRONG_MINING_POTION = POTIONS.register("hcs_strong_mining", () -> new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 1800, 1))),
            CONSTANT_TEMPERATURE_POTION = POTIONS.register("hcs_constant_temperature", () -> new Potion(new MobEffectInstance(HcsEffects.CONSTANT_TEMPERATURE.get(), 3600))),
            LONG_CONSTANT_TEMPERATURE_POTION = POTIONS.register("hcs_long_constant_temperature", () -> new Potion(new MobEffectInstance(HcsEffects.CONSTANT_TEMPERATURE.get(), 9600))),
            PAIN_KILLING_POTION = POTIONS.register("hcs_pain_killing", () -> new Potion(new MobEffectInstance(HcsEffects.PAIN_KILLING.get(), 3600))),
            LONG_PAIN_KILLING_POTION = POTIONS.register("hcs_long_pain_killing", () -> new Potion(new MobEffectInstance(HcsEffects.PAIN_KILLING.get(), 9600))),
            FEARLESSNESS_POTION = POTIONS.register("hcs_fearlessness", () -> new Potion(new MobEffectInstance(HcsEffects.FEARLESSNESS.get(), 3600))),
            LONG_FEARLESSNESS_POTION = POTIONS.register("hcs_long_fearlessness", () -> new Potion(new MobEffectInstance(HcsEffects.FEARLESSNESS.get(), 9600)));

    // --- Entity Types ---
    public static final RegistryObject<EntityType<RockProjectileEntity>> ROCK_PROJECTILE_ENTITY = ENTITIES.register("rock_projectile_entity", () -> EntityType.Builder.<RockProjectileEntity>of(RockProjectileEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).build("rock_projectile_entity"));
    public static final RegistryObject<EntityType<FlintProjectileEntity>> FLINT_PROJECTILE_ENTITY = ENTITIES.register("flint_projectile_entity", () -> EntityType.Builder.<FlintProjectileEntity>of(FlintProjectileEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).build("flint_projectile_entity"));

    // --- Block Entities ---
    public static final RegistryObject<BlockEntityType<IceboxBlockEntity>> ICEBOX_BLOCK_ENTITY = BLOCK_ENTITIES.register("icebox_block_entity", () -> BlockEntityType.Builder.of(IceboxBlockEntity::new, ICEBOX.get()).build(null));
    public static final RegistryObject<BlockEntityType<DryingRackBlockEntity>> DRYING_RACK_BLOCK_ENTITY = BLOCK_ENTITIES.register("drying_rack_block_entity", () -> BlockEntityType.Builder.of(DryingRackBlockEntity::new, DRYING_RACK.get()).build(null));
    public static final RegistryObject<BlockEntityType<BurningCrudeTorchBlockEntity>> BURNING_CRUDE_TORCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("burning_crude_torch", () -> BlockEntityType.Builder.of(BurningCrudeTorchBlockEntity::new, BURNING_CRUDE_TORCH_BLOCK.get(), WALL_BURNING_CRUDE_TORCH_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<SmolderingOrBurntCampfireBlockEntity>> SMOLDERING_OR_BURNT_CAMPFIRE_BLOCK_ENTITY = BLOCK_ENTITIES.register("smoldering_or_burnt_campfire", () -> BlockEntityType.Builder.of(SmolderingOrBurntCampfireBlockEntity::new, SMOLDERING_CAMPFIRE_BLOCK.get(), BURNT_CAMPFIRE_BLOCK.get()).build(null));


    // --- Recipe Serializers ---
    public static final RegistryObject<RecipeSerializer<?>>
            EXTRACT_WATER_FROM_BAMBOO_RECIPE = RECIPE_SERIALIZERS.register("hcs_extract_water_from_bamboo", () -> new SimpleCraftingRecipeSerializer<>(ExtractWaterFromBambooRecipe::new)),
            EXTRACT_WATER_FROM_SNOW_RECIPE = RECIPE_SERIALIZERS.register("hcs_extract_water_from_snow", () -> new SimpleCraftingRecipeSerializer<>(ExtractWaterFromSnowRecipe::new)),
            PETALS_SALAD_RECIPE = RECIPE_SERIALIZERS.register("hcs_petals_salad", () -> new SimpleCraftingRecipeSerializer<>(PetalsSaladRecipe::new)),
            SPIKED_CLUB_RECIPE = RECIPE_SERIALIZERS.register("hcs_spiked_club_recipe", () -> new SimpleCraftingRecipeSerializer<>(SpikedClubRecipe::new)),
            COLD_WATER_BOTTLE_RECIPE = RECIPE_SERIALIZERS.register("hcs_cold_water_bottle_recipe", () -> new SimpleCraftingRecipeSerializer<>(ColdWaterBottleRecipe::new)),
            HOT_WATER_BOTTLE_RECIPE = RECIPE_SERIALIZERS.register("hcs_hot_water_bottle_recipe", () -> new SimpleCraftingRecipeSerializer<>(HotWaterBottleRecipe::new)),
            SAPLING_TO_STICK_RECIPE = RECIPE_SERIALIZERS.register("hcs_sapling_to_stick_recipe", () -> new SimpleCraftingRecipeSerializer<>(SaplingToStickRecipe::new)),
            POUR_OUT_CONTENT_RECIPE = RECIPE_SERIALIZERS.register("hcs_pour_out_content_recipe", () -> new SimpleCraftingRecipeSerializer<>(PourOutContentRecipe::new)),
            TORCH_IGNITE_RECIPE = RECIPE_SERIALIZERS.register("torch_ignite", () -> new SimpleCraftingRecipeSerializer<>(TorchIgniteRecipe::new));


    // GameRules
    public static final GameRules.Key<GameRules.IntegerValue> HCS_DIFFICULTY = GameRules.register("hcsDifficulty", GameRules.Category.PLAYER, GameRules.IntegerValue.create(1));

    public static final Predicate<Item> IS_BARK = item -> item == BARK.get() || item == WILLOW_BARK.get();

    // Creative Tab
    public static final RegistryObject<CreativeModeTab> HCS_TAB = CREATIVE_MODE_TABS.register(MOD_ID, () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("itemGroup.hcsurvival.main"))
            .icon(FLINT_HATCHET.get()::getDefaultInstance)
            .displayItems((parameters, output) -> {
                output.accept(GRASS_FIBER.get());
                output.accept(FIBER_STRING.get());
                output.accept(SHORT_STICK.get());
                output.accept(TINDER.get());
                output.accept(FIREWOOD.get());
                output.accept(FIRE_PLOUGH.get());
                output.accept(FIRE_BOW.get());
                output.accept(EXTINGUISHED_CAMPFIRE.get());
                output.accept(SMOLDERING_CAMPFIRE.get());
                output.accept(BURNT_CAMPFIRE.get());
                output.accept(ASHES.get());
                output.accept(ROCK.get());
                output.accept(SHARP_ROCK.get());
                output.accept(SHARP_FLINT.get());
                output.accept(RAW_COPPER_POWDER.get());
                output.accept(Items.WOODEN_SWORD);
                output.accept(SPIKED_CLUB.get());
                output.accept(SHARP_BROKEN_BONE.get());
                output.accept(STONE_CONE.get());
                output.accept(STONE_KNIFE.get());
                output.accept(STONE_SPEAR.get());
                output.accept(FLINT_CONE.get());
                output.accept(FLINT_KNIFE.get());
                output.accept(FLINT_SPEAR.get());
                output.accept(FLINT_HATCHET.get());
                output.accept(COPPER_SWORD.get());
                output.accept(COPPER_AXE.get());
                output.accept(COPPER_PICKAXE.get());
                output.accept(COPPER_HOE.get());
                output.accept(COPPER_SHOVEL.get());
                output.accept(COPPER_HELMET.get());
                output.accept(COPPER_CHESTPLATE.get());
                output.accept(COPPER_LEGGINGS.get());
                output.accept(COPPER_BOOTS.get());
                output.accept(WOOLEN_HOOD.get());
                output.accept(WOOLEN_COAT.get());
                output.accept(WOOLEN_TROUSERS.get());
                output.accept(WOOLEN_BOOTS.get());
                output.accept(WOODEN_HELMET.get());
                output.accept(WOODEN_CHESTPLATE.get());
                output.accept(WOODEN_LEGGINGS.get());
                output.accept(WOODEN_BOOTS.get());
                output.accept(GARLAND.get());
                output.accept(IMPROVISED_SHIELD.get());
                output.accept(HOT_WATER_BOTTLE.get().getDefaultInstance());
                output.accept(WORM.get());
                output.accept(ROASTED_WORM.get());
                output.accept(BAT_WINGS.get());
                output.accept(ROASTED_BAT_WINGS.get());
                output.accept(RAW_MEAT.get());
                output.accept(COOKED_MEAT.get());
                output.accept(ANIMAL_VISCERA.get());
                output.accept(COOKED_ANIMAL_VISCERA.get());
                output.accept(CACTUS_FLESH.get());
                output.accept(COOKED_CACTUS_FLESH.get());
                output.accept(BAMBOO_SHOOT.get());
                output.accept(COOKED_BAMBOO_SHOOT.get());
                output.accept(PUMPKIN_SLICE.get());
                output.accept(COOKED_PUMPKIN_SLICE.get());
                output.accept(ROASTED_SEEDS.get());
                output.accept(COOKED_CARROT.get());
                output.accept(COOKED_SWEET_BERRIES.get());
                output.accept(FRIED_EGG.get());
                output.accept(COOKED_KELP.get());
                output.accept(POTHERB.get());
                output.accept(ORANGE.get());
                output.accept(GINGER.get());
                output.accept(PETALS_SALAD.get());
                output.accept(JERKY.get());
                output.accept(SMALL_JERKY.get());
                output.accept(RAW_JERKY.get());
                output.accept(RAW_SMALL_JERKY.get());
                output.accept(ROT.get());
                output.accept(SELAGINELLA.get());
                output.accept(SPIDER_GLAND.get());
                output.accept(BERRY_BUSH.get());
                output.accept(BARK.get());
                output.accept(WILLOW_BARK.get());
                output.accept(FEARLESSNESS_HERB.get());
                output.accept(IMPROVISED_BANDAGE.get());
                output.accept(BANDAGE.get());
                output.accept(HEALING_SALVE.get());
                output.accept(SPLINT.get());
                output.accept(BOOSTER_SHOT.get());
                output.accept(PURIFIED_WATER_BOTTLE.get());
                output.accept(SALTWATER_BOTTLE.get());
                output.accept(COLD_WATER_BOTTLE.get());
                output.accept(CACTUS_JUICE.get());
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), IRONSKIN_POTION.get()));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), RETURN_POTION.get()));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), MINING_POTION.get()));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), CONSTANT_TEMPERATURE_POTION.get()));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), PAIN_KILLING_POTION.get()));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), FEARLESSNESS_POTION.get()));
                output.accept(ICEBOX_ITEM.get());
                output.accept(DRYING_RACK_ITEM.get());
                output.accept(CRUDE_TORCH_ITEM.get());
                output.accept(BURNING_CRUDE_TORCH_ITEM.get());
                output.accept(UNLIT_TORCH_ITEM.get());
                output.accept(GLOWSTONE_TORCH_ITEM.get());
            }).build());

    public Hcs(@NotNull FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        // Add listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        // Call sub-registers(facade)
        register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        // Config
        context.registerConfig(ModConfig.Type.COMMON, Config__.CFG_SPEC);
        // Networks
        ServerC2S.init();
    }

    public static void register(IEventBus bus) {
        Object dummy = HcsEffects.BLEEDING;
        MOB_EFFECTS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        POTIONS.register(bus);
        BLOCK_ENTITIES.register(bus);
        ENTITIES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        CREATIVE_MODE_TABS.register(bus);
    }

    private void commonSetup(final @NotNull FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            registerBrewing();
            registerCompostables();
        });
        String dummy = FOOD_SPOIL.gameRule.toString(); // must be called to ensure early initialization
    }

    private void registerBrewing() {
        registerBrewingRecipe(Potions.AWKWARD, Items.IRON_NUGGET, IRONSKIN_POTION.get());
        registerBrewingRecipe(Potions.AWKWARD, Items.SALMON, RETURN_POTION.get());
        registerBrewingRecipe(Potions.AWKWARD, Items.MANGROVE_PROPAGULE, MINING_POTION.get());
        registerBrewingRecipe(Potions.AWKWARD, Items.QUARTZ, CONSTANT_TEMPERATURE_POTION.get());
        registerBrewingRecipe(Potions.AWKWARD, WILLOW_BARK.get(), PAIN_KILLING_POTION.get());
        registerBrewingRecipe(Potions.AWKWARD, FEARLESSNESS_HERB.get(), FEARLESSNESS_POTION.get());
        registerBrewingRecipe(IRONSKIN_POTION.get(), Items.GLOWSTONE_DUST, STRONG_IRONSKIN_POTION.get());
        registerBrewingRecipe(IRONSKIN_POTION.get(), Items.REDSTONE, LONG_IRONSKIN_POTION.get());
        registerBrewingRecipe(MINING_POTION.get(), Items.GLOWSTONE_DUST, STRONG_MINING_POTION.get());
        registerBrewingRecipe(CONSTANT_TEMPERATURE_POTION.get(), Items.REDSTONE, LONG_CONSTANT_TEMPERATURE_POTION.get());
        registerBrewingRecipe(PAIN_KILLING_POTION.get(), Items.REDSTONE, LONG_PAIN_KILLING_POTION.get());
        registerBrewingRecipe(FEARLESSNESS_POTION.get(), Items.REDSTONE, LONG_FEARLESSNESS_POTION.get());
    }

    private void registerBrewingRecipe(Potion input, Item ingredient, Potion output) {
        BrewingRecipeRegistry.addRecipe(new BetterBrewingRecipe(input, ingredient, output));
    }

    private void registerCompostables() {
        ComposterBlock.add(0.3F, BERRY_BUSH.get());
        ComposterBlock.add(0.3F, ROASTED_SEEDS.get());
        ComposterBlock.add(0.3F, POTHERB.get());
        ComposterBlock.add(0.5F, PUMPKIN_SLICE.get());
        ComposterBlock.add(0.5F, COOKED_PUMPKIN_SLICE.get());
        ComposterBlock.add(0.5F, CACTUS_FLESH.get());
        ComposterBlock.add(0.5F, COOKED_CACTUS_FLESH.get());
        ComposterBlock.add(0.5F, BAMBOO_SHOOT.get());
        ComposterBlock.add(0.5F, COOKED_BAMBOO_SHOOT.get());
        ComposterBlock.add(0.5F, COOKED_CARROT.get());
        ComposterBlock.add(0.5F, COOKED_SWEET_BERRIES.get());
        ComposterBlock.add(0.65F, ORANGE.get());
        ComposterBlock.add(1.0F, ROT.get());
        ComposterBlock.add(0.5F, BARK.get());
        ComposterBlock.add(0.5F, WILLOW_BARK.get());
        ComposterBlock.add(0.5F, FEARLESSNESS_HERB.get());
    }

    @SubscribeEvent
    public void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        Item item = event.getItemStack().getItem();
        if (item == GRASS_FIBER.get()) event.setBurnTime(50);
        else if (item == FIBER_STRING.get()) event.setBurnTime(100);
        else if (item == SHORT_STICK.get()) event.setBurnTime(80);
        else if (item == FIREWOOD.get()) event.setBurnTime(300);
        else if (item == TINDER.get()) event.setBurnTime(30);
        else if (item == ROT.get()) event.setBurnTime(100);
        else if (item == BARK.get()) event.setBurnTime(100);
        else if (item == WILLOW_BARK.get()) event.setBurnTime(100);
        else if (item == BANDAGE.get()) event.setBurnTime(80);
        else if (item == IMPROVISED_BANDAGE.get()) event.setBurnTime(60);
        else if (item == SPLINT.get()) event.setBurnTime(100);
        else if (item == SPIKED_CLUB.get()) event.setBurnTime(160);
        else if (item == WOOLEN_HOOD.get() || item == WOODEN_HELMET.get()) event.setBurnTime(150);
        else if (item == WOOLEN_COAT.get() || item == WOODEN_CHESTPLATE.get()) event.setBurnTime(240);
        else if (item == WOOLEN_TROUSERS.get() || item == WOODEN_LEGGINGS.get()) event.setBurnTime(210);
        else if (item == WOOLEN_BOOTS.get() || item == WOODEN_BOOTS.get()) event.setBurnTime(120);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("village").executes(context -> {
            context.getSource().sendSuccess(() -> Component.translatable(WorldHelper.shouldGenerateVillages() ? "hcs.tip.can_gen_village" : "hcs.tip.cant_gen_village"), false);
            return 1;
        }));
    }

    private void addCreative(@NotNull BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }

    // Loggers
    public static void info(Object... contents) {
        LOGGER.info(getInfoString(contents));
    }

    public static void warn(Object... contents) {
        LOGGER.warn(getInfoString(contents));
    }

    public static void error(Object... contents) {
        LOGGER.error(getInfoString(contents));
    }

    private static @NotNull String getInfoString(@Nullable Object... contents) {
        if (contents == null) {
            LOGGER.warn("Printed content is null");
            return "";
        }
        StringBuilder outputBuilder = new StringBuilder();
        for (Object content : contents) {
            outputBuilder.append(content);
            outputBuilder.append(" ");
        }
        return outputBuilder.toString();
    }

}