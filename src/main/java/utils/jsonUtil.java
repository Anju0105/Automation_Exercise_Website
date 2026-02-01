package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

public class jsonUtil {

    public static JsonNode readJson(String fileName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = jsonUtil.class
                    .getClassLoader()
                    .getResourceAsStream(fileName);

            if (is == null) {
                throw new RuntimeException("JSON file not found: " + fileName);
            }

            return mapper.readTree(is);

        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON: " + fileName, e);
        }
    }

    public static String getValue(String fileName, String key) {
        JsonNode data = readJson(fileName);
        return data.get(key).asText();
    }
}
