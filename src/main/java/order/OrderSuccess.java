package order;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class OrderSuccess {

    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private By orderPlacedHeading = By.cssSelector("h2[data-qa='order-placed']");
    private By continueButton = By.cssSelector("a[data-qa='continue-button']");
    private By downloadInvoiceButton = By.xpath("//a[contains(@href,'/download_invoice')]");

    public OrderSuccess(WebDriver driver) {
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
            Thread.sleep(300);
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
            Thread.sleep(500);
        } catch (Exception ignored) {}
    }

    /**
     * Verify order was placed successfully
     * @return true if "Order Placed!" heading is visible
     */
    public boolean isOrderPlaced() {
        try {
            WebElement heading = wait.until(ExpectedConditions.presenceOfElementLocated(orderPlacedHeading));
            boolean isVisible = heading.isDisplayed();

            if (isVisible) {
                String text = heading.getText();
                System.out.println("\n>>> Order Success Page");
                System.out.println("Heading: " + text);
            }

            return isVisible;
        } catch (Exception e) {
            System.err.println("Order placed heading not found: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get order success message
     * @return The success message text
     */
    public String getSuccessMessage() {
        try {
            WebElement message = driver.findElement(By.xpath("//p[contains(text(),'Congratulations')]"));
            return message.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Click Continue button to go back to home page
     */
    public void clickContinue() {
        try {
            System.out.println("\n>>> Clicking Continue Button");

            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(continueButton));
            scrollToElement(button);
            highlight(button);

            button.click();
            System.out.println("✓ Continue button clicked");
            Thread.sleep(2000);

        } catch (Exception e) {
            System.err.println("✗ Failed to click Continue: " + e.getMessage());

            // Try JavaScript click as fallback
            try {
                System.out.println("Attempting JavaScript click fallback...");
                WebElement button = driver.findElement(continueButton);
                scrollToElement(button);
                highlight(button);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                System.out.println("✓ Continue clicked via JavaScript");
                Thread.sleep(2000);
            } catch (Exception e2) {
                throw new RuntimeException("Failed to click Continue button", e2);
            }
        }
    }

    /**
     * Verify Continue button is visible
     * @return true if button is visible
     */
    public boolean isContinueButtonVisible() {
        try {
            WebElement button = driver.findElement(continueButton);
            return button.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verify we're on the order success page
     * @return true if URL contains payment_done
     */
    public boolean isOnOrderSuccessPage() {
        try {
            String currentUrl = driver.getCurrentUrl();
            return currentUrl.contains("/payment_done");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Click Download Invoice button (optional)
     */
    public void clickDownloadInvoice() {
        try {
            System.out.println("\n>>> Clicking Download Invoice Button");

            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(downloadInvoiceButton));
            scrollToElement(button);
            highlight(button);

            button.click();
            System.out.println("✓ Download Invoice button clicked");
            Thread.sleep(1000);

        } catch (Exception e) {
            System.err.println("✗ Failed to click Download Invoice: " + e.getMessage());
        }
    }
}