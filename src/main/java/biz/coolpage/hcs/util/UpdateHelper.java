package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Reg;
import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"LoggingSimilarMessage", "StringConcatenationArgumentToLogCall"})
public class UpdateHelper {
    public static final String MOD_VER = "0.17.0";

    @Contract(pure = true)
    public static String fetchLatestVersion() {
        try {
            final String url = "https://modrinth.com/mod/hardcore-survival/versions";
            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            connection.setRequestProperty("charset", "UTF-8");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
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
        } catch (Exception exception) {
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
