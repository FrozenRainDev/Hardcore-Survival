package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Reg;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommUtil {

    public static String customNumFormat(String pattern, double value) {
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        return decimalFormat.format(value);
    }

    public static String customNumFormat(String pattern, float value) {
        return customNumFormat(pattern, (double) value);
    }

    public static String retain5(double val) {
        // Retain five decimal places
        return String.format("%.5f", val);
    }

    @Deprecated
    public static void renderGuiQuad(@NotNull BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha, boolean needShader) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(x, y, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y, 0.0).color(red, green, blue, alpha).next();
        if (needShader) BufferRenderer.drawWithGlobalProgram(buffer.end());
        else BufferRenderer.drawWithGlobalProgram(buffer.end()); //never use it !!
    }

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
    public static boolean regEntryContains(@NotNull RegistryEntry<?> entry, String pattern) {
        Optional<? extends RegistryKey<?>> key = entry.getKey();
        if (key != null && key.isPresent()) return key.get().getValue().getPath().contains(pattern);
        return false;
    }

    public static class UpdateHelper {
        public static final String MOD_VER = "0.15.1";

        @Contract(pure = true)
        public static String fetchLatestVersion() {
            try {
                final String url = "https://modrinth.com/mod/hardcore-survival/versions";
                URL website = new URL(url);
                URLConnection connection = website.openConnection();
                connection.setRequestProperty("charset", "UTF-8");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String line;
                StringBuilder htmlCode = new StringBuilder();
                while ((line = reader.readLine()) != null) htmlCode.append(line);
                reader.close();
                String patternString = "aria-label=\"Download ([^\"]+)\"";
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(htmlCode);
                List<String> versions = new ArrayList<>();
                while (matcher.find()) {
                    String download = matcher.group(1);
                    versions.add(download);
                }
                return getLatestVersionInList(versions);
            } catch (IOException exception) {
                Reg.LOGGER.error("UpdateHelper: " + exception.getMessage());
            }
            return "";
        }

        @Contract(pure = true)
        public static int compareVersions(String ver1, String ver2) {
            ver1 = ver1.replaceAll("[a-zA-Z\\s]+", "");
            ver2 = ver2.replaceAll("[a-zA-Z\\s]+", "");
            String[] v1 = ver1.split("\\."), v2 = ver2.split("\\.");
            for (int i = 0; i < Math.min(v1.length, v2.length); ++i) {
                try {
                    int ele1 = Integer.parseInt(v1[i]), ele2 = Integer.parseInt(v2[i]);
                    int result = Integer.compare(ele1, ele2);
                    if (result != 0) return result;
                } catch (NumberFormatException exception) {
                    Reg.LOGGER.error("UpdateHelper: " + exception.getMessage());
                }
            }
            return 0;
        }

        @Contract(pure = true)
        private static String getLatestVersionInList(List<String> versions) {
            String latestFound = "";
            if (versions == null) return latestFound;
            for (String version : versions) {
                if (latestFound.isEmpty()) latestFound = version;
                else latestFound = compareVersions(latestFound, version) >= 0 ? latestFound : version;
            }
            return latestFound;
        }
    }

}
