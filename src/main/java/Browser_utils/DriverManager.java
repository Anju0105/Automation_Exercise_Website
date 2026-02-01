// DriverManager.java - Singleton that owns the single WebDriver instance
package Browser_utils;

import config.ConfigLoader;
import org.openqa.selenium.WebDriver;

public class DriverManager {

    private static WebDriver driver;

    private DriverManager() {} // prevent instantiation

    public static void initializeDriver() {
        if (driver != null) {
            return; // Already initialized
        }

        String browser = ConfigLoader.getBrowser();
        if (browser == null || browser.trim().isEmpty()) {
            throw new IllegalArgumentException("Browser type not specified in config.");
        }

        try {
            driver = DriverFactory.createDriver(browser);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to initialize WebDriver for browser: " + browser, e);
        }
    }

    public static WebDriver getDriver() {
        if (driver == null) {
            throw new IllegalStateException(
                    "Driver not initialized. Call initializeDriver() first.");
        }
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("Error while quitting driver: " + e.getMessage());
            } finally {
                driver = null;
            }
        }
    }
}