package biz.coolpage.hcs.client.mixin.gui;

import biz.coolpage.hcs.Hcs; // biz.coolpage.hcs.Reg 类名改为 biz.coolpage.hcs.Hcs
import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.SanityManager;
import biz.coolpage.hcs.status.manager.StatusManager;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import biz.coolpage.hcs.util.ArmorHelper;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.HcsFactory;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting; // Formatting -> ChatFormatting
import net.minecraft.client.Minecraft; // MinecraftClient -> Minecraft
import net.minecraft.client.gui.Font; // TextRenderer -> Font
import net.minecraft.client.gui.Gui; // InGameHud -> Gui
import net.minecraft.client.gui.GuiGraphics; // DrawContext -> GuiGraphics
import net.minecraft.resources.ResourceLocation; // Identifier -> ResourceLocation
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth; // MathHelper -> Mth
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping; // JumpingMount -> PlayerRideableJumping
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player; // PlayerEntity -> Player
import net.minecraft.world.food.FoodData; // HungerManager -> FoodData
import net.minecraft.world.effect.MobEffects; // StatusEffects -> MobEffects
import net.minecraft.network.chat.Component; // Text -> Component
import net.minecraft.network.chat.MutableComponent; // MutableText -> MutableComponent
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static biz.coolpage.hcs.config.Configs.*;
import static biz.coolpage.hcs.util.CommUtil.applyNullable;
import static biz.coolpage.hcs.util.CommUtil.numFormat;

@Mixin(Gui.class)
public abstract class InGameHudMixin {
    @Final
    @Shadow
    private Minecraft minecraft; // client -> minecraft
    @Shadow
    private int screenHeight, screenWidth, tickCount; // scaledHeight -> screenHeight, scaledWidth -> screenWidth, ticks -> tickCount

    @Shadow
    protected abstract Player getCameraPlayer();

    @Shadow
    protected abstract int getVehicleMaxHearts(LivingEntity entity); // getHeartCount -> getVehicleMaxHearts

    @Shadow
    public abstract Font getFont(); // getTextRenderer -> getFont

    @Shadow
    @Final
    private static ResourceLocation POWDER_SNOW_OUTLINE_LOCATION; // POWDER_SNOW_OUTLINE -> POWDER_SNOW_OUTLINE_LOCATION

    @Shadow
    protected abstract void renderTextureOverlay(GuiGraphics context, ResourceLocation texture, float opacity); // renderOverlay -> renderTextureOverlay

    @Shadow protected abstract LivingEntity getPlayerVehicleWithHealth();

    @Unique
    private float heaLast = 0.0F;
    @Unique // For Status Icons Automatic Centering
    private final Map<String, Boolean> renderEntries = new HashMap<>();
    @Unique
    private Boolean shouldRenderMountHealth = false, shouldRenderMountJumpBar = false;
    @Unique
    private int renderExperienceBarX;
    @Unique
    private int heaTwinkleCoolDown = 0, sanTwinkleCoolDown = 0;
    @Unique
    private static final ResourceLocation HCS_ICONS_TEXTURE = HcsFactory.createResourceLocation( "textures/gui/hcs_stat.png");
    @Unique
    private static final ResourceLocation EMPTY_TEXTURE = HcsFactory.createResourceLocation( "textures/gui/empty.png");
    @Unique
    private static final ResourceLocation HEATSTROKE_BLUR = HcsFactory.createResourceLocation( "textures/misc/heatstroke_blur.png");
    @Unique
    private static final ResourceLocation INSANITY_OUTLINE = HcsFactory.createResourceLocation( "textures/misc/insanity_outline.png");
    @Unique
    private static final ResourceLocation DARKNESS = HcsFactory.createResourceLocation( "textures/misc/darkness.png");
    @Unique
    private static final ResourceLocation DARKNESS_JUMP_SCARE = HcsFactory.createResourceLocation( "textures/misc/darkness_jump_scare.png");
    @Unique
    private static final ResourceLocation HURT_BLUR = HcsFactory.createResourceLocation( "textures/misc/hurt_blur.png");
    @Unique
    private static final ResourceLocation FAINT_BLUR = HcsFactory.createResourceLocation( "textures/misc/panic_blur.png");

