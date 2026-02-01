package order;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class DeleteAccount {

    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private By deleteAccountLink = By.xpath("//a[contains(text(),'Delete Account')]");
    private By accountDeletedHeading = By.cssSelector("h2[data-qa='account-deleted']");
    private By continueButton = By.xpath("//a[contains(text(),'Continue')]");

    // Ad/overlay locators — same set as ViewProduct
    private static final By[] AD_LOCATORS = {
            By.cssSelector("div.adchoices-container"),
            By.cssSelector("iframe[src*='ads']"),
            By.cssSelector("div[class*='ad-banner']"),
            By.cssSelector("div[class*='popup']"),
            By.cssSelector("div[id*='ad']"),
            By.cssSelector("div[class*='overlay']"),
            By.cssSelector(".close-btn"),
            By.cssSelector("button[class*='close']"),
            By.xpath("//button[contains(@class,'close') or contains(@aria-label,'Close') or contains(@aria-label,'close')]"),
            By.xpath("//div[contains(@class,'ad')]//button"),
            By.cssSelector("div[style*='position: fixed']")
    };

    public DeleteAccount(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Dismiss any ad or overlay blocking clicks or visibility.
     */
    private void dismissAds() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll(" +
                            "'div[style*=\"position: fixed\"], " +
                            "div[style*=\"position:fixed\"], " +
                            "iframe[src*=\"ads\"], " +
                            "div[class*=\"ad-\"], " +
                            "div[id*=\"ad\"]').forEach(el => el.remove());"
            );
        } catch (Exception ignored) {}

        for (By locator : AD_LOCATORS) {
            try {
                List<WebElement> elements = driver.findElements(locator);
                for (WebElement el : elements) {
                    if (el.isDisplayed()) {
                        el.click();
                        System.out.println("Dismissed ad/overlay: " + locator);
                        new WebDriverWait(driver, Duration.ofSeconds(2))
                                .until(ExpectedConditions.stalenessOf(el));
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private void scrollToTop() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
    }

    /**
     * Check if Delete Account link is visible.
     */
    public boolean isDeleteAccountLinkVisible() {
        try {
            scrollToTop();
            dismissAds();
            WebElement link = wait.until(ExpectedConditions.visibilityOfElementLocated(deleteAccountLink));
            return link.isDisplayed();
        } catch (Exception e) {
            System.err.println("Delete Account link not found: " + e.getMessage());
            return false;
        }
    }

    /**
     * Click the Delete Account link.
     */
    public void clickDeleteAccount() {
        try {
            scrollToTop();
            dismissAds();

            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(deleteAccountLink));
            link.click();
            System.out.println("Clicked Delete Account");

            // Wait for the page to actually change after clicking delete
            // The account-deleted heading appearing IS the confirmation the page loaded
            wait.until(ExpectedConditions.presenceOfElementLocated(accountDeletedHeading));

        } catch (Exception e) {
            System.err.println("Error clicking Delete Account: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Verify account was deleted — checks for the confirmation heading.
     * This was failing because an ad was covering the heading.
     */
    public boolean isAccountDeleted() {
        try {
            // Dismiss ads FIRST — this is why the h2 was never found before.
            // The ad overlay was sitting on top, and the wait kept timing out
            // trying to find an element that was technically present but hidden behind the ad.
            dismissAds();

            WebElement heading = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(accountDeletedHeading)
            );
            System.out.println("Account deleted heading found: " + heading.getText());
            return heading.isDisplayed();

        } catch (Exception e) {
            System.err.println("Account deleted heading not found: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the deletion confirmation message text.
     */
    public String getDeletionMessage() {
        try {
            dismissAds();
            WebElement heading = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(accountDeletedHeading)
            );
            return heading.getText();
        } catch (Exception e) {
            System.err.println("Could not get deletion message: " + e.getMessage());
            return "Account deletion message not found";
        }
    }

    /**
     * Click Continue after account deletion to return to home.
     */
    public void clickContinueAfterDeletion() {
        try {
            dismissAds();

            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(continueButton));
            btn.click();
            System.out.println("Clicked Continue after deletion");

            // Wait for navigation back to home — no blind sleep
            wait.until(ExpectedConditions.not(
                    ExpectedConditions.urlContains("account-deleted")
            ));
            System.out.println("Returned to home page: " + driver.getCurrentUrl());

        } catch (Exception e) {
            System.err.println("Error clicking Continue after deletion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}