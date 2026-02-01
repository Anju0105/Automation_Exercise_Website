package pages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Product {

    @JsonProperty("productId")
    private String productId;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("price")
    private String price;

    @JsonProperty("isDetailPage")
    private boolean isDetailPage;

    // Default constructor required by Jackson
    public Product() {
        this.isDetailPage = false;
    }

    public Product(String productId, String productName, int quantity, String price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.isDetailPage = false;
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public boolean isDetailPage() {
        return isDetailPage;
    }

    public void setDetailPage(boolean detailPage) {
        isDetailPage = detailPage;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", price='" + price + '\'' +
                ", isDetailPage=" + isDetailPage +
                '}';
    }
}