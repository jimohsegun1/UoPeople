package com.ecommerce;

/**
 * Represents a product available for purchase in the e-commerce system.
 */
public class Product {
    private String productID;
    private String name;
    private double price;

    public Product(String productID, String name, double price) {
        this.productID = productID;
        this.name = name;
        setPrice(price); // Uses setter for validation
    }

    public String getProductID() {
        return productID;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    /**
     * Sets the product price with validation to prevent negative values.
     * 
     * @param price The cost of the product
     */
    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.price = price;
    }

    @Override
    public String toString() {
        return name + " ($" + String.format("%.2f", price) + ")";
    }
}