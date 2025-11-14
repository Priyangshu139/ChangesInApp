package com.dineout.code.data;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Utility class to clear Firebase data when Firebase is still enabled
 * This provides a bridge between local authentication and existing Firebase functionality
 */
public class FirebaseDataCleaner {

    private static final String TAG = "FirebaseDataCleaner";
    private Context context;

    public FirebaseDataCleaner(Context context) {
        this.context = context;
    }

    /**
     * Clear all order-related data from Firebase (if available)
     */
    public void clearFirebaseOrders() {
        try {
            // Try to clear Firebase data if Firebase is still enabled
            com.google.firebase.database.DatabaseReference ref =
                    com.google.firebase.database.FirebaseDatabase.getInstance().getReference();

            // Clear orders
            ref.child("Order").removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Firebase orders cleared successfully");
                } else {
                    Log.w(TAG, "Failed to clear Firebase orders", task.getException());
                }
            });

            // Clear order details
            ref.child("OrderDetails").removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Firebase order details cleared successfully");
                } else {
                    Log.w(TAG, "Failed to clear Firebase order details", task.getException());
                }
            });

            // Clear cart
            ref.child("cart").removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Firebase cart cleared successfully");
                } else {
                    Log.w(TAG, "Failed to clear Firebase cart", task.getException());
                }
            });

            Log.d(TAG, "Firebase order clearing initiated");

        } catch (Exception e) {
            // Firebase not available or not configured - this is fine
            Log.d(TAG, "Firebase not available for clearing orders: " + e.getMessage());
        }
    }

    /**
     * Reset Firebase order IDs and counters (if available)
     */
    public void resetFirebaseCounters() {
        try {
            com.google.firebase.database.DatabaseReference ref =
                    com.google.firebase.database.FirebaseDatabase.getInstance().getReference();

            // Reset order ID counter
            ref.child("Ids").child("Orderid").setValue(1);

            // Reset other ID counters if needed
            ref.child("Ids").child("Employeeid").setValue(1);
            ref.child("Ids").child("Tableid").setValue(1);

            Log.d(TAG, "Firebase counters reset");

        } catch (Exception e) {
            Log.d(TAG, "Firebase not available for resetting counters: " + e.getMessage());
        }
    }

    /**
     * Clear all Firebase data for fresh start
     */
    public void clearAllFirebaseData() {
        clearFirebaseOrders();
        resetFirebaseCounters();
    }

    /**
     * Clear Firebase data with user feedback
     */
    public void clearFirebaseDataWithFeedback() {
        try {
            clearAllFirebaseData();
            Toast.makeText(context, "All previous data cleared - fresh start!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Firebase not available - show local-only message
            Toast.makeText(context, "Starting fresh session (local mode)", Toast.LENGTH_SHORT).show();
        }
    }
}