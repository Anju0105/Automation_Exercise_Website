package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.FluentWait;
import utils.Actions;
import Browser_utils.DriverManager;
import org.openqa.selenium.WebDriver;
import utils.jsonUtil;
import java.time.Duration;

public class Signup {

    private final By signupLoginBtn = By.cssSelector("a[href='/login']");
    private final By nameInput      = By.cssSelector("input[data-qa='signup-name']");
    private final By emailInput     = By.cssSelector("input[data-qa='signup-email']");
    private final By signUpBtn      = By.cssSelector("button[data-qa='signup-button']");

    private final By emailExistsError = By.xpath(
            "//*[contains(text(),'Email Address already exist')]");

    // Stores the unique email we generated this run — LoginPage and Tests need this
    private static String currentEmail;
    private static String currentName;

    public void clickSignupLogin() {
        Actions.click(signupLoginBtn);
    }

    /**
     * Fills signup form with a UNIQUE email every run.
     * Appends timestamp to the base email from signup.json so it never
     * collides with a previously created (and later deleted) account.
     *
     * signup.json email:  "testing123458@gmail.com"
     * actual email used:  "testing123458_1769977955840@gmail.com"
     */
    public void fillSignupForm() {
        currentName  = jsonUtil.getValue("config/signup.json", "Name");
        String baseEmail = jsonUtil.getValue("config/signup.json", "Email_address");

        // Generate unique email: insert timestamp before the @
        currentEmail = generateUniqueEmail(baseEmail);

        System.out.println("Using unique email: " + currentEmail);

        Actions.sendKeys(nameInput, currentName);
        Actions.sendKeys(emailInput, currentEmail);
    }

    /**
     * If login fails (account was deleted but email blacklisted),
     * this re-does the signup with a brand new unique email.
     * Called by Tests.java when LoginPage detects login failure.
     */
    public void redoSignupWithNewEmail() {
        WebDriver driver = DriverManager.getDriver();

        System.out.println(">>> Re-doing signup with a fresh email...");

        // Navigate back to login/signup page
        Actions.click(signupLoginBtn);

        // Wait for signup form to appear
        Actions.waitForElementVisible(nameInput);

        // Clear and fill with NEW unique email
        currentName  = jsonUtil.getValue("config/signup.json", "Name");
        String baseEmail = jsonUtil.getValue("config/signup.json", "Email_address");
        currentEmail = generateUniqueEmail(baseEmail);

        System.out.println("New unique email: " + currentEmail);

        Actions.sendKeys(nameInput, currentName);
        Actions.sendKeys(emailInput, currentEmail);

        // Click signup
        Actions.click(signUpBtn);

        // This time it MUST succeed — email is brand new
        // Wait briefly then check
        boolean existsAgain = isEmailAlreadyExists();
        if (existsAgain) {
            throw new RuntimeException(
                    "Signup failed even with unique email: " + currentEmail +
                            " — something else is wrong.");
        }

        System.out.println("✓ Re-signup successful with: " + currentEmail);
    }

    public void clickFinalSignupButton() {
        Actions.click(signUpBtn);
    }

    public boolean isEmailAlreadyExists() {
        WebDriver driver = DriverManager.getDriver();
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(5))
                    .pollingEvery(Duration.ofMillis(300))
                    .ignoring(NoSuchElementException.class)
                    .until(d -> d.findElement(emailExistsError));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================================
    // GETTERS — other classes need the email we generated
    // =========================================================================

    /** The unique email used this run. LoginPage uses this instead of login.json. */
    public static String getCurrentEmail() {
        return currentEmail;
    }

    /** The name used this run. */
    public static String getCurrentName() {
        return currentName;
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Takes "testing123458@gmail.com" and returns "testing123458_1769977955840@gmail.com"
     * The timestamp makes it unique every single run — no collisions ever.
     */
    private String generateUniqueEmail(String baseEmail) {
        int atIndex = baseEmail.indexOf("@");
        String local = baseEmail.substring(0, atIndex);
        String domain = baseEmail.substring(atIndex); // includes the @
        return local + "_" + System.currentTimeMillis() + domain;
    }
}