package com.dineout.code.order;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.dineout.R;
import com.dineout.code.data.LocalMenuManager;
import com.dineout.code.data.LocalOrderManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/*This class is responsible for showing pop up when Confirm Order is clicked by user in cart page*/

public class confirmOrder extends AppCompatDialogFragment {
    public static int order_id = 1;
    String dishName;
    private LocalMenuManager menuManager;
    private LocalOrderManager orderManager;

    //HARDCODED TABLEID
    int tableid = 4, servingSize;

    //data available locally on app when offline
    //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    FirebaseDatabase appeteaserDb = FirebaseDatabase.getInstance();
    final DatabaseReference appDb = FirebaseDatabase.getInstance().getReference("");

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        // Initialize local managers
        menuManager = new LocalMenuManager(getContext());
        orderManager = new LocalOrderManager(getContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.order_orderconfirmpopup,null);//specifies xml file of confirmation popup
        builder.setView(view);
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(confirmOrder.this.getActivity(), cartListView.class));
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Process cart items with local and Firebase fallback
                processCartItems();
            }
        });

        return builder.create();
    }

    private void processCartItems() {
        try {
            // First try Firebase method
            appDb.child("cart").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> dishNames = new ArrayList<>();
                    List<Integer> quantities = new ArrayList<>();

                    for (DataSnapshot dsCart : dataSnapshot.getChildren()) {
                        try {
                            cart c = dsCart.getValue(cart.class);
                            if (c != null) {
                                servingSize = c.getQuantity();
                                dishName = c.getAddedname();
                                dishNames.add(dishName);
                                quantities.add(servingSize);
                            }
                        } catch (Exception e) {
                            // If Firebase fails, use local cart data
                            useLocalCartData(dishNames, quantities);
                            return;
                        }
                    }

                    if (!dishNames.isEmpty()) {
                        // Calculate estimated time using local menu
                        int totalEstimatedTime = calculateEstimatedTime(dishNames, quantities);
                        createOrdersWithTime(dishNames, quantities, totalEstimatedTime);
                    } else {
                        // Fallback to local cart if Firebase cart is empty
                        useLocalCartData(dishNames, quantities);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("Firebase read failed, using local data: " + databaseError.getCode());
                    // Fallback to local cart data
                    List<String> dishNames = new ArrayList<>();
                    List<Integer> quantities = new ArrayList<>();
                    useLocalCartData(dishNames, quantities);
                }
            });
        } catch (Exception e) {
            // If Firebase completely fails, use local data
            List<String> dishNames = new ArrayList<>();
            List<Integer> quantities = new ArrayList<>();
            useLocalCartData(dishNames, quantities);
        }
    }

    private void useLocalCartData(List<String> dishNames, List<Integer> quantities) {
        // Get cart data from SharedPreferences (local cart system)
        SharedPreferences prefs = getContext().getSharedPreferences("cart_data", Context.MODE_PRIVATE);
        String cartItems = prefs.getString("cart_items", "");

        if (!cartItems.isEmpty()) {
            // Parse local cart data (simple format: "dishName1:qty1,dishName2:qty2")
            String[] items = cartItems.split(",");
            for (String item : items) {
                String[] parts = item.split(":");
                if (parts.length == 2) {
                    dishNames.add(parts[0]);
                    quantities.add(Integer.parseInt(parts[1]));
                }
            }
        } else {
            // If no cart data at all, use a sample order for testing
            dishNames.add("Grilled Chicken");
            quantities.add(1);
            Toast.makeText(getContext(), "No cart data found, using sample order", Toast.LENGTH_SHORT).show();
        }

        if (!dishNames.isEmpty()) {
            int totalEstimatedTime = calculateEstimatedTime(dishNames, quantities);
            createOrdersWithTime(dishNames, quantities, totalEstimatedTime);
        }
    }

    private int calculateEstimatedTime(List<String> dishNames, List<Integer> quantities) {
        int totalTime = 0;

        for (int i = 0; i < dishNames.size(); i++) {
            String dishName = dishNames.get(i);
            int quantity = quantities.get(i);

            // Get cooking time from local menu
            int cookingTime = menuManager.getCookingTimeByName(dishName);

            // Multiply by quantity (assuming parallel cooking reduces time)
            int dishTotalTime = cookingTime + (quantity - 1) * (cookingTime / 2);
            totalTime += dishTotalTime;
        }

        // Add base preparation time and consider kitchen queue
        totalTime += 5; // 5 minutes base prep time

        // Factor in existing orders in queue
        int existingOrders = orderManager.getActiveOrderCount();
        totalTime += existingOrders * 3; // 3 minutes delay per existing order

        return Math.max(totalTime, 10); // Minimum 10 minutes
    }

    private void createOrdersWithTime(List<String> dishNames, List<Integer> quantities, int estimatedTime) {
        try {
            // Debug: Print what we're creating orders for
            System.out.println("DEBUG: Creating orders for " + dishNames.size() + " different dishes:");
            for (int i = 0; i < dishNames.size(); i++) {
                System.out.println("  " + dishNames.get(i) + " x" + quantities.get(i));
            }

            // Create orders with calculated estimated time
            String ts = new Timestamp(System.currentTimeMillis()).toString();

            // IMPORTANT: Use the SAME order ID for all items in this order session
            String currentOrderId = Integer.toString(order_id);

            for (int i = 0; i < dishNames.size(); i++) {
                String currentDishName = dishNames.get(i);
                int currentQuantity = quantities.get(i);

                // Create Order with estimated time - all items share the same order ID
                Order ord = new Order(currentOrderId, Integer.toString(tableid), ts, estimatedTime);
                OrderDetails od = new OrderDetails(currentDishName, estimatedTime, currentOrderId, 0, currentQuantity, 0);

                // Try to save to Firebase
                try {
                    DatabaseReference addRow = appDb.child("Order").push();
                    addRow.setValue(ord);
                    addRow = appDb.child("OrderDetails").push();
                    addRow.setValue(od);

                    System.out.println("DEBUG: Saved to Firebase - " + currentDishName + " x" + currentQuantity + " for order " + currentOrderId);
                } catch (Exception e) {
                    System.out.println("Firebase save failed, using local storage: " + e.getMessage());
                }

                // Always save to local storage as backup
                orderManager.addOrder(ord, od);

                // IMPORTANT: Add order to kitchen system for immediate processing
                addOrderToKitchenSystem(currentDishName, estimatedTime, currentOrderId, currentQuantity);
            }

            // Only increment order_id ONCE after all items are processed
            order_id++;

            // Clear cart after successful order
            try {
                appDb.child("cart").removeValue();
                System.out.println("DEBUG: Cart cleared from Firebase");
            } catch (Exception e) {
                // Clear local cart
                SharedPreferences prefs = getContext().getSharedPreferences("cart_data", Context.MODE_PRIVATE);
                prefs.edit().clear().apply();
                System.out.println("DEBUG: Local cart cleared");
            }

            // Show success message with estimated time
            Toast.makeText(getContext(),
                    "Order confirmed! " + dishNames.size() + " items, Estimated time: " + estimatedTime + " minutes",
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Order failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            System.out.println("ERROR in createOrdersWithTime: " + e.getMessage());
            e.printStackTrace();
        }

        // Navigate to time page
        startActivity(new Intent(confirmOrder.this.getActivity(), Time.class));
    }

    /**
     * Add customer order directly to kitchen system for immediate processing
     */
    private void addOrderToKitchenSystem(String dishName, int estimatedTime, String orderId, int quantity) {
        try {
            // Create OrderDetailsDb for kitchen system
            // Constructor: OrderDetailsDb(String dishname, int estimatedtime, String orderid, int priority, int servings, int status)
            com.dineout.code.kitchen.models.OrderDetailsDb kitchenOrder =
                    new com.dineout.code.kitchen.models.OrderDetailsDb(
                            dishName,
                            estimatedTime,
                            orderId,
                            0, // normal priority
                            quantity,
                            0  // waiting status
                    );

            // Set a unique node ID for the order
            kitchenOrder.setNodeId("customer_" + orderId + "_" + System.currentTimeMillis());

            // Use the safe static method to add customer order to kitchen
            com.dineout.code.kitchen.MainActivity.addCustomerOrderToKitchen(kitchenOrder);

            System.out.println("Customer order sent to kitchen: " + dishName + " x" + quantity);

        } catch (Exception e) {
            System.out.println("Could not add order to kitchen system: " + e.getMessage());
            // This is not critical - the order is still saved to database
        }
    }
}
