package pages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProductReader {

    /**
     * Reads products from JSON file using Jackson
     * @param fileName Name of the JSON file relative to resources (e.g., "testdata/products.json")
     * @return List of Product objects
     */
    public static List<Product> readProductsFromJson(String fileName) {
        List<Product> products = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();

            // Try to read from resources folder first
            InputStream is = ProductReader.class
                    .getClassLoader()
                    .getResourceAsStream(fileName);

            JsonNode rootNode;

            if (is != null) {
                // Read from resources
                rootNode = mapper.readTree(is);
                System.out.println("Reading products from resources: " + fileName);
            } else {
                // Try to read from file system
                File file = new File(fileName);
                if (!file.exists()) {
                    throw new RuntimeException("JSON file not found: " + fileName);
                }
                rootNode = mapper.readTree(file);
                System.out.println("Reading products from file: " + fileName);
            }

            // Get the products array
            JsonNode productsArray = rootNode.get("products");

            if (productsArray == null || !productsArray.isArray()) {
                throw new RuntimeException("Invalid JSON structure: 'products' array not found");
            }

            // Parse each product
            for (JsonNode productNode : productsArray) {
                Product product = mapper.treeToValue(productNode, Product.class);
                products.add(product);
            }

            System.out.println("Successfully loaded " + products.size() + " products from JSON");

        } catch (Exception e) {
            System.err.println("Error reading products from JSON: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to read products from JSON", e);
        }

        return products;
    }

    /**
     * Alternative method: Read products from a specific file path
     * @param filePath Full path to the JSON file
     * @return List of Product objects
     */
    public static List<Product> readProductsFromFilePath(String filePath) {
        List<Product> products = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(filePath);

            if (!file.exists()) {
                throw new RuntimeException("JSON file not found at path: " + filePath);
            }

            JsonNode rootNode = mapper.readTree(file);
            JsonNode productsArray = rootNode.get("products");

            if (productsArray == null || !productsArray.isArray()) {
                throw new RuntimeException("Invalid JSON structure: 'products' array not found");
            }

            for (JsonNode productNode : productsArray) {
                Product product = mapper.treeToValue(productNode, Product.class);
                products.add(product);
            }

            System.out.println("Successfully loaded " + products.size() + " products from: " + filePath);

        } catch (Exception e) {
            System.err.println("Error reading products from file path: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to read products from file path", e);
        }

        return products;
    }
}