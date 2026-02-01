package payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CardInfo {

    @JsonProperty("nameOnCard")
    private String nameOnCard;

    @JsonProperty("cardNumber")
    private String cardNumber;

    @JsonProperty("cvc")
    private String cvc;

    @JsonProperty("expiryMonth")
    private String expiryMonth;

    @JsonProperty("expiryYear")
    private String expiryYear;

    // Default constructor for Jackson
    public CardInfo() {}

    public CardInfo(String nameOnCard, String cardNumber, String cvc, String expiryMonth, String expiryYear) {
        this.nameOnCard = nameOnCard;
        this.cardNumber = cardNumber;
        this.cvc = cvc;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
    }

    // Getters and Setters
    public String getNameOnCard() {
        return nameOnCard;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    @Override
    public String toString() {
        return "CardInfo{" +
                "nameOnCard='" + nameOnCard + '\'' +
                ", cardNumber='" + maskCardNumber(cardNumber) + '\'' +
                ", cvc='" + "***" + '\'' +
                ", expiryMonth='" + expiryMonth + '\'' +
                ", expiryYear='" + expiryYear + '\'' +
                '}';
    }

    /**
     * Mask card number for security (show only last 4 digits)
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "************" + cardNumber.substring(cardNumber.length() - 4);
    }
}