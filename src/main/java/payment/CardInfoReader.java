package payment;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;

public class CardInfoReader {

    /**
     * Reads card information from JSON file
     * @param fileName Name of the JSON file relative to resources (e.g., "config/card_info.json")
     * @return CardInfo object with payment details
     */
    public static CardInfo readCardInfoFromJson(String fileName) {
        CardInfo cardInfo = null;

        try {
            ObjectMapper mapper = new ObjectMapper();

            // Try to read from resources folder first
            InputStream is = CardInfoReader.class
                    .getClassLoader()
                    .getResourceAsStream(fileName);

            if (is != null) {
                // Read from resources
                cardInfo = mapper.readValue(is, CardInfo.class);
                System.out.println("Reading card info from resources: " + fileName);
            } else {
                // Try to read from file system
                File file = new File(fileName);
                if (!file.exists()) {
                    throw new RuntimeException("Card info JSON file not found: " + fileName);
                }
                cardInfo = mapper.readValue(file, CardInfo.class);
                System.out.println("Reading card info from file: " + fileName);
            }

            System.out.println("Successfully loaded card information");
            System.out.println("Cardholder: " + cardInfo.getNameOnCard());

        } catch (Exception e) {
            System.err.println("Error reading card info from JSON: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to read card info from JSON", e);
        }

        return cardInfo;
    }

    /**
     * Alternative method: Read card info from a specific file path
     * @param filePath Full path to the JSON file
     * @return CardInfo object with payment details
     */
    public static CardInfo readCardInfoFromFilePath(String filePath) {
        CardInfo cardInfo = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(filePath);

            if (!file.exists()) {
                throw new RuntimeException("Card info JSON file not found at path: " + filePath);
            }

            cardInfo = mapper.readValue(file, CardInfo.class);
            System.out.println("Successfully loaded card info from: " + filePath);

        } catch (Exception e) {
            System.err.println("Error reading card info from file path: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to read card info from file path", e);
        }

        return cardInfo;
    }
}