    @Unique
    public void drawHCSTexture(@NotNull GuiGraphics ctx, int x, int y, int u, int v, int width, int height) {
        // (u, v) is the coordinate of texture
        RenderSystem.setShaderTexture(0, HCS_ICONS_TEXTURE);
        ctx.blit(HCS_ICONS_TEXTURE, x, y, 0, u, v, width, height, 256, 256);
        RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);
    }

    @Unique
    @Deprecated
    public void drawHCSTexture(@NotNull GuiGraphics ctx, int x, int y, int u, int v, int width, int height, float scale) {
        float descale = 1 / scale;
        x = (int) (x * descale);
        y = (int) (y * descale);
        ctx.pose().scale(scale, scale, scale);
        this.drawHCSTexture(ctx, x, y, u, v, width, height);
        ctx.pose().scale(descale, descale, descale);//reset to default scale
    }

    @Unique
    public void drawTextWithThickShadow(@NotNull GuiGraphics ctx, String text, int x, int y, int color, float scale) {
        Font renderer = this.getFont();
        float descale = 1 / scale;
        ctx.pose().scale(scale, scale, scale);
        x = (int) (x * descale);
        y = (int) (y * descale);
        // TODO test here if redundant or unsuitable
        ctx.drawString(renderer, text, x + 1, y, 0, false);
        ctx.drawString(renderer, text, x - 1, y, 0, false);
        ctx.drawString(renderer, text, x, (y + 1), 0, false);
        ctx.drawString(renderer, text, x, (y - 1), 0, false);
        ctx.drawString(renderer, text, x, y, color, false);
        ctx.pose().scale(descale, descale, descale);
    }

    @Unique
    public int getDrawIconHeight(float val) {
        int result = Math.round(val * 14) + 1;//+2
        if ((val <= 0 && result >= 2) || result < 0) result = 0;
        else if (result > 16) result = 16;
        return result;
    }

    @Unique
    public int getDrawIconHeight(double val, int initAdd, int maxCut) {
        int result = Math.round((float) val * ((14 - maxCut) - initAdd)) + initAdd;
        if ((val <= 0 && result >= 2) || result < 0) result = 0;
        else if (result > 16) result = 16;
        return result;
    }

    @Unique
    public int getColorByPercentage(double val) {
        int r, g;
        String R, G;
        if (val > 1) val = 1;
        else if (val < 0) val = 0;
        if (val > 0.5) {
            r = Mth.clamp((int) (((1 - val) * 2) * 255), 0, 255);
            g = 255;
        } else {
            r = 255;
            g = Mth.clamp((int) ((val * 2) * 255), 0, 255);
        }
        R = Integer.toHexString(r);
        G = Integer.toHexString(g);
        if (R.length() < 2) R = "0" + R;
        if (G.length() < 2) G = "0" + G;
        return Integer.parseInt(R + G + "00", 16);
    }

    @Unique
    public double getTempForDisplay(double x) {
        if (x <= 0.5F) return 0.5 - Math.pow(0.5 - x, 1.6) * 1.5;
        return Math.pow(x - 0.5, 1.6) * 1.5 + 0.5;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getTicksFrozen()I"))
    public void render(@NotNull GuiGraphics context, float tickDelta, CallbackInfo ci) {
        if (this.minecraft != null && this.minecraft.player != null && this.minecraft.level != null) {
            double panic = ((StatAccessor) this.minecraft.player).getMoodManager().getRealPanic();
            StatusManager statusManager = ((StatAccessor) this.minecraft.player).getStatusManager();
            if (this.minecraft.player.hasEffect(HcsEffects.INSANITY)) {
                double san = ((StatAccessor) this.minecraft.player).getSanityManager().get();
                this.renderTextureOverlay(context, INSANITY_OUTLINE, Math.min(1.0F, Math.max(0.3F, 1.0F - (float) san / 0.3F + 0.3F * (1.0F - (float) san / 0.6F) * Mth.sin((float) this.tickCount * (float) Math.PI / 15.0f))));
            }
            TemperatureManager temperatureManager = ((StatAccessor) this.minecraft.player).getTemperatureManager();
            double temp = temperatureManager.get();
            float tempOpacity = Math.min(1.0F, 0.2F + temperatureManager.getSaturationPercentage());
            if (temp >= 1.0F && this.minecraft.player.hasEffect(HcsEffects.HEATSTROKE))
                this.renderTextureOverlay(context, HEATSTROKE_BLUR, tempOpacity);
            else if (temp <= 0.0F && this.minecraft.player.getTicksFrozen() <= 0 && this.minecraft.player.hasEffect(HcsEffects.HYPOTHERMIA))
                this.renderTextureOverlay(context, POWDER_SNOW_OUTLINE_LOCATION, tempOpacity);
            int inDarkTicks = statusManager.getInDarknessTicks();
            if (inDarkTicks > 720 && inDarkTicks < 740 && HcsDifficulty.isOf(this.minecraft.player, HcsDifficulty.HcsDifficultyEnum.challenging))
                this.renderTextureOverlay(context, DARKNESS_JUMP_SCARE, 0.9F);
            else if (inDarkTicks > 60)
                this.renderTextureOverlay(context, DARKNESS, Mth.clamp((inDarkTicks - 60) / 550.0F, 0.0F, 1.0F));
            if (statusManager.getRecentHurtTicks() > 0) {
                int x = Mth.clamp(20 - statusManager.getRecentHurtTicks(), 0, 20);
                float opa = Mth.clamp((float) (x < 5 ? Math.sin(Math.PI / 10 * x) : Math.sin(Math.PI / 30 * (x + 10))), 0.0F, 1.0F);
                opa *= Mth.clamp(statusManager.getRecentFeelingDamage() / 12.0F, 0.0F, 0.8F);
                this.renderTextureOverlay(context, HURT_BLUR, opa);
            }
            if (EntityHelper.getEffectAmplifier(this.minecraft.player, HcsEffects.DEHYDRATED) >= 0 || EntityHelper.getEffectAmplifier(this.minecraft.player, HcsEffects.STARVING) >= 0 || EntityHelper.getEffectAmplifier(this.minecraft.player, HcsEffects.PAIN) > 1) {
                float opacity = (float) (0.5 * (Mth.sin((float) Math.PI / 40 * this.tickCount) + 1));
                double hunger = applyNullable(this.minecraft.player.getFoodData(), hm -> hm.getFoodLevel() / 20.0, 1.0), thirst = ((StatAccessor) this.minecraft.player).getThirstManager().get(), pain = ((StatAccessor) this.minecraft.player).getInjuryManager().getRealPain();
                float multiplier = (float) Mth.clamp((0.3 - Mth.clamp(Math.min(hunger, thirst), 0.0, 0.3)) / 0.3 + (pain - 1.0) / 7, 0.0, 0.5);
                if (thirst < 0.2 || hunger < 0.2 || pain > 2.0)
                    this.renderTextureOverlay(context, DARKNESS, opacity * multiplier);
            }
            if (EntityHelper.getEffectAmplifier(this.minecraft.player, HcsEffects.PANIC) >= 1)
                this.renderTextureOverlay(context, FAINT_BLUR, Mth.clamp((float) ((panic - 2.0) / 5.0), 0.0F, 1.0F));
        }
    }

    @Deprecated
    @SuppressWarnings("all")
//    @Inject(method = "renderPlayerHealth", at = @At("HEAD"))
    private void renderStatusBarsHead0(GuiGraphics context, CallbackInfo ci) {
        // Disable original rendering without using ci.cancel() or overwrite
//        RenderSystem.setShaderTexture(0, EMPTY_TEXTURE);
        // this.minecraft.getProfiler().pop();
    }

    @Unique
    private static ResourceLocation disableVanillaHUDByModArgs(ResourceLocation texture) {
        if (Objects.equals(Gui.GUI_ICONS_LOCATION, texture)) return EMPTY_TEXTURE;
        return texture;
    }

    @ModifyArg(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"), index = 0)
    private ResourceLocation renderStatusBarsModArg0(ResourceLocation texture) {
        return disableVanillaHUDByModArgs(texture);
    }

    @ModifyArg(method = "renderHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"), index = 0)
    private ResourceLocation renderStatusBarsModArg1(ResourceLocation texture) {
        return disableVanillaHUDByModArgs(texture);
    }

    @Inject(method = "renderPlayerHealth", at = @At("TAIL"))
    private void renderStatusBarsTail(@NotNull GuiGraphics ctx, CallbackInfo ci) {
        Player player = this.getCameraPlayer();
        if (player == null || this.minecraft.player == null) return;
        var config = ((StatAccessor) player).getConfigManager();
        LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
        int xx = this.screenWidth / 2;
        int yy = this.screenHeight - 46;
        int yyy = yy + 2;
        // Automatic Centering for Icons
        for (Boolean displace : renderEntries.values()) {
            if (displace) xx -= 10;
        }
        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        // EXP BAR
        if (shouldRenderMountJumpBar) {
            float f = this.minecraft.player.getJumpRidingScale();
            int j = (int) (f * 183.0f);
            int k = this.screenHeight - 32 + 3;
            ctx.blit(Gui.GUI_ICONS_LOCATION, renderExperienceBarX, k, 0, 84, 182, 5);
            if (j > 0) {
                ctx.blit(Gui.GUI_ICONS_LOCATION, renderExperienceBarX, k, 0, 89, j, 5);
            }
        } else {
            int l;
            int k;
            assert this.minecraft.player != null;
            int i = this.minecraft.player.getXpNeededForNextLevel();
            if (i > 0) {
                k = (int) (this.minecraft.player.experienceProgress * 183.0f);
                l = this.screenHeight - 32 + 3;
                ctx.blit(Gui.GUI_ICONS_LOCATION, renderExperienceBarX, l, 0, 64, 182, 5);
                if (k > 0) {
                    ctx.blit(Gui.GUI_ICONS_LOCATION, renderExperienceBarX, l, 0, 69, k, 5);
                }
            }
            if (this.minecraft.player.experienceLevel > 0) {
                String string = String.valueOf(this.minecraft.player.experienceLevel);
                k = (this.screenWidth - this.getFont().width(string)) / 2;
                l = this.screenHeight - 31 + 4;
                this.drawTextWithThickShadow(ctx, string, k, l, 8453920, 1F);
            }
        }
        // ARMOR
        float arm = ArmorHelper.getFinalProtection(player);
        float armPercentage = arm / 20;
        if (arm > 0) {
            renderEntries.put("armor", true);
            int armHeight = this.getDrawIconHeight(armPercentage);
            this.drawHCSTexture(ctx, xx, yy, 0, 32, 16, 16);
            this.drawHCSTexture(ctx, xx, yy + 16 - armHeight, 16, 48 - armHeight, 16, armHeight);
            this.drawTextWithThickShadow(ctx, String.format("%.1f", arm), arm < 10.0F ? (xx + 4) : (xx + 1), yyy + 11, getColorByPercentage(armPercentage), 0.75F);
            xx += 20;
        } else renderEntries.put("armor", false);
        // HEALTH
        renderEntries.put("health", true);
        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
        double tem = temperatureManager.get();
        float hea = player.getHealth();
        float heaMax = player.getMaxHealth();
        float heaPercentage = hea / heaMax;
        int heaHeight = this.getDrawIconHeight((float) Math.pow(heaPercentage, 0.8D));
        int heaDeviation = 0;
        int heaShake = 0;
        float heaAbsorption = player.getAbsorptionAmount();
        if (Mth.floor(hea) - Mth.floor(heaLast) != 0 || heaLast > hea) heaTwinkleCoolDown = 5;
        boolean heaTwinkle = heaTwinkleCoolDown > 0;
        if (hea <= 4 && this.tickCount % 3 == 0) heaShake = Math.round((float) Math.random() * 2) - 1;
        if (player.hasEffect(MobEffects.POISON)) {
            heaDeviation = 16;
            if (hea > 0 && hea < 1) heaHeight = 2;
        } else if (player.hasEffect(MobEffects.WITHER)) heaDeviation = 32;
        else if (player.hasEffect(MobEffects.ABSORPTION)) heaDeviation = 48;
        else if (player.isFullyFrozen()) heaDeviation = 80;
        else if (tem >= 1.0F) heaDeviation = 64;
        this.drawHCSTexture(ctx, xx, yy + heaShake, heaTwinkle ? 16 : 0, 0, 16, 16);
        if (heaTwinkle) --heaTwinkleCoolDown;
        this.drawHCSTexture(ctx, xx, yy + (16 - heaHeight) + heaShake, 32 + heaDeviation, 16 - heaHeight, 16, heaHeight);
        if (player.level().getLevelData().isHardcore())
            this.drawHCSTexture(ctx, xx, yy + (16 - heaHeight) + heaShake, heaDeviation > 0 ? 144 : 128, 16 - heaHeight, 16, heaHeight);
        this.drawTextWithThickShadow(ctx, String.format("%.1f", hea > 0 ? Math.max(hea, 0.1F) : Math.max(hea, 0.0F)), hea < 10.0F ? xx + 2 : xx, yyy + 11, getColorByPercentage(heaPercentage), 0.75F);
        this.drawTextWithThickShadow(ctx, (heaAbsorption >= 1.0F ? "+" + String.format("%.1f", heaAbsorption) : "") + "/" + String.format("%.1f", heaMax), xx, yyy + 17, getColorByPercentage(heaPercentage), 0.5F);
        // STAMINA
        if (config.get(STAMINA)) {
            renderEntries.put("stamina", true);
            double str = ((StatAccessor) player).getStaminaManager().get();
            xx += 20;
            int strHeight = this.getDrawIconHeight((float) Math.pow(str, 0.8D));
            int strDeviation = 0, strShake = 0;
            if (this.tickCount % (Math.round(str * 20) + 1) == 0 && str < 0.3F)
                strShake = Math.round((float) Math.random() * 2) - 1;
            this.drawHCSTexture(ctx, xx, yy + strShake, 0, 112, 16, 16);
            this.drawHCSTexture(ctx, xx, yy + (16 - strHeight) + strShake, 16 + strDeviation, 128 - strHeight, 16, strHeight);
            this.drawTextWithThickShadow(ctx, numFormat(str < 0.1 ? " #%" : "##%", str), xx + 2, yyy + 11, getColorByPercentage(str), 0.75F);
        } else renderEntries.put("stamina", false);
        // THIRST
        if (config.get(THIRST)) {
            renderEntries.put("thirst", true);
            xx += 20;
            double thi = ((StatAccessor) player).getThirstManager().get();
            int thiHeight = this.getDrawIconHeight(thi, 1, 1);
            if (thiHeight < 0) thiHeight = 0;
            else if (thi > 0.05F && thiHeight <= 1) thiHeight = 2;
            int thiDeviation = 0, thiShake = 0;
            if (this.tickCount % (Math.round(thi * 20) * 3 + 1) == 0 && thi < 0.3F)
                thiShake = Math.round((float) Math.random() * 2) - 1;
            if (player.hasEffect(HcsEffects.THIRST) || player.hasEffect(HcsEffects.DIARRHEA) || player.hasEffect(HcsEffects.PARASITE_INFECTION) || player.hasEffect(HcsEffects.FOOD_POISONING))
                thiDeviation = 16;
            this.drawHCSTexture(ctx, xx, yy + thiShake, 0, 48, 16, 16);
            this.drawHCSTexture(ctx, xx, yy + (16 - thiHeight) + thiShake, 16 + thiDeviation, 64 - thiHeight, 16, thiHeight);
            this.drawTextWithThickShadow(ctx, numFormat(thi < 0.1 ? " #%" : "##%", thi), xx + 2, yyy + 11, getColorByPercentage(thi), 0.75F);
        } else renderEntries.put("thirst", false);
        // HUNGER
        renderEntries.put("hunger", true);
        xx += 20;
        FoodData hunManager = player.getFoodData();
        float hun = (float) hunManager.getFoodLevel();
        float hunSaturation = hunManager.getSaturationLevel();
        float hunExhaustion = (hunSaturation > 0 || statusManager.hasDecimalFoodLevel()) ? 0.0F : statusManager.getExhaustion();
        float hunPercentage = (hun - hunExhaustion / 4.0F) / 20.0F;
        if (hunPercentage < 0.0F) hunPercentage = 0.0F;
        else if (hunPercentage > 1.0F) hunPercentage = 1.0F;
        int hunHeight = this.getDrawIconHeight(hunPercentage * 1.1F, 4, 0);
        int hunDeviation = 0, hunShake = 0;
        if (hunSaturation <= 0.0F && this.tickCount % (hun * 3 + 1) == 0)
            hunShake = Math.round((float) Math.random() * 2) - 1;
        if (player.hasEffect(MobEffects.HUNGER) || player.hasEffect(HcsEffects.DIARRHEA) || player.hasEffect(HcsEffects.PARASITE_INFECTION) || player.hasEffect(HcsEffects.FOOD_POISONING))
            hunDeviation = 16;
        this.drawHCSTexture(ctx, xx, yy + hunShake, 0, 16, 16, 16);
        this.drawHCSTexture(ctx, xx, yy + (16 - hunHeight) + hunShake, 16 + hunDeviation, 32 - hunHeight, 16, hunHeight);
        this.drawTextWithThickShadow(ctx, numFormat(hunPercentage < 0.1 ? " #%" : "##%", hunPercentage), xx + 2, yyy + 11, getColorByPercentage(hunPercentage), 0.75F);
        // SANITY
        if (config.get(SANITY)) {
            renderEntries.put("sanity", true);
            xx += 20;
            SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
            double san = sanityManager.get();
            double sanDifference = sanityManager.getDifference(), sanDifferenceAbs = Math.abs(sanDifference);
            if (san > 1.0F) san = 1.0F;
            else if (san < 0.0F) san = 0.0F;
            int sanHeight = this.getDrawIconHeight((float) Math.pow(san, 0.6D));
            int sanDeviation = 0, sanShake = 0;
            if (this.tickCount % (Math.round(san * 20) * 3 + 1) == 0 && san < 0.3F)
                sanShake = Math.round((float) Math.random() * 2) - 1;
            if (sanDifferenceAbs > 0.0049F) sanTwinkleCoolDown = 5;
            boolean sanTwinkle = sanTwinkleCoolDown > 0;
            if (sanTwinkle) --sanTwinkleCoolDown;
            this.drawHCSTexture(ctx, xx, yy + sanShake, sanTwinkle ? 16 : 0, 80, 16, 16);
            this.drawHCSTexture(ctx, xx, yy + (16 - sanHeight) + sanShake, 32 + sanDeviation, 96 - sanHeight, 16, sanHeight);
            if (sanDifferenceAbs > 0.0F && sanTwinkleCoolDown < 5) {
                int devi, shakeInterval = 24;
                if (sanDifference < -0.000079F) {
                    devi = 96;
                    shakeInterval = 6;
                } else if (sanDifference < -0.000039F) {
                    devi = 80;
                    shakeInterval = 12;
                } else if (sanDifference < 0.0F) devi = 64;
                else if (sanDifference < 0.000039F) devi = 112;
                else if (sanDifference < 0.000079F) {
                    devi = 128;
                    shakeInterval = 12;
                } else {
                    devi = 144;
                    shakeInterval = 6;
                }
                this.drawHCSTexture(ctx, xx, yy + (((this.tickCount % (shakeInterval * 2)) < shakeInterval) ? 1 : 0), devi, 80, 16, 16);
            }
            this.drawTextWithThickShadow(ctx, numFormat(san < 0.1F ? " #%" : "##%", san), xx + 2, yyy + 11, getColorByPercentage(san), 0.75F);
        } else renderEntries.put("sanity", false);
        // TEMPERATURE
        if (config.get(TEMPERATURE)) {
            renderEntries.put("temperature", true);
            xx += 20;
            float temSaturationPercentage = temperatureManager.getSaturationPercentage();
            int temShake = 0;
            if (this.tickCount % 3 == 0) temShake = Math.round((float) Math.random() * 2) - 1;
            int temHeight = this.getDrawIconHeight(temSaturationPercentage);
            if (tem <= 0.0F) {
                this.drawHCSTexture(ctx, xx, yy + temShake, 0, 64, 16, 16);
                this.drawHCSTexture(ctx, xx, yy + (16 - temHeight) + temShake, 16, 80 - temHeight, 16, temHeight);
            } else if (tem >= 1.0F) {
                this.drawHCSTexture(ctx, xx + temShake, yy, 208, 64, 16, 16);
                this.drawHCSTexture(ctx, xx, yy + (16 - temHeight) + temShake, 224, 80 - temHeight, 16, temHeight);
            } else {
                int temDeviation = (int) Math.floor(getTempForDisplay(tem) * 12) * 16;
                if (temDeviation <= 0) temDeviation = 16;
                else if (temDeviation > 176) temDeviation = 176;
                this.drawHCSTexture(ctx, xx, yy, 16 + temDeviation, 64, 16, 16);
            }
        }
        // AIR
        int air = player.getAirSupply();
        if (air < 0) air = 0;
        int airMax = player.getMaxAirSupply();
        if (player.isEyeInFluid(FluidTags.WATER) || air < airMax) {
            renderEntries.put("air", true);
            xx += 20;
            int airShake = 0;
            float airPercentage = (float) air / airMax;
            if (air <= (airMax / 3) && this.tickCount % 3 == 0) airShake = Math.round((float) Math.random() * 2) - 1;
            this.drawHCSTexture(ctx, xx, yy + airShake, 32, 32, 16, 16);
            this.drawTextWithThickShadow(ctx, numFormat(airPercentage < 0.1 ? " #%" : "##%", airPercentage), xx + 2, yyy + 11, getColorByPercentage(airPercentage), 0.75F);
        } else renderEntries.put("air", false);
        // MOUNT HEALTH
        if (shouldRenderMountHealth && livingEntity != null) {
            renderEntries.put("mount", true);
            xx += 20;
            float mou = livingEntity.getHealth();
            float mouMax = livingEntity.getMaxHealth();
            float mouPercentage = mou / mouMax;
            int mouHeight = getDrawIconHeight(mouPercentage);
            this.drawHCSTexture(ctx, xx, yy, 0, 0, 16, 16);
            this.drawHCSTexture(ctx, xx, yy + (16 - mouHeight), 48, 48 - mouHeight, 16, mouHeight);
            this.drawTextWithThickShadow(ctx, String.format("%.1f", mou > 0 ? Math.max(mou, 0.1F) : Math.max(mou, 0.0F)), xx, yyy + 11, getColorByPercentage(mouPercentage), 0.75F);
            this.drawTextWithThickShadow(ctx, "/" + String.format("%.1f", mouMax), xx, yyy + 17, getColorByPercentage(mouPercentage), 0.5F);
        } else renderEntries.put("mount", false);

        heaLast = hea;
        shouldRenderMountHealth = shouldRenderMountJumpBar = false;
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void renderExperienceBar(GuiGraphics context, int x, @NotNull CallbackInfo ci) {
        this.minecraft.getProfiler().push("expBar");
        this.minecraft.getProfiler().push("expLevel");
        renderExperienceBarX = x;
        ci.cancel();
    }

    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    private void renderMountHealth(GuiGraphics context, CallbackInfo ci) {
        LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
        if (livingEntity == null) {
            shouldRenderMountHealth = false;
            return;
        }
        int i = this.getVehicleMaxHearts(livingEntity);
        if (i == 0) {
            shouldRenderMountHealth = false;
            return;
        }
        shouldRenderMountHealth = true;
        this.minecraft.getProfiler().popPush("mountHealth");
        ci.cancel();
    }

    @Inject(method = "renderJumpMeter", at = @At("HEAD"), cancellable = true)
    public void renderMountJumpBar(PlayerRideableJumping mount, GuiGraphics context, int x, @NotNull CallbackInfo ci) {
        this.minecraft.getProfiler().push("jumpBar");
        shouldRenderMountJumpBar = true;
        renderExperienceBarX = x;
        ci.cancel();
    }

    @ModifyArg(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"), index = 1)
    public Component renderHeldItemTooltipModArg1(Component text) {
        if (text != null && this.minecraft.player != null && ((StatAccessor) this.minecraft.player).getSanityManager().get() < 0.1 && this.minecraft.player.hasEffect(HcsEffects.INSANITY))
            return MutableComponent.create(text.getContents()).withStyle(ChatFormatting.OBFUSCATED);
        return text;
    }

}