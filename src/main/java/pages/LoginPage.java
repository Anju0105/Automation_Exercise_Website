package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.FluentWait;
import utils.Actions;
import Browser_utils.DriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.time.Duration;

public class LoginPage {

    private final By emailInput    = By.cssSelector("input[data-qa='login-email']");
    private final By passwordInput = By.cssSelector("input[data-qa='login-password']");
    private final By loginButton   = By.cssSelector("button[data-qa='login-button']");

    // ✅ FIX: The site actually shows "Your email or password is incorrect!"
    // The old locator was checking for "Your email address and password do not match"
    // which NEVER matched — so the error was never detected and the script just hung.
    private final By loginErrorMsg = By.xpath(
            "//*[contains(text(),'Your email or password is incorrect')]");

    /** Tracks whether login failed this run — Tests.java checks this. */
    private static boolean loginFailed = false;

    /**
     * Login using the SAME email that Signup generated this run.
     * Does NOT read from login.json anymore — that email is stale
     * (it's from a previous run and may have been deleted).
     *
     * Password still comes from login.json because that's the password
     * the user set during signup details.
     */
    public void loginWithCredentials() {
        WebDriver driver = DriverManager.getDriver();
        loginFailed = false; // reset each attempt

        // Use the unique email Signup actually used this run
        String email    = pages.Signup.getCurrentEmail();
        String password = utils.jsonUtil.getValue("config/login.json", "password");

        if (email == null || email.isEmpty()) {
            throw new RuntimeException(
                    "LoginPage: No email from Signup — Signup.getCurrentEmail() returned null. " +
                            "Make sure fillSignupForm() ran before loginWithCredentials().");
        }

        System.out.println("Logging in with: " + email);

        Actions.waitForElementVisible(emailInput);

        WebElement emailEl = driver.findElement(emailInput);
        emailEl.clear();
        Actions.sendKeys(emailInput, email);

        WebElement passEl = driver.findElement(passwordInput);
        passEl.clear();
        Actions.sendKeys(passwordInput, password);

        Actions.click(loginButton);

        waitForLoginResponse();
    }

    /**
     * Polls until either:
     *   - Page navigates away from /login  →  login succeeded
     *   - Error message appears            →  login failed (sets flag, does NOT throw)
     *
     * We don't throw here anymore. Tests.java checks isLoginFailed() and
     * triggers re-signup if needed.
     */
    private void waitForLoginResponse() {
        WebDriver driver = DriverManager.getDriver();

        new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class)
                .until(d -> {
                    // Success: navigated away from /login
                    if (!d.getCurrentUrl().contains("/login")) {
                        System.out.println("✓ Login succeeded — navigated to: " + d.getCurrentUrl());
                        return true;
                    }

                    // Failure: site showed its error message
                    try {
                        d.findElement(loginErrorMsg);
                        // Found the error — mark it and stop polling
                        loginFailed = true;
                        System.out.println("✗ Login failed — site showed incorrect credentials error");
                        return true; // stop polling, don't throw
                    } catch (NoSuchElementException e) {
                        // Neither navigated nor error yet — keep polling
                        return false;
                    }
                });
    }

    /**
     * Returns true if the last login attempt failed.
     * Tests.java calls this to decide whether to re-signup.
     */
    public static boolean isLoginFailed() {
        return loginFailed;
    }
}