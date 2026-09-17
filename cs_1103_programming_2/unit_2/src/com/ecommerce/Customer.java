package com.ecommerce;

import com.ecommerce.orders.Order;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer and manages their shopping cart operations.
 */
public class Customer {
    private String customerID;
    private String name;
    private List<Product> shoppingCart;

    public Customer(String customerID, String name) {
        this.customerID = customerID;
        this.name = name;
        this.shoppingCart = new ArrayList<>();
    }

    public String getCustomerID() {
        return customerID;
    }

    public String getName() {
        return name;
    }

    public void addProductToCart(Product product) {
        if (product != null) {
            shoppingCart.add(product);
            System.out.println(product.getName() + " added to cart.");
        }
    }

    public void removeProductFromCart(Product product) {
        if (shoppingCart.remove(product)) {
            System.out.println(product.getName() + " removed from cart.");
        }
    }

    public double calculateTotalCost() {
        double total = 0;
        for (Product product : shoppingCart) {
            total += product.getPrice();
        }
        return total;
    }

    /**
     * Places an order using the current shopping cart contents.
     * 
     * @param orderID The unique identifier for the new order
     * @return The generated Order object
     */
    public Order placeOrder(String orderID) {
        if (shoppingCart.isEmpty()) {
            throw new IllegalStateException("Cannot place order: Cart is empty.");
        }
        Order newOrder = new Order(orderID, this, new ArrayList<>(shoppingCart));
        shoppingCart.clear(); // Empty cart after successful order
        return newOrder;
    }
}