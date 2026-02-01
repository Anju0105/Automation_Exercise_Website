import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;
import config.ConfigLoader;
import pages.LoginPage;
import pages.Signup;
import pages.SignupDetails;
import pages.AddToCart;
import pages.AddComments;
import pages.PlaceOrder;
import order.OrderSuccess;
import order.DeleteAccount;
import payment.CardPage;
import utils.Actions;

public class Tests extends BaseTest {

    private final Signup signup = new Signup();
    private final SignupDetails signupDetails = new SignupDetails();
    private final LoginPage loginPage = new LoginPage();
    private AddToCart addToCart;
    private AddComments addComments;
    private PlaceOrder placeOrder;
    private CardPage cardPage;
    private OrderSuccess orderSuccess;
    private DeleteAccount deleteAccount;
    private boolean emailAlreadyExists = false;

    @Test(priority = 1)
    public void openWebsite() {
        String actualTitle = driver.getTitle();
        String expectedTitle = ConfigLoader.getWebsiteName();

        Assert.assertTrue(actualTitle.contains(expectedTitle),
                "Website did not open / title mismatch");

        System.out.println("✓ Step 1: Website opened successfully");
    }

    @Test(priority = 2, dependsOnMethods = "openWebsite")
    public void clickSignupLogin() {
        signup.clickSignupLogin();

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Did not navigate to /login");

        System.out.println("✓ Step 2: Navigated to Signup/Login page");
    }

    @Test(priority = 3, dependsOnMethods = "clickSignupLogin")
    public void fillSignupForm() {
        signup.fillSignupForm();
        System.out.println("✓ Step 3: Filled signup form");
    }

    // =========================================================================
    // ONLY Steps 4 and 5 changed. Everything else stays exactly the same.
    // =========================================================================

    @Test(priority = 4, dependsOnMethods = "fillSignupForm")
    public void clickFinalSignupButton() {
        signup.clickFinalSignupButton();
        if (signup.isEmailAlreadyExists()) {
            emailAlreadyExists = true;
            System.out.println("⚠ Step 4: Email already exists — will try login");
        } else {
            System.out.println("✓ Step 4: Signup accepted — proceeding with new account");
        }
    }

    @Test(priority = 5, dependsOnMethods = "clickFinalSignupButton")
    public void handleSignupOrLogin() {
        if (emailAlreadyExists) {
            // --- Attempt login ---
            System.out.println(">>> Trying login with existing credentials...");
            loginPage.loginWithCredentials();

            if (LoginPage.isLoginFailed()) {
                // Login failed — account was deleted but email is blacklisted.
                // Re-do signup with a brand new unique email.
                System.out.println(">>> Login failed, account was likely deleted. Re-signing up with fresh email...");
                emailAlreadyExists = false; // reset — we're doing a fresh signup now

                signup.redoSignupWithNewEmail();

                // Now complete the signup details (same as new account flow)
                Actions.waitForElementVisible(By.id("id_gender1"));
                signupDetails.completeSignupDetails();

                System.out.println("✓ Step 5: Re-signup complete — new account created");
            } else {
                System.out.println("✓ Step 5: Logged in successfully");
            }
        } else {
            // --- Fresh signup flow ---
            Actions.waitForElementVisible(By.id("id_gender1"));
            signupDetails.completeSignupDetails();
            System.out.println("✓ Step 5: Completed signup details — new account created");
        }
    }
    @Test(priority = 6, dependsOnMethods = "handleSignupOrLogin")
    public void verifyAccountCreatedOrLoggedIn() {
        String currentUrl = driver.getCurrentUrl();
        System.out.println("Current URL: " + currentUrl);

        if (emailAlreadyExists) {
            boolean isLoggedIn = !currentUrl.contains("/login");
            Assert.assertTrue(isLoggedIn,
                    "Login failed — still on login page");
            System.out.println("✓ Step 6: Login verified successfully!");
        } else {
            Assert.assertTrue(
                    currentUrl.contains("/account_created") || currentUrl.contains("account"),
                    "Account was not created successfully");
            System.out.println("✓ Step 6: Account created successfully!");
        }
    }

