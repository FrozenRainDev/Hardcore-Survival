package com.hcs.mixin.client.gui;

import com.hcs.main.manager.SanityManager;
import com.hcs.main.manager.StatusManager;
import com.hcs.main.manager.TemperatureManager;
import com.hcs.misc.HcsEffects;
import com.hcs.misc.accessor.StatAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

import static com.hcs.main.helper.EntityHelper.customNumberFormatter;


@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {
    @Final
    @Shadow
    private MinecraftClient client;
    @Shadow
    private int scaledHeight, scaledWidth, ticks;

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Shadow
    protected abstract LivingEntity getRiddenEntity();

    @Shadow
    protected abstract int getHeartCount(LivingEntity entity);

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Shadow
    private void renderOverlay(MatrixStack matrices, Identifier texture, float opacity) {
    }

    @Shadow
    @Final
    private static Identifier POWDER_SNOW_OUTLINE;
    private float heaLast = 0.0F;
    private final Map<String, Boolean> displacement = new HashMap<>();
    private Boolean shouldRenderMountHealth = false, shouldRenderMountJumpBar = false;
    private int renderExperienceBarX;
    private int heaTwinkleCoolDown = 0, sanTwinkleCoolDown = 0;
    private static final Identifier HCS_ICONS_TEXTURE = new Identifier("hcs", "textures/gui/hcs_stat.png");
    private static final Identifier EMPTY_TEXTURE = new Identifier("hcs", "textures/gui/empty.png");
    private static final Identifier HEATSTROKE_BLUR = new Identifier("hcs", "textures/misc/heatstroke_blur.png");
    private static final Identifier INSANITY_OUTLINE = new Identifier("hcs", "textures/misc/insanity_outline.png");

    public void drawHCSTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        //(u,v) is the coordinate of texture
        RenderSystem.setShaderTexture(0, HCS_ICONS_TEXTURE);
        DrawableHelper.drawTexture(matrices, x, y, 0, u, v, width, height, 256, 256);
        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
    }

    @Deprecated
    public void drawHCSTexture(@NotNull MatrixStack matrices, int x, int y, int u, int v, int width, int height, float scale) {
        float descale = 1 / scale;
        x *= descale;
        y *= descale;
        matrices.scale(scale, scale, scale);
        this.drawHCSTexture(matrices, x, y, u, v, width, height);
        matrices.scale(descale, descale, descale);//reset to default scale
    }

    public void drawTextWithThickShadow(@NotNull MatrixStack matrices, String text, int x, int y, int color, float scale) {
        // 1/(1/0.75)==0.75
        TextRenderer renderer = this.getTextRenderer();
        float descale = 1 / scale;
        matrices.scale(scale, scale, scale);
        x *= descale;
        y *= descale;
        renderer.draw(matrices, text, (float) (x + 1), (float) y, 0);
        renderer.draw(matrices, text, (float) (x - 1), (float) y, 0);
        renderer.draw(matrices, text, (float) x, (float) (y + 1), 0);
        renderer.draw(matrices, text, (float) x, (float) (y - 1), 0);
        renderer.draw(matrices, text, (float) x, (float) y, color);
        matrices.scale(descale, descale, descale);
    }

    public int getDrawIconHeight(float val) {
        int result = Math.round(val * 14) + 1;//+2
        if ((val <= 0 && result >= 2) || result < 0) result = 0;
        else if (result > 16) result = 16;
        return result;
    }

    public int getDrawIconHeight(float val, int initAdd, int maxCut) {
        int result = Math.round(val * ((14 - maxCut) - initAdd)) + initAdd;
        if ((val <= 0 && result >= 2) || result < 0) result = 0;
        else if (result > 16) result = 16;
        return result;
    }

    public int getColorByPercentage(float val) {
        int r, g;
        String R, G;
        if (val > 1) val = 1;
        else if (val < 0) val = 0;
        if (val > 0.5) {
            r = (int) (((1 - val) * 2) * 255);
            g = 255;
        } else {
            r = 255;
            g = (int) ((val * 2) * 255);
        }
        R = Integer.toHexString(r);
        G = Integer.toHexString(g);
        if (R.length() < 2) R = "0" + R;
        if (G.length() < 2) G = "0" + G;
        return Integer.parseInt(R + G + "00", 16);
    }

    public double getTempForDisplay(float x) {
        if (x <= 0.5F) return 0.5 - Math.pow(0.5 - x, 1.6) * 1.5;
        return Math.pow(x - 0.5, 1.6) * 1.5 + 0.5;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFrozenTicks()I"))
    public void render(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (this.client.player != null) {
            float san = ((StatAccessor) this.client.player).getSanityManager().get();
            if (san < 0.3F) this.renderOverlay(matrices, INSANITY_OUTLINE, 1.0F - san / 0.3F);
            TemperatureManager temperatureManager = ((StatAccessor) this.client.player).getTemperatureManager();
            float temp = temperatureManager.get();
            float opacity = Math.min(1.0F, 0.2F + temperatureManager.getSaturationPercentage());
            if (!getCameraPlayer().getAbilities().invulnerable) {
                if (temp >= 1.0F) this.renderOverlay(matrices, HEATSTROKE_BLUR, opacity);
                else if (temp <= 0.0F && this.client.player.getFrozenTicks() <= 0)
                    this.renderOverlay(matrices, POWDER_SNOW_OUTLINE, opacity);
            }
        }
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void renderStatusBarsHead(MatrixStack matrices, CallbackInfo ci) {
        //Disable original rendering without using ci.cancel() or overwrite
        RenderSystem.setShaderTexture(0, EMPTY_TEXTURE);
        //this.client.getProfiler().pop();//used for 1.19.3 debug
    }

    @Inject(method = "renderStatusBars", at = @At("TAIL"))
    private void renderStatusBarsTail(MatrixStack matrices, CallbackInfo ci) {
        PlayerEntity player = this.getCameraPlayer();
        if (player == null || this.client.player == null) return;
        LivingEntity livingEntity = this.getRiddenEntity();
        int xx = this.scaledWidth / 2;//-91
        int yy = this.scaledHeight - 46;
        int yyy = yy + 2;
        //Automatic Centering for Icons
        for (Boolean displace : displacement.values()) {
            if (displace.equals(true)) xx -= 10;
        }
        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        //EXP BAR
        if (shouldRenderMountJumpBar) {
            //this.client.getProfiler().push("jumpBar");
            RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
            float f = this.client.player.getMountJumpStrength();
            int j = (int) (f * 183.0f);
            int k = this.scaledHeight - 32 + 3;
            InGameHud.drawTexture(matrices, renderExperienceBarX, k, 0, 84, 182, 5);
            if (j > 0) {
                InGameHud.drawTexture(matrices, renderExperienceBarX, k, 0, 89, j, 5);
            }
            //this.client.getProfiler().pop();
        } else {
            int l;
            int k;
            //this.client.getProfiler().push("expBar");
            RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
            assert this.client.player != null;
            int i = this.client.player.getNextLevelExperience();
            if (i > 0) {
                k = (int) (this.client.player.experienceProgress * 183.0f);
                l = this.scaledHeight - 32 + 3;
                InGameHud.drawTexture(matrices, renderExperienceBarX, l, 0, 64, 182, 5);
                if (k > 0) {
                    InGameHud.drawTexture(matrices, renderExperienceBarX, l, 0, 69, k, 5);
                }
            }
            //this.client.getProfiler().pop();
            if (this.client.player.experienceLevel > 0) {
                //this.client.getProfiler().push("expLevel");
                String string = String.valueOf(this.client.player.experienceLevel);
                k = (this.scaledWidth - this.getTextRenderer().getWidth(string)) / 2;
                l = this.scaledHeight - 31 + 4;//-4 originally or +3
                this.drawTextWithThickShadow(matrices, string, k, l, 8453920, 1F);
//                //this.client.getProfiler().pop();
            }
        }
        //ARMOR
        //this.client.getProfiler().push("armor");
        int arm = player.getArmor();
        float armPercentage = (float) arm / 20;//NOTE: Math.round(int/int)==0
        if (arm > 0) {
            displacement.put("arm", true);
            int armHeight = this.getDrawIconHeight(armPercentage);
            this.drawHCSTexture(matrices, xx, yy, 0, 32, 16, 16);
            this.drawHCSTexture(matrices, xx, yy + 16 - armHeight, 16, 48 - armHeight, 16, armHeight);
            this.drawTextWithThickShadow(matrices, arm < 10 ? " " + arm : String.valueOf(arm), xx + 4, yyy + 11, getColorByPercentage(armPercentage), 0.75F);
            xx += 20;
        } else displacement.put("arm", false);
        //HEALTH
        //this.client.getProfiler().swap("health");
        displacement.put("hea", true);
        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
        float tem = temperatureManager.get();
        float hea = player.getHealth();
        float heaMax = player.getMaxHealth();
        float heaPercentage = hea / heaMax;
        int heaHeight = this.getDrawIconHeight((float) Math.pow(heaPercentage, 0.8D));//(float)(Math.log1p(((double)heaPercentage)*1.2D)/(Math.log1p(1.2D))
        int heaDeviation = 0;
        int heaShake = 0;
        float heaAbsorption = player.getAbsorptionAmount();
        if (Math.floor(hea) - Math.floor(heaLast) != 0 || heaLast > hea) heaTwinkleCoolDown = 5;
        boolean heaTwinkle = heaTwinkleCoolDown > 0;
        if (hea <= 4 && this.ticks % 3 == 0) heaShake = Math.round((float) Math.random() * 2) - 1;
        if (player.hasStatusEffect(StatusEffects.POISON)) {
            heaDeviation = 16;
            if (hea > 0 && hea < 1) heaHeight = 2;
        } else if (player.hasStatusEffect(StatusEffects.WITHER)) heaDeviation = 32;
        else if (player.hasStatusEffect(StatusEffects.ABSORPTION)) heaDeviation = 48;
        else if (player.isFrozen()) heaDeviation = 80;
        else if (tem >= 1.0F) heaDeviation = 64;
        this.drawHCSTexture(matrices, xx, yy + heaShake, heaTwinkle ? 16 : 0, 0, 16, 16);//layer 1(background)
        if (heaTwinkle) --heaTwinkleCoolDown;
        this.drawHCSTexture(matrices, xx, yy + (16 - heaHeight) + heaShake, 32 + heaDeviation, 16 - heaHeight, 16, heaHeight);//layer 2
        if (player.world.getLevelProperties().isHardcore())
            this.drawHCSTexture(matrices, xx, yy + (16 - heaHeight) + heaShake, heaDeviation > 0 ? 144 : 128, 16 - heaHeight, 16, heaHeight);
        this.drawTextWithThickShadow(matrices, String.format("%.1f", hea > 0 ? Math.max(hea, 0.1F) : Math.max(hea, 0.0F)), xx, yyy + 11, getColorByPercentage(heaPercentage), 0.75F);//\n is invalid
        this.drawTextWithThickShadow(matrices, (heaAbsorption >= 1.0F ? "+" + String.format("%.1f", heaAbsorption) : "") + "/" + String.format("%.1f", heaMax), xx, yyy + 17, getColorByPercentage(heaPercentage), 0.5F);
        //STAMINA
        float str = ((StatAccessor) player).getStaminaManager().get();
        displacement.put("str", true);
        xx += 20;
        int strHeight = this.getDrawIconHeight((float) Math.pow(str, 0.8D));
        int strDeviation = 0, strShake = 0;
        if (this.ticks % (Math.round(str * 20) + 1) == 0 && str < 0.3F)
            strShake = Math.round((float) Math.random() * 2) - 1;
        this.drawHCSTexture(matrices, xx, yy + strShake, 0, 112, 16, 16);
        this.drawHCSTexture(matrices, xx, yy + (16 - strHeight) + strShake, 16 + strDeviation, 128 - strHeight, 16, strHeight);
        this.drawTextWithThickShadow(matrices, customNumberFormatter(str < 0.1 ? " #%" : "##%", str), xx + 2, yyy + 11, getColorByPercentage(str), 0.75F);
        //THIRST
        displacement.put("thi", true);
        xx += 20;
        float thi = ((StatAccessor) player).getThirstManager().get();
        int thiHeight = this.getDrawIconHeight(thi, 1, 1);//(float)Math.pow(thi,1.18D)
        if (thiHeight < 0) thiHeight = 0;
        else if (thi > 0.05F && thiHeight <= 1) thiHeight = 2;
        int thiDeviation = 0, thiShake = 0;
        //Note that int%0 will throw java.lang.ArithmeticException: / by zero
        if (this.ticks % (Math.round(thi * 20) * 3 + 1) == 0 && thi < 0.3F)
            thiShake = Math.round((float) Math.random() * 2) - 1;
        if (player.hasStatusEffect(HcsEffects.THIRST) || player.hasStatusEffect(HcsEffects.DIARRHEA))
            thiDeviation = 16;
        this.drawHCSTexture(matrices, xx, yy + thiShake, 0, 48, 16, 16);
        this.drawHCSTexture(matrices, xx, yy + (16 - thiHeight) + thiShake, 16 + thiDeviation, 64 - thiHeight, 16, thiHeight);
        this.drawTextWithThickShadow(matrices, customNumberFormatter(thi < 0.1 ? " #%" : "##%", thi), xx + 2, yyy + 11, getColorByPercentage(thi), 0.75F);
        //HUNGER
        //this.client.getProfiler().swap("food");
        displacement.put("hun", true);
        xx += 20;
        HungerManager hunManager = player.getHungerManager();
        float hun = (float) hunManager.getFoodLevel();
        float hunSaturation = hunManager.getSaturationLevel();
        float hunExhaustion = (hunSaturation > 0 || statusManager.hasDecimalFoodLevel()) ? 0.0F : statusManager.getExhaustion();
        float hunPercentage = (hun - hunExhaustion / 4.0F) / 20.0F;
        if (hunPercentage < 0.0F) hunPercentage = 0.0F;
        else if (hunPercentage > 1.0F) hunPercentage = 1.0F;
        int hunHeight = this.getDrawIconHeight(hunPercentage * 1.1F, 4, 0);
        int hunDeviation = 0, hunShake = 0;
        if (hunSaturation <= 0.0F && this.ticks % (hun * 3 + 1) == 0)
            hunShake = Math.round((float) Math.random() * 2) - 1;
        if (player.hasStatusEffect(StatusEffects.HUNGER) || player.hasStatusEffect(HcsEffects.DIARRHEA))
            hunDeviation = 16;
        this.drawHCSTexture(matrices, xx, yy + hunShake, 0, 16, 16, 16);
        this.drawHCSTexture(matrices, xx, yy + (16 - hunHeight) + hunShake, 16 + hunDeviation, 32 - hunHeight, 16, hunHeight);
        this.drawTextWithThickShadow(matrices, customNumberFormatter(hunPercentage < 0.1 ? " #%" : "##%", hunPercentage), xx + 2, yyy + 11, getColorByPercentage(hunPercentage), 0.75F);
        //SANITY
        displacement.put("san", true);
        xx += 20;
        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
        float san = sanityManager.get();
        float sanDifference = sanityManager.getDifference(), sanDifferenceAbs = Math.abs(sanDifference);
        if (san > 1.0F) san = 1.0F;
        else if (san < 0.0F) san = 0.0F;
        int sanHeight = this.getDrawIconHeight((float) Math.pow(san, 0.6D));
        int sanDeviation = 0, sanShake = 0;
        if (this.ticks % (Math.round(san * 20) * 3 + 1) == 0 && san < 0.3F)
            sanShake = Math.round((float) Math.random() * 2) - 1;
        if (sanDifferenceAbs > 0.0049F) sanTwinkleCoolDown = 5;
        boolean sanTwinkle = sanTwinkleCoolDown > 0;
        if (sanTwinkle) --sanTwinkleCoolDown;
        this.drawHCSTexture(matrices, xx, yy + sanShake, sanTwinkle ? 16 : 0, 80, 16, 16);
        this.drawHCSTexture(matrices, xx, yy + (16 - sanHeight) + sanShake, 32 + sanDeviation, 96 - sanHeight, 16, sanHeight);
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
            this.drawHCSTexture(matrices, xx, yy + sanShake + (((this.ticks % (shakeInterval * 2)) < shakeInterval) ? 1 : 0), devi, 80, 16, 16);
        }
        this.drawTextWithThickShadow(matrices, customNumberFormatter(san < 0.1F ? " #%" : "##%", san), xx + 2, yyy + 11, getColorByPercentage(san), 0.75F);
        //TEMPERATURE
        displacement.put("tem", true);
        xx += 20;
        //The time before get damaged for too hot or too cold
        float temSaturationPercentage = temperatureManager.getSaturationPercentage();
        int temShake = 0;
        if (this.ticks % 3 == 0) temShake = Math.round((float) Math.random() * 2) - 1;
        int temHeight = this.getDrawIconHeight(temSaturationPercentage);
        if (tem <= 0.0F) {//Cold
            this.drawHCSTexture(matrices, xx, yy + temShake, 0, 64, 16, 16);
            this.drawHCSTexture(matrices, xx, yy + (16 - temHeight) + temShake, 16, 80 - temHeight, 16, temHeight);
        } else if (tem >= 1.0F) {//Hot
            this.drawHCSTexture(matrices, xx + temShake, yy, 208, 64, 16, 16);
            this.drawHCSTexture(matrices, xx, yy + (16 - temHeight) + temShake, 224, 80 - temHeight, 16, temHeight);
        } else {
            int temDeviation = (int) Math.floor(getTempForDisplay(tem) * 12) * 16;
            if (temDeviation <= 0) temDeviation = 16;
            else if (temDeviation > 176) temDeviation = 176;
            this.drawHCSTexture(matrices, xx, yy, 16 + temDeviation, 64, 16, 16);
        }
        //WETNESS
        /*float we = 0.0F;
        if (we > 0.0F) {
            xx += 20;
            displacement.put("we", true);
            int weHeight = this.getDrawIconHeight(we);
            int weShake = 0;
            this.drawHCSTexture(matrices, xx, yy + weShake, 0, 128, 16, 16);
            this.drawHCSTexture(matrices, xx, yy + (16 - weHeight) + weShake, 16, 144 - weHeight, 16, weHeight);
            this.drawTextWithThickShadow(matrices, customNumberFormatter(we < 0.1 ? " #%" : "##%", we), xx + 2, yyy + 11, getColorByPercentage(we), 0.75F);
        } else*/
        displacement.put("we", false);
        //AIR
        //this.client.getProfiler().swap("air");
        int ai = player.getAir();
        if (ai < 0) ai = 0;
        int aiMax = player.getMaxAir();
        if (player.isSubmergedIn(FluidTags.WATER) || ai < aiMax) {
            displacement.put("ai", true);
            xx += 20;
            int aiShake = 0;
            float aiPercentage = (float) ai / aiMax;
            if (ai <= (aiMax / 3) && this.ticks % 3 == 0) aiShake = Math.round((float) Math.random() * 2) - 1;
            this.drawHCSTexture(matrices, xx, yy + aiShake, 32, 32, 16, 16);//, Math.max((float)Math.pow(aiPercentage,0.67F),0.33F)
            this.drawTextWithThickShadow(matrices, customNumberFormatter(aiPercentage < 0.1 ? " #%" : "##%", aiPercentage), xx + 2, yyy + 11, getColorByPercentage(aiPercentage), 0.75F);
        } else displacement.put("ai", false);
        //MOUNT HEALTH
        if (shouldRenderMountHealth && livingEntity != null) {
            displacement.put("mo", true);
            xx += 20;
            float mo = livingEntity.getHealth();
            float moMax = livingEntity.getMaxHealth();
            float moPercentage = mo / moMax;
            int moHeight = getDrawIconHeight(moPercentage);
            this.drawHCSTexture(matrices, xx, yy, 0, 0, 16, 16);
            this.drawHCSTexture(matrices, xx, yy + (16 - moHeight), 48, 48 - moHeight, 16, moHeight);
            this.drawTextWithThickShadow(matrices, String.format("%.1f", mo > 0 ? Math.max(mo, 0.1F) : Math.max(mo, 0.0F)), xx, yyy + 11, getColorByPercentage(moPercentage), 0.75F);
            this.drawTextWithThickShadow(matrices, "/" + String.format("%.1f", moMax), xx, yyy + 17, getColorByPercentage(moPercentage), 0.5F);
        } else displacement.put("mo", false);
        //this.client.getProfiler().pop();
        heaLast = hea;
        shouldRenderMountHealth = shouldRenderMountJumpBar = false;
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void renderExperienceBar(MatrixStack matrices, int x, @NotNull CallbackInfo ci) {
        this.client.getProfiler().push("expBar");
        this.client.getProfiler().push("expLevel");
        renderExperienceBarX = x;
        ci.cancel();
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    private void renderMountHealth(MatrixStack matrices, CallbackInfo ci) {
        LivingEntity livingEntity = this.getRiddenEntity();
        if (livingEntity == null) {
            shouldRenderMountHealth = false;
            return;
        }
        int i = this.getHeartCount(livingEntity);
        if (i == 0) {
            shouldRenderMountHealth = false;
            return;
        }
        shouldRenderMountHealth = true;
        this.client.getProfiler().swap("mountHealth");
        ci.cancel();
    }

    @Inject(method = "renderMountJumpBar", at = @At("HEAD"), cancellable = true)
    public void renderMountJumpBar(JumpingMount mount, MatrixStack matrices, int x, @NotNull CallbackInfo ci) {
        this.client.getProfiler().push("jumpBar");
        shouldRenderMountJumpBar = true;
        renderExperienceBarX = x;
        ci.cancel();
    }
}
