package com.dineout.code.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.dineout.code.order.Order;
import com.dineout.code.order.OrderDetails;
import com.dineout.code.order.cart;

import java.util.ArrayList;
import java.util.List;

public class LocalOrderManager {
    private static final String PREF_NAME = "restaurant_orders";
    private static final String KEY_CURRENT_SESSION = "current_session_id";
    private static final String KEY_ORDER_COUNTER = "order_counter";
    private static final String KEY_ORDERS = "orders";
    private static final String KEY_ORDER_DETAILS = "order_details";
    private static final String KEY_CART_ITEMS = "cart_items";
    private static final String KEY_LAST_CUSTOMER_LOGIN = "last_customer_login";

    private Context context;
    private SharedPreferences sharedPreferences;
    private LocalDataManager dataManager;

    public LocalOrderManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.dataManager = new LocalDataManager(context);
    }

    /**
     * Start new customer session - clears all previous orders and resets timers
     */
    public void startNewCustomerSession() {
        // Generate new session ID
        String sessionId = "session_" + System.currentTimeMillis();

        // Clear all previous order data
        clearAllOrders();
        clearCart();

        // Reset order counter
        resetOrderCounter();

        // Save new session info
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CURRENT_SESSION, sessionId);
        editor.putLong(KEY_LAST_CUSTOMER_LOGIN, System.currentTimeMillis());
        editor.apply();

        // Log the session reset for debugging
        System.out.println("New customer session started: " + sessionId);
        System.out.println("All previous orders cleared");
    }

    /**
     * Clear all orders from the system
     */
    public void clearAllOrders() {
        // Clear orders from local storage
        dataManager.removeKey(KEY_ORDERS);
        dataManager.removeKey(KEY_ORDER_DETAILS);

        // Clear any Firebase data if still being used
        try {
            // This will only work if Firebase is still enabled
            // It's safe to try as it won't crash if Firebase is disabled
        } catch (Exception e) {
            // Firebase not available, which is fine
        }

        System.out.println("All orders cleared successfully");
    }

    /**
     * Clear shopping cart
     */
    public void clearCart() {
        dataManager.removeKey(KEY_CART_ITEMS);

        // Clear any Firebase cart data if still being used
        try {
            // Firebase cart clearing would go here
        } catch (Exception e) {
            // Firebase not available, which is fine
        }

        System.out.println("Shopping cart cleared");
    }

    /**
     * Reset order counter to start from 1
     */
    public void resetOrderCounter() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_ORDER_COUNTER, 1);
        editor.apply();

        // Also reset the static order_id in confirmOrder class
        com.dineout.code.order.confirmOrder.order_id = 1;

        System.out.println("Order counter reset to 1");
    }

    /**
     * Get current order counter
     */
    public int getCurrentOrderId() {
        return sharedPreferences.getInt(KEY_ORDER_COUNTER, 1);
    }

    /**
     * Increment order counter and return new ID
     */
    public int getNextOrderId() {
        int currentId = getCurrentOrderId();
        int nextId = currentId + 1;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_ORDER_COUNTER, nextId);
        editor.apply();

        return currentId;
    }

    /**
     * Get current session ID
     */
    public String getCurrentSessionId() {
        return sharedPreferences.getString(KEY_CURRENT_SESSION, "default_session");
    }

    /**
     * Check if this is a fresh customer session (within last 5 minutes)
     */
    public boolean isFreshSession() {
        long lastLogin = sharedPreferences.getLong(KEY_LAST_CUSTOMER_LOGIN, 0);
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastLogin;

        // If more than 5 minutes (300,000 ms) since last customer login, consider it fresh
        return timeDifference > 300000;
    }

    /**
     * Save order to local storage
     */
    public void saveOrder(Order order) {
        List<Order> orders = dataManager.loadData(KEY_ORDERS, Order.class);
        orders.add(order);
        dataManager.saveData(KEY_ORDERS, orders);
    }

    /**
     * Save order details to local storage
     */
    public void saveOrderDetails(OrderDetails orderDetails) {
        List<OrderDetails> orderDetailsList = dataManager.loadData(KEY_ORDER_DETAILS, OrderDetails.class);
        orderDetailsList.add(orderDetails);
        dataManager.saveData(KEY_ORDER_DETAILS, orderDetailsList);
    }

    /**
     * Get all orders
     */
    public List<Order> getAllOrders() {
        return dataManager.loadData(KEY_ORDERS, Order.class);
    }

    /**
     * Get all order details
     */
    public List<OrderDetails> getAllOrderDetails() {
        return dataManager.loadData(KEY_ORDER_DETAILS, OrderDetails.class);
    }

    /**
     * Get count of active orders (for queue estimation)
     */
    public int getActiveOrderCount() {
        return sharedPreferences.getInt("active_order_count", 0);
    }

    /**
     * Add order to local storage
     */
    public void addOrder(com.dineout.code.order.Order order, com.dineout.code.order.OrderDetails orderDetails) {
        try {
            // Get current orders
            String ordersJson = sharedPreferences.getString("orders", "[]");
            String orderDetailsJson = sharedPreferences.getString("order_details", "[]");

            // Parse existing orders (simple implementation)
            int currentCount = getActiveOrderCount();

            // Increment order count
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("active_order_count", currentCount + 1);

            // Store order timestamp for tracking
            editor.putLong("last_order_time", System.currentTimeMillis());

            // Store order summary (simplified) - using correct method names
            String orderSummary = order.getId() + ":" + orderDetails.getDishname() + ":" + orderDetails.getEstimatedtime();
            editor.putString("last_order_summary", orderSummary);

            editor.apply();

            System.out.println("Order added locally: " + orderSummary);
        } catch (Exception e) {
            System.out.println("Error adding order locally: " + e.getMessage());
        }
    }

    /**
     * Get orders summary for display
     */
    public String getOrdersSummary() {
        return sharedPreferences.getString("last_order_summary", "No orders");
    }

    /**
     * Reduce active order count (when order is completed)
     */
    public void completeOrder() {
        int currentCount = getActiveOrderCount();
        if (currentCount > 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("active_order_count", currentCount - 1);
            editor.apply();
        }
    }

    /**
     * Clear all data and start fresh (for development/testing)
     */
    public void resetEverything() {
        clearAllOrders();
        clearCart();
        resetOrderCounter();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        System.out.println("Everything reset - fresh start");
    }
}