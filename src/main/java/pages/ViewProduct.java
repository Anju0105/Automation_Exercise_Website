package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ViewProduct {

    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private By continueShoppingButton = By.xpath("//button[contains(text(),'Continue Shopping')]");
    private By modalDialog = By.className("modal-dialog");

    // Ad/overlay locators
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

    public ViewProduct(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Dismiss any ad or overlay blocking clicks.
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

    /**
     * Reliable base URL extraction.
     */
    private String getBaseUrl() {
        String url = driver.getCurrentUrl();
        int idx = url.indexOf("/", url.indexOf("//") + 2);
        return idx == -1 ? url : url.substring(0, idx);
    }

    /**
     * Scroll page back to the very top. Call this before every operation
     * so relative scroll doesn't accumulate across loop iterations.
     */
    private void scrollToTop() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
    }

    /**
     * Scroll down to products section (absolute 500px from top, not relative).
     */
    public void scrollToProducts() {
        try {
            // Always go to top first, THEN scroll down a fixed amount
            scrollToTop();
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 500);");
            System.out.println("Scrolled to products section");
        } catch (Exception e) {
            System.err.println("Error scrolling: " + e.getMessage());
        }
    }

    /**
     * Scroll to a specific element.
     */
    public void scrollToElement(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        } catch (Exception e) {
            System.err.println("Error scrolling to element: " + e.getMessage());
        }
    }

    /**
     * Wait for the product grid to be fully rendered on the page.
     * Call this after any navigation to /products before interacting with products.
     */
    private void waitForProductGridLoaded() {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("a.add-to-cart[data-product-id]")
        ));
    }

    /**
     * Add product to cart from the product listing page.
     */
    public void addProductToCart(String productId) {
        try {
            // Reset scroll so we start from a known position every time
            scrollToTop();
            dismissAds();

            By addToCartLocator = By.cssSelector("a.add-to-cart[data-product-id='" + productId + "']");

            // Scroll to the specific button, not just "down 500px"
            WebElement addToCartButton = wait.until(ExpectedConditions.presenceOfElementLocated(addToCartLocator));
            scrollToElement(addToCartButton);

            // Dismiss again after scroll (ad may have re-appeared)
            dismissAds();

            // Re-fetch — element may be stale after ad removal
            addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(addToCartLocator));
            addToCartButton.click();
            System.out.println("Product " + productId + " added to cart");

            // Wait for modal before handling it
            wait.until(ExpectedConditions.visibilityOfElementLocated(modalDialog));
            clickContinueShopping();

        } catch (Exception e) {
            System.err.println("Error adding product " + productId + " to cart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Add product to cart with specific quantity via the product detail page.
     */
    public void addProductWithQuantity(String productId, int quantity) {
        try {
            String baseUrl = getBaseUrl();
            String productUrl = baseUrl + "/product_details/" + productId;

            System.out.println("Navigating to: " + productUrl);
            driver.get(productUrl);

            // Confirm we actually landed on the correct product page
            wait.until(ExpectedConditions.urlContains("/product_details/" + productId));

            scrollToTop();
            dismissAds();

            // Find quantity input
            By quantityLocator = By.id("quantity");
            WebElement quantityInput = wait.until(ExpectedConditions.elementToBeClickable(quantityLocator));
            scrollToElement(quantityInput);

            dismissAds();
            quantityInput = wait.until(ExpectedConditions.elementToBeClickable(quantityLocator));
            quantityInput.clear();
            quantityInput.sendKeys(String.valueOf(quantity));
            System.out.println("Set quantity to: " + quantity);

            // Click add to cart
            By addToCartLocator = By.cssSelector("button.cart");
            WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(addToCartLocator));
            scrollToElement(addToCartButton);

            dismissAds();
            addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(addToCartLocator));
            addToCartButton.click();

            System.out.println("Product " + productId + " added with quantity: " + quantity);

            // Wait for modal before handling
            wait.until(ExpectedConditions.visibilityOfElementLocated(modalDialog));
            clickContinueShopping();

        } catch (Exception e) {
            System.err.println("Error adding product with quantity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Click continue shopping button in modal.
     */
    public void clickContinueShopping() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(modalDialog));
            dismissAds();

            WebElement continueButton = wait.until(
                    ExpectedConditions.elementToBeClickable(continueShoppingButton)
            );
            continueButton.click();
            System.out.println("Clicked Continue Shopping");

            // Wait for modal to fully disappear before anything else runs
            wait.until(ExpectedConditions.invisibilityOfElementLocated(modalDialog));

        } catch (Exception e) {
            System.err.println("Error clicking continue shopping: " + e.getMessage());
        }
    }

    /**
     * Increase quantity for a product already in cart.
     */
    public void increaseQuantityInCart(String productId, int newQuantity) {
        try {
            String baseUrl = getBaseUrl();
            driver.get(baseUrl + "/view_cart");
            wait.until(ExpectedConditions.urlContains("/view_cart"));

            scrollToTop();
            dismissAds();

            By quantityButtonLocator = By.cssSelector(
                    "button.cart_quantity_button[data-product-id='" + productId + "']");

            try {
                WebElement quantityButton = wait.until(
                        ExpectedConditions.elementToBeClickable(quantityButtonLocator));
                quantityButton.click();
            } catch (Exception e) {
                String xpath = "//td[contains(@class, 'cart_product')]" +
                        "//a[@href='/product_details/" + productId + "']" +
                        "/ancestor::tr//input[@class='cart_quantity_input']";
                WebElement quantityInput = wait.until(
                        ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
                quantityInput.clear();
                quantityInput.sendKeys(String.valueOf(newQuantity));
                quantityInput.submit();
            }

            System.out.println("Updated quantity for product " + productId + " to: " + newQuantity);

        } catch (Exception e) {
            System.err.println("Error updating quantity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Add multiple products from JSON config file (resource path).
     */
    public void addProductsFromJson(String jsonFileName, String baseUrl) {
        try {
            List<Product> products = ProductReader.readProductsFromJson(jsonFileName);

            if (products.isEmpty()) {
                System.err.println("No products found in JSON file!");
                return;
            }

            driver.get(baseUrl + "/products");
            wait.until(ExpectedConditions.urlContains("/products"));
            waitForProductGridLoaded();

            for (Product product : products) {
                System.out.println("\n--- Processing: " + product.getProductName() + " ---");

                if (product.isDetailPage() || product.getQuantity() > 1) {
                    addProductWithQuantity(product.getProductId(), product.getQuantity());

                    // Back to products — wait for grid to fully render before next iteration
                    driver.get(baseUrl + "/products");
                    wait.until(ExpectedConditions.urlContains("/products"));
                    waitForProductGridLoaded();
                } else {
                    addProductToCart(product.getProductId());
                }

                System.out.println("Successfully added: " + product.getProductName());
            }

            System.out.println("\n=== All products added successfully! ===");

        } catch (Exception e) {
            System.err.println("Error in addProductsFromJson: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Add multiple products from JSON file (full file path).
     */
    public void addProductsFromJsonFile(String filePath, String baseUrl) {
        try {
            List<Product> products = ProductReader.readProductsFromFilePath(filePath);

            if (products.isEmpty()) {
                System.err.println("No products found in JSON file!");
                return;
            }

            driver.get(baseUrl + "/products");
            wait.until(ExpectedConditions.urlContains("/products"));
            waitForProductGridLoaded();

            for (Product product : products) {
                System.out.println("\n--- Processing: " + product.getProductName() + " ---");

                if (product.isDetailPage() || product.getQuantity() > 1) {
                    addProductWithQuantity(product.getProductId(), product.getQuantity());

                    driver.get(baseUrl + "/products");
                    wait.until(ExpectedConditions.urlContains("/products"));
                    waitForProductGridLoaded();
                } else {
                    addProductToCart(product.getProductId());
                }

                System.out.println("Successfully added: " + product.getProductName());
            }

            System.out.println("\n=== All products added successfully! ===");

        } catch (Exception e) {
            System.err.println("Error in addProductsFromJsonFile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * View cart.
     */
    public void viewCart() {
        try {
            scrollToTop();
            dismissAds();

            WebElement cartLink = wait.until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Cart')]"))
            );
            cartLink.click();

            wait.until(ExpectedConditions.urlContains("/view_cart"));
            System.out.println("Navigated to cart page");
        } catch (Exception e) {
            System.err.println("Error viewing cart: " + e.getMessage());
        }
    }
}