package payment;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class CardPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private By nameOnCardInput = By.cssSelector("input[name='name_on_card']");
    private By cardNumberInput = By.cssSelector("input[name='card_number']");
    private By cvcInput = By.cssSelector("input[name='cvc']");
    private By expiryMonthInput = By.cssSelector("input[name='expiry_month']");
    private By expiryYearInput = By.cssSelector("input[name='expiry_year']");
    private By payButton = By.cssSelector("button[data-qa='pay-button']");
    private By successMessage = By.id("success_message");

    public CardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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
     * Fill payment form using CardInfo from JSON
     * @param cardInfo CardInfo object containing payment details
     */
    public void fillPaymentForm(CardInfo cardInfo) {
        try {
            System.out.println("\n>>> Filling Payment Form");
            System.out.println("Cardholder: " + cardInfo.getNameOnCard());

            // Wait for form to be visible
            wait.until(ExpectedConditions.presenceOfElementLocated(nameOnCardInput));

            // Fill Name on Card
            WebElement nameField = driver.findElement(nameOnCardInput);
            scrollToElement(nameField);
            highlight(nameField);
            nameField.clear();
            nameField.sendKeys(cardInfo.getNameOnCard());
            System.out.println("✓ Entered name on card");
            Thread.sleep(300);

            // Fill Card Number
            WebElement cardNumField = driver.findElement(cardNumberInput);
            scrollToElement(cardNumField);
            highlight(cardNumField);
            cardNumField.clear();
            cardNumField.sendKeys(cardInfo.getCardNumber());
            System.out.println("✓ Entered card number");
            Thread.sleep(300);

            // Fill CVC
            WebElement cvcField = driver.findElement(cvcInput);
            scrollToElement(cvcField);
            highlight(cvcField);
            cvcField.clear();
            cvcField.sendKeys(cardInfo.getCvc());
            System.out.println("✓ Entered CVC");
            Thread.sleep(300);

            // Fill Expiry Month
            WebElement monthField = driver.findElement(expiryMonthInput);
            scrollToElement(monthField);
            highlight(monthField);
            monthField.clear();
            monthField.sendKeys(cardInfo.getExpiryMonth());
            System.out.println("✓ Entered expiry month");
            Thread.sleep(300);

            // Fill Expiry Year
            WebElement yearField = driver.findElement(expiryYearInput);
            scrollToElement(yearField);
            highlight(yearField);
            yearField.clear();
            yearField.sendKeys(cardInfo.getExpiryYear());
            System.out.println("✓ Entered expiry year");
            Thread.sleep(300);

            System.out.println("✓ Payment form filled successfully");

        } catch (Exception e) {
            System.err.println("✗ Failed to fill payment form: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fill payment form", e);
        }
    }

    /**
     * Fill payment form from JSON file
     * @param jsonFilePath Path to card_info.json file
     */
    public void fillPaymentFormFromJson(String jsonFilePath) {
        CardInfo cardInfo = CardInfoReader.readCardInfoFromJson(jsonFilePath);
        fillPaymentForm(cardInfo);
    }

    /**
     * Click Pay and Confirm Order button
     */
    public void clickPayButton() {
        try {
            System.out.println("\n>>> Clicking Pay and Confirm Order Button");

            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(payButton));
            scrollToElement(button);
            highlight(button);

            button.click();
            System.out.println("✓ Pay button clicked");
            Thread.sleep(2000);

        } catch (Exception e) {
            System.err.println("✗ Failed to click pay button: " + e.getMessage());
            e.printStackTrace();

            // Try JavaScript click as fallback
            try {
                System.out.println("Attempting JavaScript click fallback...");
                WebElement button = driver.findElement(payButton);
                scrollToElement(button);
                highlight(button);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                System.out.println("✓ Pay button clicked via JavaScript");
                Thread.sleep(2000);
            } catch (Exception e2) {
                throw new RuntimeException("Failed to click pay button", e2);
            }
        }
    }

    /**
     * Verify if payment was successful by checking success message
     * @return true if success message is visible
     */
    public boolean isPaymentSuccessful() {
        try {
            Thread.sleep(2000);

            // Check if success message is visible
            WebElement message = wait.until(ExpectedConditions.presenceOfElementLocated(successMessage));

            // Check if the element is displayed (not hidden)
            boolean isVisible = message.isDisplayed();

            if (isVisible) {
                System.out.println("\n✓✓✓ SUCCESS: " + message.getText());
            }

            return isVisible;

        } catch (Exception e) {
            System.out.println("Success message not found or not visible yet");
            return false;
        }
    }

    /**
     * Verify we're on the payment page
     * @return true if URL contains /payment
     */
    public boolean isOnPaymentPage() {
        try {
            String currentUrl = driver.getCurrentUrl();
            return currentUrl.contains("/payment");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get success message text
     * @return The success message text, or empty string if not found
     */
    public String getSuccessMessage() {
        try {
            WebElement message = driver.findElement(successMessage);
            if (message.isDisplayed()) {
                return message.getText();
            }
        } catch (Exception e) {
            System.err.println("Could not get success message: " + e.getMessage());
        }
        return "";
    }
}