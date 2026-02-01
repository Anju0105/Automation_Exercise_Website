package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AddComments {

    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private By commentTextArea = By.cssSelector("textarea[name='message']");
    private By orderMsgDiv = By.id("ordermsg");

    public AddComments(WebDriver driver) {
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
     * Add comment to order
     * @param comment The comment text to add
     */
    public void addOrderComment(String comment) {
        try {
            System.out.println("\n>>> Adding Order Comment");
            System.out.println("Comment: " + comment);

            // Wait for the comment textarea to be present
            WebElement textarea = wait.until(ExpectedConditions.presenceOfElementLocated(commentTextArea));

            // Scroll to textarea
            scrollToElement(textarea);

            // Highlight textarea
            highlight(textarea);

            // Clear and enter comment
            textarea.clear();
            textarea.sendKeys(comment);

            System.out.println("✓ Comment added successfully");
            Thread.sleep(500);

        } catch (Exception e) {
            System.err.println("✗ Failed to add comment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add order comment", e);
        }
    }

    /**
     * Verify comment field is visible
     * @return true if comment field is visible
     */
    public boolean isCommentFieldVisible() {
        try {
            WebElement textarea = driver.findElement(commentTextArea);
            return textarea.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the current comment text
     * @return The text in the comment field
     */
    public String getCurrentComment() {
        try {
            WebElement textarea = driver.findElement(commentTextArea);
            return textarea.getAttribute("value");
        } catch (Exception e) {
            System.err.println("Failed to get comment text: " + e.getMessage());
            return "";
        }
    }
}