// Actions.java - Complete utility for all Selenium actions (fixed: handles ads/overlays + safer click/type)
package utils;

import Browser_utils.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class Actions {

    // ✅ FIX 1: Lazy getters instead of static final fields.
    // The old code did:
    //     private static final WebDriver driver = DriverManager.getDriver();
    //     private static final WebDriverWait wait = new WebDriverWait(driver, ...);
    // These run at class-load time — BEFORE DriverManager.initializeDriver() is called
    // in BaseTest's @BeforeClass. That causes an immediate crash.
    private static WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    private static WebDriverWait getWait() {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(10));
    }

    // -------------------- CLICK --------------------
    public static void click(By locator) {
        dismissAdsAndOverlays();

        WebElement element = getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
        scrollIntoView(element);
        highlight(element);

        try {
            getWait().until(ExpectedConditions.elementToBeClickable(locator)).click();
        } catch (ElementClickInterceptedException e) {
            // overlay/ad blocked click → remove and JS click
            dismissAdsAndOverlays();
            jsClick(locator);
        } catch (TimeoutException e) {
            // if clickable wait fails, try JS click as fallback
            jsClick(locator);
        }
    }

    // -------------------- SEND KEYS --------------------
    public static void sendKeys(By locator, String value) {
        dismissAdsAndOverlays();

        WebElement element = getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
        scrollIntoView(element);
        highlight(element);

        try {
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            element.sendKeys(Keys.DELETE);
            element.sendKeys(value);
        } catch (Exception e) {
            // fallback JS set value
            jsType(locator, value);
        }
    }

    // -------------------- WAITS --------------------
    public static void waitForElementVisible(By locator) {
        dismissAdsAndOverlays();
        getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // -------------------- SELECT --------------------
    public static void selectByValue(By locator, String value) {
        dismissAdsAndOverlays();
        WebElement element = getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
        scrollIntoView(element);
        highlight(element);
        new Select(element).selectByValue(value);
    }

    public static void selectByVisibleText(By locator, String text) {
        dismissAdsAndOverlays();
        WebElement element = getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
        scrollIntoView(element);
        highlight(element);
        new Select(element).selectByVisibleText(text);
    }

    // -------------------- CHECKBOX --------------------
    public static void setCheckbox(By locator, boolean shouldBeChecked) {
        dismissAdsAndOverlays();
        WebElement element = getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
        scrollIntoView(element);
        highlight(element);

        boolean isChecked = element.isSelected();
        if (isChecked != shouldBeChecked) {
            try {
                getWait().until(ExpectedConditions.elementToBeClickable(locator)).click();
            } catch (Exception e) {
                jsClick(locator);
            }
        }
    }

    // -------------------- HELPERS --------------------
    private static void highlight(WebElement element) {
        try {
            ((JavascriptExecutor) getDriver()).executeScript(
                    "arguments[0].style.border='5px solid black';" +
                            "arguments[0].style.boxShadow='0 0 10px black';",
                    element
            );
        } catch (Exception ignored) {}
    }

    private static void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) getDriver()).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'center'});",
                    element
            );
        } catch (Exception ignored) {}
    }

    private static void jsClick(By locator) {
        WebElement element = getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", element);
    }

    // ✅ FIX 2: Fire input + change events after setting value.
    // The old code only did: arguments[0].value = arguments[1];
    // That silently sets the DOM value but never notifies the page's JS framework,
    // so React/Angular/vanilla listeners never fire and the form submits empty.
    private static void jsType(By locator, String value) {
        WebElement element = getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) getDriver()).executeScript(
                "arguments[0].value = arguments[1];" +
                        "arguments[0].dispatchEvent(new Event('input',  { bubbles: true }));" +
                        "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                element, value
        );
    }

    // Removes common ad iframes/overlays on AutomationExercise + generic fixed overlays
    private static void dismissAdsAndOverlays() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) getDriver();

            js.executeScript(
                    "const selectors=[" +
                            "'iframe[id^=\"aswift\"]'," +
                            "'iframe[src*=\"doubleclick\"]'," +
                            "'iframe[src*=\"googlesyndication\"]'," +
                            "'ins.adsbygoogle'," +
                            "'.adsbygoogle'," +
                            "'.google-auto-placed'," +
                            "'.ad-container'," +
                            "'.overlay'," +
                            "'.modal'," +
                            "'.modal-backdrop'," +
                            "'div[style*=\"position: fixed\"][style*=\"z-index\"]'" +
                            "];" +
                            "selectors.forEach(s=>document.querySelectorAll(s).forEach(el=>el.remove()));"
            );

            // try close buttons if present
            safeJsClickIfPresent("button[aria-label='Close']");
            safeJsClickIfPresent(".close");
            safeJsClickIfPresent("button.close");

        } catch (Exception ignored) {}
    }

    private static void safeJsClickIfPresent(String css) {
        try {
            ((JavascriptExecutor) getDriver()).executeScript(
                    "let el=document.querySelector(arguments[0]); if(el){el.click();}", css
            );
        } catch (Exception ignored) {}
    }
}
