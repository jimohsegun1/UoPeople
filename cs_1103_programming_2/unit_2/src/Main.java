import com.ecommerce.Product;
import com.ecommerce.Customer;
import com.ecommerce.orders.Order;

/**
 * Main application class to demonstrate e-commerce functionality.
 */
public class Main {
    public static void main(String[] args) {
        try {
            // 1. Create instances of products
            Product laptop = new Product("P001", "Developer Laptop", 1299.99);
            Product mouse = new Product("P002", "Wireless Mouse", 49.50);
            Product keyboard = new Product("P003", "Mechanical Keyboard", 110.00);

            // 2. Create a customer
            Customer customer = new Customer("C001", "Jane Doe");

            System.out.println("Welcome, " + customer.getName() + "!");

            // 3. Customer browses and adds items to the shopping cart
            customer.addProductToCart(laptop);
            customer.addProductToCart(mouse);
            customer.addProductToCart(keyboard);
            
            // Remove an item to demonstrate functionality
            customer.removeProductFromCart(mouse);

            System.out.println("Current Cart Total: $" 
                + String.format("%.2f", customer.calculateTotalCost()));

            // 4. Customer places an order
            Order customerOrder = customer.placeOrder("ORD-98765");

            // 5. Update order status and display information
            customerOrder.updateStatus("Processing");
            customerOrder.generateOrderSummary();

            // 6. Demonstrate Error Handling (Trying to order with an empty cart)
            System.out.println("Attempting to place an order with an empty cart...");
            customer.placeOrder("ORD-98766");

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error encountered: " + e.getMessage());
        }
    }
}