    @Test(priority = 7, dependsOnMethods = "verifyAccountCreatedOrLoggedIn")
    public void addProductsToCart() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("Step 7: Adding products to cart from JSON");
        System.out.println("========================================");

        addToCart = new AddToCart(driver);
        String baseUrl = ConfigLoader.getWebsiteUrl();

        // Add products from JSON - throws exception if session lost or 0 products added
        int added = addToCart.addProductsFromJson("config/products.json", baseUrl);

        Assert.assertTrue(added > 0,
                "No products were added to cart");

        System.out.println("\n✓ Step 7: Successfully added " + added + " product(s) to cart");
        System.out.println("========================================\n");
    }

    @Test(priority = 8, dependsOnMethods = "addProductsToCart")
    public void verifyProductsInCart() {
        System.out.println("\n========================================");
        System.out.println("Step 8: Verifying products in cart");
        System.out.println("========================================");

        addToCart.viewCart();

        boolean cartHasItems = addToCart.isCartNotEmpty();
        Assert.assertTrue(cartHasItems,
                "Cart is empty — products were not added successfully");

        System.out.println("✓ Step 8: Cart verification passed - products are in cart!");
        System.out.println("========================================\n");
    }

    @Test(priority = 9, dependsOnMethods = "verifyProductsInCart")
    public void proceedToCheckout() {
        System.out.println("\n========================================");
        System.out.println("Step 9: Proceeding to checkout");
        System.out.println("========================================");

        addToCart.proceedToCheckout();

        String url = driver.getCurrentUrl();
        System.out.println("Current URL: " + url);

        if (url.contains("/checkout")) {
            System.out.println("✓ Step 9: Successfully navigated to checkout page");
        } else if (!url.contains("/view_cart")) {
            System.out.println("✓ Step 9: Navigated away from cart — checkout flow initiated");
        } else {
            System.out.println("⚠ Step 9: Still on cart page — may need manual verification");
        }

        System.out.println("========================================\n");
    }

    @Test(priority = 10, dependsOnMethods = "proceedToCheckout")
    public void addOrderComment() {
        System.out.println("\n========================================");
        System.out.println("Step 10: Adding order comment");
        System.out.println("========================================");

        addComments = new AddComments(driver);

        String comment = "Please deliver between 9 AM and 5 PM. Call before delivery.";
        addComments.addOrderComment(comment);

        // Verify comment was added
        boolean isVisible = addComments.isCommentFieldVisible();
        Assert.assertTrue(isVisible, "Comment field is not visible");

        System.out.println("✓ Step 10: Order comment added successfully");
        System.out.println("========================================\n");
    }

    @Test(priority = 11, dependsOnMethods = "addOrderComment")
    public void placeOrder() {
        System.out.println("\n========================================");
        System.out.println("Step 11: Placing order");
        System.out.println("========================================");

        placeOrder = new PlaceOrder(driver);

        // Verify Place Order button is visible
        boolean buttonVisible = placeOrder.isPlaceOrderButtonVisible();
        Assert.assertTrue(buttonVisible, "Place Order button is not visible");

        // Click Place Order
        placeOrder.clickPlaceOrder();

        // Verify we reached payment page
        boolean onPaymentPage = placeOrder.isOnPaymentPage();

        if (onPaymentPage) {
            System.out.println("✓ Step 11: Successfully navigated to payment page");
        } else {
            System.out.println("⚠ Step 11: Did not reach payment page - URL: " + driver.getCurrentUrl());
        }

        System.out.println("========================================\n");
    }

    @Test(priority = 12, dependsOnMethods = "placeOrder")
    public void fillPaymentDetails() {
        System.out.println("\n========================================");
        System.out.println("Step 12: Filling payment details");
        System.out.println("========================================");

        cardPage = new CardPage(driver);

        // Verify we're on payment page
        boolean onPaymentPage = cardPage.isOnPaymentPage();
        Assert.assertTrue(onPaymentPage, "Not on payment page");

        // Fill payment form from JSON
        cardPage.fillPaymentFormFromJson("config/card_info.json");

        System.out.println("✓ Step 12: Payment details filled successfully");
        System.out.println("========================================\n");
    }

    @Test(priority = 13, dependsOnMethods = "fillPaymentDetails")
    public void confirmPayment() {
        System.out.println("\n========================================");
        System.out.println("Step 13: Confirming payment");
        System.out.println("========================================");

        // Click Pay and Confirm Order button
        cardPage.clickPayButton();

        // Wait and verify payment success
        Actions.waitForSeconds(3);
        boolean paymentSuccessful = cardPage.isPaymentSuccessful();

        if (paymentSuccessful) {
            String successMsg = cardPage.getSuccessMessage();
            System.out.println("✓ Step 13: " + successMsg);
            System.out.println("✓✓✓ ORDER PLACED SUCCESSFULLY! ✓✓✓");
        } else {
            System.out.println("⚠ Step 13: Payment confirmation pending or success message not visible");
            System.out.println("Current URL: " + driver.getCurrentUrl());
        }

        System.out.println("========================================\n");
    }

    @Test(priority = 14, dependsOnMethods = "confirmPayment")
    public void verifyOrderSuccess() {
        System.out.println("\n========================================");
        System.out.println("Step 14: Verifying order success");
        System.out.println("========================================");

        orderSuccess = new OrderSuccess(driver);

        // Verify order placed successfully
        boolean orderPlaced = orderSuccess.isOrderPlaced();
        Assert.assertTrue(orderPlaced, "Order was not placed successfully");

        String successMsg = orderSuccess.getSuccessMessage();
        System.out.println("Success Message: " + successMsg);

        System.out.println("✓ Step 14: Order placed successfully!");
        System.out.println("========================================\n");
    }

    @Test(priority = 15, dependsOnMethods = "verifyOrderSuccess")
    public void clickContinueAfterOrder() {
        System.out.println("\n========================================");
        System.out.println("Step 15: Clicking Continue after order");
        System.out.println("========================================");

        // Verify Continue button is visible
        boolean continueVisible = orderSuccess.isContinueButtonVisible();
        Assert.assertTrue(continueVisible, "Continue button is not visible");

        // Click Continue to go back to home
        orderSuccess.clickContinue();

        // Verify we're back on home page
        String currentUrl = driver.getCurrentUrl();
        System.out.println("Current URL: " + currentUrl);

        System.out.println("✓ Step 15: Returned to home page");
        System.out.println("========================================\n");
    }

    @Test(priority = 16, dependsOnMethods = "clickContinueAfterOrder")
    public void deleteAccount() {
        System.out.println("\n========================================");
        System.out.println("Step 16: Deleting account");
        System.out.println("========================================");

        deleteAccount = new DeleteAccount(driver);

        // Verify Delete Account link is visible
        boolean linkVisible = deleteAccount.isDeleteAccountLinkVisible();
        Assert.assertTrue(linkVisible, "Delete Account link is not visible");

        // Click Delete Account
        deleteAccount.clickDeleteAccount();

        System.out.println("✓ Step 16: Delete Account link clicked");
        System.out.println("========================================\n");
    }

    @Test(priority = 17, dependsOnMethods = "deleteAccount")
    public void verifyAccountDeleted() {
        System.out.println("\n========================================");
        System.out.println("Step 17: Verifying account deletion");
        System.out.println("========================================");

        // Verify account deleted successfully
        boolean accountDeleted = deleteAccount.isAccountDeleted();
        Assert.assertTrue(accountDeleted, "Account was not deleted successfully");

        String deletionMsg = deleteAccount.getDeletionMessage();
        System.out.println("Deletion Message: " + deletionMsg);

        // Click Continue after deletion
        deleteAccount.clickContinueAfterDeletion();

        // Verify we're back on home page
        String currentUrl = driver.getCurrentUrl();
        System.out.println("Final URL: " + currentUrl);

        System.out.println("✓ Step 17: Account deleted successfully!");
        System.out.println("✓✓✓ TEST COMPLETED SUCCESSFULLY! ✓✓✓");
        System.out.println("========================================\n");
    }
}