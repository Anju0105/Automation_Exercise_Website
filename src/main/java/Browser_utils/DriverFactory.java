// DriverFactory.java - Creates a configured WebDriver for the requested browser
package Browser_utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;

public class DriverFactory {

    public static WebDriver createDriver(String browser) {
        browser = browser.toLowerCase();
        WebDriver driver;

        switch (browser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--incognito");
                chromeOptions.addArguments("--disable-notifications");
                chromeOptions.addArguments("--disable-infobars");
                chromeOptions.addArguments("--disable-extensions");
                chromeOptions.addArguments("--disable-features=VizDisplayCompositor");
                chromeOptions.setExperimentalOption("excludeSwitches",
                        new String[]{"enable-automation", "load-extension"});
                chromeOptions.setExperimentalOption("prefs",
                        java.util.Map.of(
                                "credentials_enable_service", false,
                                "profile.password_manager_enabled", false,
                                "profile.default_content_setting_values.notifications", 2
                        ));
                driver = new ChromeDriver(chromeOptions);
                break;

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments("-private");
                firefoxOptions.addPreference("dom.webnotifications.enabled", false);
                firefoxOptions.addPreference("signon.rememberSignons", false);
                firefoxOptions.addPreference("signon.autofillForms", false);
                driver = new FirefoxDriver(firefoxOptions);
                break;

            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("inprivate");
                edgeOptions.addArguments("--disable-notifications");
                edgeOptions.addArguments("--disable-extensions");
                edgeOptions.setExperimentalOption("prefs",
                        java.util.Map.of(
                                "credentials_enable_service", false,
                                "profile.password_manager_enabled", false
                        ));
                driver = new EdgeDriver(edgeOptions);
                break;

            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        // Browser-level setup
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        return driver;
    }
}