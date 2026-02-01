package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AddToCart {

    private WebDriver driver;
    private WebDriverWait wait;

    private By continueShoppingButton = By.cssSelector("button.close-modal[data-dismiss='modal']");
    private By modalDialog = By.id("cartModal");
    private By cartLink = By.xpath("//a[contains(@href,'/view_cart')]");

    public AddToCart(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * Highlight element with black border - using Actions class style
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
     * Checks whether the current page shows a logged-in state.
     * automationexercise.com displays "Logged in as ..." in the header when authenticated.
     */
    public boolean isLoggedIn() {
        try {
            List<WebElement> loggedIn = driver.findElements(
                    By.xpath("//a[contains(text(),'Logged in as')]")
            );
            List<WebElement> signupLogin = driver.findElements(
                    By.xpath("//a[contains(text(),'Signup') or contains(text(),'Login')]")
            );

            System.out.println("  [session] loggedInIndicator=" + !loggedIn.isEmpty()
                    + " | signupLink=" + !signupLogin.isEmpty());

            if (!loggedIn.isEmpty()) return true;
            if (!signupLogin.isEmpty()) return false;

            // URL fallback
            String url = driver.getCurrentUrl();
            return !url.contains("/login") && !url.contains("/signup");

        } catch (Exception e) {
            System.err.println("  [session] check threw: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────
    // SCROLL HELPERS
    // ─────────────────────────────────────────────────────────

    public void scrollToAllProductsHeading() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebElement heading = driver.findElement(
                    By.xpath("//h2[contains(text(),'All Products')]")
            );
            js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'start'});", heading);
            Thread.sleep(800);
            js.executeScript("window.scrollBy(0,200)");
            Thread.sleep(600);
        } catch (Exception e) {
            System.err.println("  scrollToAllProductsHeading: " + e.getMessage());
        }
    }

    public void scrollToProduct(WebElement el) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", el);
            Thread.sleep(600);
        } catch (Exception e) {
            System.err.println("  scrollToProduct: " + e.getMessage());
        }
    }

    public void addProductToCart(String productId) throws InterruptedException {
        System.out.println("\n>>> [listing] product " + productId);

        scrollToAllProductsHeading();

        By loc = By.cssSelector("a.add-to-cart[data-product-id='" + productId + "']");
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(loc));
        scrollToProduct(btn);
        btn = wait.until(ExpectedConditions.elementToBeClickable(loc));

        // Highlight before clicking
        highlight(btn);

        // JS click — product-overlay div intercepts normal clicks
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", btn);

        System.out.println("  Clicked. Waiting for modal...");
        Thread.sleep(1500);

        clickContinueShopping();
        Thread.sleep(800);
    }

    // ─────────────────────────────────────────────────────────
    // ADD FROM DETAIL PAGE (qty >= 1)
    // ─────────────────────────────────────────────────────────

    public void addProductWithQuantity(String productId, int quantity) throws InterruptedException {
        System.out.println("\n>>> [detail] product " + productId + " qty=" + quantity);

        String currentUrl = driver.getCurrentUrl();
        String baseUrl = currentUrl.split("/")[0] + "//" + currentUrl.split("/")[2];
        driver.get(baseUrl + "/product_details/" + productId);
        Thread.sleep(2000);

        System.out.println("  URL=" + driver.getCurrentUrl());

        // Scroll directly to quantity input (NOT scrollToAllProductsHeading — that h2 doesn't exist here)
        WebElement qtyInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("quantity"))
        );
        scrollToProduct(qtyInput);
        Thread.sleep(400);

        // Highlight quantity input
        highlight(qtyInput);

        // Reliable clear + set
        qtyInput.click();
        Thread.sleep(150);
        qtyInput.sendKeys(Keys.CONTROL + "a");
        Thread.sleep(100);
        qtyInput.sendKeys(String.valueOf(quantity));
        Thread.sleep(400);

        // Verify
        String actual = qtyInput.getAttribute("value");
        System.out.println("  qty input reads: " + actual);

        if (!actual.equals(String.valueOf(quantity))) {
            System.out.println("  ⚠ Mismatch, retrying with JS...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    "var el=arguments[0]; el.value=arguments[1];" +
                            "el.dispatchEvent(new Event('input',{bubbles:true}));" +
                            "el.dispatchEvent(new Event('change',{bubbles:true}));",
                    qtyInput, String.valueOf(quantity)
            );
            Thread.sleep(300);
            actual = qtyInput.getAttribute("value");
            System.out.println("  After JS retry: " + actual);
        }

        // Click Add to cart
        WebElement addBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button.cart"))
        );
        scrollToProduct(addBtn);
        Thread.sleep(300);

        // Highlight Add to Cart button
        highlight(addBtn);

        addBtn.click();

        System.out.println("  Clicked. Waiting for modal...");
        Thread.sleep(1500);

        clickContinueShopping();
        Thread.sleep(800);
    }


    public void clickContinueShopping() {
        try {
            System.out.println("  Waiting for modal...");
            wait.until(ExpectedConditions.visibilityOfElementLocated(modalDialog));
            System.out.println("  ✓ Modal visible");
            Thread.sleep(500);

            WebElement btn;
            try {
                btn = wait.until(ExpectedConditions.elementToBeClickable(continueShoppingButton));
            } catch (Exception e) {
                btn = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'Continue Shopping')]")
                ));
            }

            // Highlight Continue Shopping button
            highlight(btn);

            btn.click();
            System.out.println("  ✓ Clicked Continue Shopping");

            wait.until(ExpectedConditions.invisibilityOfElementLocated(modalDialog));
            System.out.println("  ✓ Modal closed");

        } catch (Exception e) {
            System.err.println("  ✗ modal: " + e.getMessage());
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript(
                        "var b=document.querySelector('button.close-modal');" +
                                "if(b) b.click();" +
                                "else { var m=document.getElementById('cartModal'); if(m) m.style.display='none'; }"
                );
                Thread.sleep(600);
                System.out.println("  ✓ Modal closed via JS fallback");
            } catch (Exception e2) {
                System.err.println("  ✗ JS fallback: " + e2.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // MAIN LOOP — returns successCount, throws if 0 added
    // ─────────────────────────────────────────────────────────

    /**
     * Adds all products from the JSON file to cart.
     * @return number of products successfully added
     * @throws RuntimeException if session is lost OR zero products were added
     */
    public int addProductsFromJson(String jsonFileName, String baseUrl) throws InterruptedException {

        List<Product> products = ProductReader.readProductsFromJson(jsonFileName);
        if (products.isEmpty()) {
            throw new RuntimeException("No products in " + jsonFileName);
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("ADDING " + products.size() + " PRODUCTS");
        System.out.println("=".repeat(60));
        System.out.println("  Before nav — URL   : " + driver.getCurrentUrl());
        System.out.println("  Before nav — title : " + driver.getTitle());

        // Navigate to products page
        driver.get(baseUrl + "/products");
        Thread.sleep(2500);

        System.out.println("  After nav  — URL   : " + driver.getCurrentUrl());
        System.out.println("  After nav  — title : " + driver.getTitle());

        // ── SESSION CHECK ──
        if (!isLoggedIn()) {
            throw new RuntimeException(
                    "SESSION LOST: automationexercise.com dropped the login session " +
                            "before /products loaded. Re-login is needed before cart operations. " +
                            "URL was: " + driver.getCurrentUrl()
            );
        }
        System.out.println("  ✓ Session confirmed — logged in\n");

        int successCount = 0;
        int failCount   = 0;

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);

            System.out.println("\n" + "─".repeat(60));
            System.out.println("PRODUCT " + (i+1) + "/" + products.size()
                    + " | " + p.getProductName() + " | ID=" + p.getProductId() + " | qty=" + p.getQuantity());
            System.out.println("─".repeat(60));

            try {
                if (p.isDetailPage() || p.getQuantity() > 1) {
                    addProductWithQuantity(p.getProductId(), p.getQuantity());

                    // Back to listing for next iteration
                    driver.get(baseUrl + "/products");
                    Thread.sleep(2000);
                } else {
                    addProductToCart(p.getProductId());
                }

                System.out.println("✓✓✓ " + p.getProductName() + " added");
                successCount++;

            } catch (Exception e) {
                System.err.println("✗✗✗ " + p.getProductName() + " FAILED: " + e.getMessage());
                failCount++;
            }
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("DONE | success=" + successCount + " | failed=" + failCount);
        System.out.println("=".repeat(60) + "\n");

        if (successCount == 0) {
            throw new RuntimeException(
                    "All " + failCount + " product additions failed. Cart is empty."
            );
        }

        return successCount;
    }

    // ─────────────────────────────────────────────────────────
    // VIEW CART
    // ─────────────────────────────────────────────────────────

    public void viewCart() {
        try {
            System.out.println("\n>>> Navigating to cart...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0,0)");
            Thread.sleep(600);

            try {
                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(cartLink));

                // Highlight cart link
                highlight(btn);

                btn.click();
                System.out.println("  ✓ cart (click)");
            } catch (Exception e) {
                String url = driver.getCurrentUrl();
                String base = url.split("/")[0] + "//" + url.split("/")[2];
                driver.get(base + "/view_cart");
                System.out.println("  ✓ cart (direct URL)");
            }
            Thread.sleep(1500);
        } catch (Exception e) {
            System.err.println("  ✗ viewCart: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // VERIFY CART
    // ─────────────────────────────────────────────────────────

    public boolean isCartNotEmpty() {
        try {
            Thread.sleep(1000);

            List<WebElement> rows = driver.findElements(
                    By.cssSelector("#cart_info_table tbody tr")
            );
            rows = rows.stream()
                    .filter(r -> {
                        String c = r.getAttribute("class");
                        return c == null || !c.contains("cart_menu");
                    })
                    .toList();

            System.out.println("\n>>> Cart Verification");
            System.out.println("  Items   : " + rows.size());
            System.out.println("  URL     : " + driver.getCurrentUrl());
            System.out.println("  Title   : " + driver.getTitle());
            System.out.println("  LoggedIn: " + isLoggedIn());

            for (WebElement row : rows) {
                try {
                    String name  = row.findElement(By.cssSelector("td.cart_description h4 a")).getText();
                    String qty   = row.findElement(By.cssSelector("td.cart_quantity button")).getText().trim();
                    String total = row.findElement(By.cssSelector("td.cart_total p")).getText();
                    System.out.println("    → " + name + " | qty=" + qty + " | " + total);
                } catch (Exception ignored) {}
            }

            return rows.size() > 0;

        } catch (Exception e) {
            System.err.println("  ✗ isCartNotEmpty: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────
    // PROCEED TO CHECKOUT
    // ─────────────────────────────────────────────────────────

    public void proceedToCheckout() {
        try {
            System.out.println("\n>>> Proceed To Checkout");

            WebElement btn = wait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector("a.check_out"))
            );
            scrollToProduct(btn);
            Thread.sleep(300);

            // Highlight checkout button
            highlight(btn);

            btn.click();
            System.out.println("  ✓ Clicked");

            Thread.sleep(2000);

            // Checkout modal fires if not logged in
            try {
                By modal = By.id("checkoutModal");
                wait.until(ExpectedConditions.visibilityOfElementLocated(modal));
                System.out.println("  ⚠ Checkout modal appeared");

                WebElement dismiss = wait.until(
                        ExpectedConditions.elementToBeClickable(
                                By.cssSelector("button.close-checkout-modal[data-dismiss='modal']")
                        )
                );

                // Highlight modal dismiss button
                highlight(dismiss);

                dismiss.click();
                wait.until(ExpectedConditions.invisibilityOfElementLocated(modal));
                System.out.println("  ✓ Dismissed");
            } catch (Exception e) {
                System.out.println("  ✓ No modal — checkout proceeding");
            }

            System.out.println("  URL: " + driver.getCurrentUrl());

        } catch (Exception e) {
            System.err.println("  ✗ proceedToCheckout: " + e.getMessage());
            e.printStackTrace();
        }
    }
}