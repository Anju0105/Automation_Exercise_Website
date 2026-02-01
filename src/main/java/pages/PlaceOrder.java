package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PlaceOrder {

    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private By placeOrderButton = By.xpath("//a[@href='/payment' and contains(@class,'check_out')]");
    private By placeOrderButtonAlt = By.cssSelector("a.btn.btn-default.check_out");

    public PlaceOrder(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Highlight element with black border
     */
    private void highlight(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].style.border='5px solid black';" +
                            "arguments[0].style.boxShadow='0 0 10px black';",
                    element
            );
        } catch (Exception ignored) {}
    }

    /**
     * Scroll element into view
     */
    private void scrollToElement(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior:'smooth',block:'center'});",
                    element
            );
        } catch (Exception ignored) {}
    }

    /**
     * Click the Place Order button to proceed to payment
     */
    public void clickPlaceOrder() {
        try {
            System.out.println("\n>>> Clicking Place Order Button");

            WebElement button = null;

            // Try primary locator, fall back to alt
            try {
                button = wait.until(ExpectedConditions.presenceOfElementLocated(placeOrderButton));
            } catch (Exception e) {
                System.out.println("Trying alternative locator...");
                button = wait.until(ExpectedConditions.presenceOfElementLocated(placeOrderButtonAlt));
            }

            scrollToElement(button);
            highlight(button);

            // Wait for clickable then click immediately
            button = wait.until(ExpectedConditions.elementToBeClickable(button));
            button.click();

            System.out.println("✓ Place Order button clicked");

            // Wait for navigation to actually complete instead of blind sleep
            wait.until(ExpectedConditions.urlContains("/payment"));
            System.out.println("Current URL: " + driver.getCurrentUrl());

        } catch (Exception e) {
            System.err.println("✗ Failed to click Place Order: " + e.getMessage());
            e.printStackTrace();

            // JavaScript click fallback
            try {
                System.out.println("Attempting JavaScript click fallback...");
                WebElement button = driver.findElement(placeOrderButton);
                scrollToElement(button);
                highlight(button);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                System.out.println("✓ Place Order clicked via JavaScript");

                // Same here — wait for URL change, not a fixed timer
                wait.until(ExpectedConditions.urlContains("/payment"));
            } catch (Exception e2) {
                System.err.println("✗ JavaScript fallback also failed: " + e2.getMessage());
                throw new RuntimeException("Failed to click Place Order button", e2);
            }
        }
    }

    /**
     * Verify Place Order button is visible
     * @return true if button is visible
     */
    public boolean isPlaceOrderButtonVisible() {
        try {
            WebElement button = driver.findElement(placeOrderButton);
            return button.isDisplayed();
        } catch (Exception e) {
            try {
                WebElement button = driver.findElement(placeOrderButtonAlt);
                return button.isDisplayed();
            } catch (Exception e2) {
                return false;
            }
        }
    }

    /**
     * Verify we're on the payment page after placing order
     * @return true if URL contains /payment
     */
    public boolean isOnPaymentPage() {
        try {
            // Already navigated via urlContains wait in clickPlaceOrder,
            // so this is just a quick check now — no sleep needed
            String currentUrl = driver.getCurrentUrl();
            System.out.println("Checking payment page - URL: " + currentUrl);
            return currentUrl.contains("/payment");
        } catch (Exception e) {
            System.err.println("Error checking payment page: " + e.getMessage());
            return false;
        }
    }
}