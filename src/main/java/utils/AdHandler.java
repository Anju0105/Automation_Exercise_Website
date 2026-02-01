package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Centralized ad dismissal handler.
 *
 * Usage — call this ONE line before any click in your page objects:
 *      AdHandler.dismiss(driver);
 *
 * Handles:
 *   - Google Vignette ads (#google_vignette — the one in your screenshot)
 *   - Google interstitial ads (#gads_interstitial)
 *   - Any iframe-based Google ad (src contains googleads/pubads/doubleclick)
 *   - Generic popup/overlay ads
 *   - Fixed-position ad banners
 */
public class AdHandler {

    private static final By[] GENERIC_CLOSE_LOCATORS = {
            By.cssSelector(".close-btn"),
            By.cssSelector("button.close"),
            By.cssSelector("button[class*='close']"),
            By.xpath("//button[@aria-label='Close']"),
            By.xpath("//button[@aria-label='close']"),
            By.xpath("//button[contains(@class,'close')]"),
            By.cssSelector("div[class*='overlay'] .close"),
            By.cssSelector("div[class*='popup'] .close")
    };

    private AdHandler() {}

    /**
     * Main entry point. Detects ad type and handles it automatically.
     */
    public static void dismiss(WebDriver driver) {
        try {
            String url = driver.getCurrentUrl();

            if (url.contains("#google_vignette")) {
                System.out.println(">>> Google Vignette detected");
                handleGoogleVignette(driver);
                return;
            }

            if (url.contains("#gads_interstitial")) {
                System.out.println(">>> Google Interstitial detected");
                handleGoogleInterstitial(driver);
                return;
            }

            if (hasGoogleAdIframe(driver)) {
                System.out.println(">>> Stray Google ad iframe detected");
                removeAdIframes(driver);
                return;
            }

            // No Google ad — check for generic popups/overlays
            removeFixedOverlays(driver);
            dismissGenericPopups(driver);

        } catch (Exception e) {
            System.out.println("AdHandler warning: " + e.getMessage());
        }
    }

    // =========================================================================
    // GOOGLE VIGNETTE — this is exactly what your screenshot shows.
    // URL gets "#google_vignette" appended.
    // The ad lives inside an iframe. The X close button is INSIDE that iframe,
    // so we have to switchTo().frame() to reach it.
    // =========================================================================
    private static void handleGoogleVignette(WebDriver driver) {
        try {
            WebElement iframe = findGoogleAdIframe(driver);

            if (iframe != null) {
                driver.switchTo().frame(iframe);
                try {
                    boolean closed = clickCloseInsideIframe(driver);
                    if (!closed) {
                        // Can't find close button inside — just nuke the iframe
                        System.out.println("No close button inside iframe, removing it");
                        driver.switchTo().defaultContent();
                        removeAdIframes(driver);
                        stripUrlHash(driver);
                        return;
                    }
                } finally {
                    driver.switchTo().defaultContent();
                }
            } else {
                // No iframe found — might be a div-based vignette
                removeFixedOverlays(driver);
            }

            stripUrlHash(driver);

            // Confirm it's gone
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(d -> !d.getCurrentUrl().contains("#google_vignette"));
            System.out.println("✓ Google Vignette dismissed");

        } catch (Exception e) {
            System.out.println("Vignette handler error, forcing removal: " + e.getMessage());
            driver.switchTo().defaultContent();
            removeAdIframes(driver);
            stripUrlHash(driver);
        }
    }

    // =========================================================================
    // GOOGLE INTERSTITIAL — same structure, different hash
    // =========================================================================
    private static void handleGoogleInterstitial(WebDriver driver) {
        try {
            WebElement iframe = findGoogleAdIframe(driver);
            if (iframe != null) {
                driver.switchTo().frame(iframe);
                try {
                    clickCloseInsideIframe(driver);
                } finally {
                    driver.switchTo().defaultContent();
                }
            }
            removeAdIframes(driver);
            stripUrlHash(driver);
            System.out.println("✓ Google Interstitial dismissed");
        } catch (Exception e) {
            driver.switchTo().defaultContent();
            removeAdIframes(driver);
            stripUrlHash(driver);
        }
    }

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    /**
     * Try clicking the close/X button that's INSIDE a Google ad iframe.
     * Google uses different class names — we try them all.
     */
    private static boolean clickCloseInsideIframe(WebDriver driver) {
        By[] closeLocators = {
                By.cssSelector("div.closeButton"),
                By.cssSelector("#closeBtn"),
                By.cssSelector(".close-btn"),
                By.cssSelector("[data-action='close']"),
                By.cssSelector(".goog-ad-close"),
                By.xpath("//*[contains(@class,'close')]"),
                By.xpath("//*[contains(@id,'close')]"),
                By.xpath("//*[contains(@class,'Close')]")
        };

        for (By locator : closeLocators) {
            try {
                WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(2))
                        .until(ExpectedConditions.elementToBeClickable(locator));
                btn.click();
                System.out.println("✓ Clicked close inside iframe via: " + locator);
                return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    /**
     * Find a visible Google ad iframe on the page.
     */
    private static WebElement findGoogleAdIframe(WebDriver driver) {
        By[] iframeLocators = {
                By.cssSelector("iframe[src*='googleads']"),
                By.cssSelector("iframe[src*='pubads']"),
                By.cssSelector("iframe[src*='google']"),
                By.cssSelector("iframe[src*='gstatic']"),
                By.cssSelector("iframe[src*='doubleclick']"),
                By.cssSelector("iframe[id*='google']"),
                By.cssSelector("iframe[class*='google']")
        };

        for (By locator : iframeLocators) {
            try {
                List<WebElement> iframes = driver.findElements(locator);
                for (WebElement iframe : iframes) {
                    if (iframe.isDisplayed()) return iframe;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static boolean hasGoogleAdIframe(WebDriver driver) {
        return findGoogleAdIframe(driver) != null;
    }

    /**
     * Remove all Google ad iframes via JavaScript — nuclear option.
     */
    private static void removeAdIframes(WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll(" +
                            "    'iframe[src*=\"google\"]," +
                            "    iframe[src*=\"pubads\"]," +
                            "    iframe[src*=\"googleads\"]," +
                            "    iframe[src*=\"gstatic\"]," +
                            "    iframe[src*=\"doubleclick\"]," +
                            "    iframe[id*=\"google\"]," +
                            "    iframe[class*=\"google\"]" +
                            "').forEach(el => el.remove());"
            );
        } catch (Exception ignored) {}
    }

    /**
     * Remove fixed-position overlay divs.
     */
    private static void removeFixedOverlays(WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll(" +
                            "    'div[style*=\"position: fixed\"]," +
                            "    div[style*=\"position:fixed\"]," +
                            "    div[class*=\"ad-banner\"]," +
                            "    div[class*=\"ad-overlay\"]," +
                            "    div[id*=\"ad\"]," +
                            "    div[class*=\"popup\"]" +
                            "').forEach(el => el.remove());"
            );
        } catch (Exception ignored) {}
    }

    /**
     * Click close on generic (non-iframe) popup ads.
     */
    private static void dismissGenericPopups(WebDriver driver) {
        for (By locator : GENERIC_CLOSE_LOCATORS) {
            try {
                List<WebElement> buttons = driver.findElements(locator);
                for (WebElement btn : buttons) {
                    if (btn.isDisplayed()) {
                        btn.click();
                        System.out.println("Dismissed generic popup via: " + locator);
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * Strip #google_vignette or #gads_interstitial from URL
     * without triggering a full page reload.
     */
    private static void stripUrlHash(WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "if (window.location.hash) {" +
                            "    history.replaceState(null, '', window.location.pathname + window.location.search);" +
                            "}"
            );
        } catch (Exception ignored) {}
    }
}