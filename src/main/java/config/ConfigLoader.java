// ConfigLoader.java - reads values from config/info.json
package config;

import utils.jsonUtil;
import com.fasterxml.jackson.databind.JsonNode;

public class ConfigLoader {

    private static JsonNode config; // ✅ FIX: removed eager init + extra semicolon
    // Old code: private static JsonNode config = jsonUtil.readJson("config/info.json");;
    // Two problems: (1) the ";;" is a compile warning/error,
    // (2) readJson() runs at class-load time before jsonUtil is ready.

    // ✅ FIX: Lazy-load with a clear error if the file is missing/malformed
    private static JsonNode getConfig() {
        if (config == null) {
            config = jsonUtil.readJson("config/info.json");
            if (config == null) {
                throw new RuntimeException(
                        "Failed to load config/info.json — file is missing or malformed.");
            }
        }
        return config;
    }

    public static String getBrowser() {
        if (!getConfig().has("browser")) {
            throw new RuntimeException("'browser' key not found in config/info.json.");
        }
        return getConfig().get("browser").asText();
    }

    public static String getWebsiteUrl() {
        if (!getConfig().has("Website_Url")) {
            throw new RuntimeException("'Website_Url' key not found in config/info.json.");
        }
        return getConfig().get("Website_Url").asText();
    }

    public static String getWebsiteName() {
        if (!getConfig().has("Website_Name")) {
            throw new RuntimeException("'Website_Name' key not found in config/info.json.");
        }
        return getConfig().get("Website_Name").asText();
    }
}