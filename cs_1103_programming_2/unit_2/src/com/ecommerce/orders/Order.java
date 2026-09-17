package com.ecommerce.orders;

import com.ecommerce.Customer;
import com.ecommerce.Product;
import java.util.List;

/**
 * Represents an order placed by a customer.
 */
public class Order {
    private String orderID;
    private Customer customer;
    private List<Product> products;
    private double orderTotal;
    private String status;

    public Order(String orderID, Customer customer, List<Product> products) {
        this.orderID = orderID;
        this.customer = customer;
        this.products = products;
        this.status = "Pending";
        calculateOrderTotal();
    }

    private void calculateOrderTotal() {
        this.orderTotal = 0;
        for (Product product : products) {
            this.orderTotal += product.getPrice();
        }
    }

    public void updateStatus(String newStatus) {
        if (newStatus != null && !newStatus.trim().isEmpty()) {
            this.status = newStatus;
        }
    }

    /**
     * Generates a formatted summary of the order details.
     */
    public void generateOrderSummary() {
        System.out.println("\n--- Order Summary ---");
        System.out.println("Order ID: " + orderID);
        System.out.println("Customer: " + customer.getName());
        System.out.println("Status: " + status);
        System.out.println("Items:");
        for (Product p : products) {
            System.out.println(" - " + p.toString());
        }
        System.out.println("Total: $" + String.format("%.2f", orderTotal));
        System.out.println("---------------------\n");
    }
}