package biz.coolpage.hcs.util;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommUtil {

    public static String numFormat(String pattern, double value) {
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        return decimalFormat.format(value);
    }

    public static String numFormat(String pattern, float value) {
        return numFormat(pattern, (double) value);
    }

    @Contract(pure = true)
    public static @NotNull String retain5(double val) {
        // Retain five decimal places
        return String.format("%.5f", val);
    }

    /*
    public static void renderGuiQuad(@NotNull BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha, boolean needShader) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(x, y, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y, 0.0).color(red, green, blue, alpha).next();
        if (needShader) BufferRenderer.drawWithGlobalProgram(buffer.end());
        else BufferRenderer.drawWithGlobalProgram(buffer.end()); //never onInteract it !!
    }
     */

    @Contract(pure = true)
    public static <T> @NotNull T optElse(@Nullable T instance, @NotNull T defaultVal) {
        return Optional.ofNullable(instance).orElse(defaultVal);
    }

    //Optional class cannot directly do these:
    @Contract(pure = true)
    public static <T> void applyNullable(@Nullable T instance, @NotNull Consumer<T> consumer) {
        if (instance != null) consumer.accept(instance);
    }

    @Contract(pure = true)
    public static <T, R> @NotNull R applyNullable(@Nullable T instance, @NotNull Function<T, R> function, @NotNull R defaultVal) {
        if (instance == null) return defaultVal;
        return function.apply(instance);
    }

    @Contract(pure = true)
    public static boolean hasNull(@Nullable Object... objects) {
        if (objects == null || objects.length == 0) return true;
        for (Object object : objects) if (object == null) return true;
        return false;
    }

    @Contract(pure = true)
    public static boolean regEntryContains(@NotNull RegistryEntry<?> entry, String pattern) {
        Optional<? extends RegistryKey<?>> key = entry.getKey();
        if (key != null && key.isPresent()) return key.get().getValue().getPath().contains(pattern);
        return false;
    }

    public static void rehabPlayerStats(@Nullable Object obj) {
        if (obj instanceof ServerPlayerEntity player) {
            player.getHungerManager().add(40, 1.0F);
            if (player instanceof StatAccessor p) {
                p.getThirstManager().addDirectly(1000D);
                p.getSanityManager().add(1.0);
                p.getStatusManager().setSoulImpairedStat(0);
                p.getInjuryManager().applyPainkiller();
                p.getInjuryManager().setBleeding(0.0);
                p.getInjuryManager().setFracture(0.0);
                p.getDiseaseManager().reset();
                p.getMoodManager().setHappiness(1.0);
            }
        }
    }

}
