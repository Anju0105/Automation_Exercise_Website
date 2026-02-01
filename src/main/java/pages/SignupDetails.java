// SignupDetails.java - Page object for the full signup details form
package pages;

import org.openqa.selenium.By;
import utils.Actions;
import utils.jsonUtil;

public class SignupDetails {

    // JSON file path (make sure the file name matches exactly)
    private final String dataFile = "config/Signupdetails.json";

    // Title
    private final By titleMr  = By.id("id_gender1");
    private final By titleMrs = By.id("id_gender2");

    // Account info
    private final By passwordInput  = By.cssSelector("input[data-qa='password']");
    private final By daysDropdown   = By.cssSelector("select[data-qa='days']");
    private final By monthsDropdown = By.cssSelector("select[data-qa='months']");
    private final By yearsDropdown  = By.cssSelector("select[data-qa='years']");

    private final By newsletterCheckbox = By.id("newsletter");
    private final By offersCheckbox     = By.id("optin");

    // Address info
    private final By firstNameInput  = By.cssSelector("input[data-qa='first_name']");
    private final By lastNameInput   = By.cssSelector("input[data-qa='last_name']");
    private final By companyInput    = By.cssSelector("input[data-qa='company']");
    private final By address1Input   = By.cssSelector("input[data-qa='address']");
    private final By address2Input   = By.cssSelector("input[data-qa='address2']");
    private final By countryDropdown = By.cssSelector("select[data-qa='country']");
    private final By stateInput      = By.cssSelector("input[data-qa='state']");
    private final By cityInput       = By.cssSelector("input[data-qa='city']");
    private final By zipcodeInput    = By.cssSelector("input[data-qa='zipcode']");
    private final By mobileInput     = By.cssSelector("input[data-qa='mobile_number']");

    // Submit
    private final By createAccountBtn = By.cssSelector("button[data-qa='create-account']");

    // ----------------- Page Methods -----------------

    public void selectTitleFromJson() {
        String title = jsonUtil.getValue(dataFile, "Title");

        if (title.equalsIgnoreCase("Mr")) {
            Actions.click(titleMr);
        } else {
            Actions.click(titleMrs);
        }
    }

    public void enterPasswordFromJson() {
        Actions.sendKeys(passwordInput, jsonUtil.getValue(dataFile, "Password"));
    }

    public void selectDobFromJson() {
        Actions.selectByValue(daysDropdown,   jsonUtil.getValue(dataFile, "DOB_Day"));
        Actions.selectByValue(monthsDropdown, jsonUtil.getValue(dataFile, "DOB_Month"));
        Actions.selectByValue(yearsDropdown,  jsonUtil.getValue(dataFile, "DOB_Year"));
    }

    public void setNewsletterFromJson() {
        boolean newsletter = Boolean.parseBoolean(jsonUtil.getValue(dataFile, "Newsletter"));
        boolean offers     = Boolean.parseBoolean(jsonUtil.getValue(dataFile, "Offers"));

        Actions.setCheckbox(newsletterCheckbox, newsletter);
        Actions.setCheckbox(offersCheckbox, offers);
    }

    public void fillAddressFromJson() {
        Actions.sendKeys(firstNameInput, jsonUtil.getValue(dataFile, "First_name"));
        Actions.sendKeys(lastNameInput,  jsonUtil.getValue(dataFile, "Last_name"));
        Actions.sendKeys(companyInput,   jsonUtil.getValue(dataFile, "Company"));
        Actions.sendKeys(address1Input,  jsonUtil.getValue(dataFile, "Address"));
        Actions.sendKeys(address2Input,  jsonUtil.getValue(dataFile, "Address2"));

        Actions.selectByVisibleText(countryDropdown, jsonUtil.getValue(dataFile, "Country"));

        Actions.sendKeys(stateInput,   jsonUtil.getValue(dataFile, "State"));
        Actions.sendKeys(cityInput,    jsonUtil.getValue(dataFile, "City"));
        Actions.sendKeys(zipcodeInput, jsonUtil.getValue(dataFile, "Zipcode"));
        Actions.sendKeys(mobileInput,  jsonUtil.getValue(dataFile, "Mobile_Number"));
    }

    public void clickCreateAccount() {
        Actions.click(createAccountBtn);
    }

    // One full flow method
    public void completeSignupDetails() {
        selectTitleFromJson();
        enterPasswordFromJson();
        selectDobFromJson();
        setNewsletterFromJson();
        fillAddressFromJson();
        clickCreateAccount();
    }
}