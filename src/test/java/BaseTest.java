// BaseTest.java - TestNG base class; boots the driver once before all tests

import Browser_utils.DriverManager;
import config.ConfigLoader;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

public class BaseTest {

    protected static WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void suiteSetup() {
        DriverManager.initializeDriver();
        driver = DriverManager.getDriver();
        driver.get(ConfigLoader.getWebsiteUrl());
    }

    @AfterClass(alwaysRun = true)
    public void suiteTearDown() {
        DriverManager.quitDriver();
    }
}