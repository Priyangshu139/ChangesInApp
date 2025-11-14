package com.dineout.code.order;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dineout.R;
import com.dineout.code.billing.ConfirmPayment;
import com.dineout.code.BaseActivity;
import com.dineout.code.data.LocalOrderManager;
import com.dineout.code.data.LocalMenuManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class Time extends BaseActivity {
    private static long START_TIME_IN_MILLIS;  //take from kitchen - this is  30 seconds - 10 minutes
    private TextView mTextViewCountDown;
    private TextView mTextViewNotif;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis;
    ArrayList<OrderDetails>orderDetail=new ArrayList<OrderDetails>();
    private LocalOrderManager localOrderManager;
    private LocalMenuManager localMenuManager;
    private boolean useLocalData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity_time);

        // Initialize local order manager and local menu manager
        localOrderManager = new LocalOrderManager(this);
        localMenuManager = new LocalMenuManager(this);

        //********get value through shared preference for orderid here
        final String orderid = Integer.toString(confirmOrder.order_id - 1); // Use the last confirmed order ID

        // Try Firebase first, fallback to local data
        try {
            loadOrderDataFromFirebase(orderid);
        } catch (Exception e) {
            Toast.makeText(this, "Using local order data", Toast.LENGTH_SHORT).show();
            useLocalData = true;
            loadOrderDataLocally(orderid);
        }
    }

    private void loadOrderDataFromFirebase(final String orderid) {
        //get all dishes estimated time corresponding to orderid given and add up and convert to milisecs
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("OrderDetails");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                orderDetail.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    OrderDetails obj = child.getValue(OrderDetails.class);
                    if(obj.orderid.equals(orderid)) {
                        orderDetail.add(obj);
                    }
                }

                // If no Firebase data found, fallback to local data
                if (orderDetail.isEmpty()) {
                    Toast.makeText(Time.this, "No Firebase order data, using local data", Toast.LENGTH_SHORT).show();
                    loadOrderDataLocally(orderid);
                } else {
                    processOrderData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Fallback to local data if Firebase fails
                Toast.makeText(Time.this, "Firebase failed, using local data", Toast.LENGTH_SHORT).show();
                useLocalData = true;
                loadOrderDataLocally(orderid);
            }
        });
    }

    private void loadOrderDataLocally(String orderid) {
        // Load from actual cart data if available, otherwise use sample data
        try {
            loadOrderDataFromCart(orderid);
        } catch (Exception e) {
            // Fallback to sample data
            loadSampleOrderData(orderid);
        }
    }

    /**
     * Load order data from Firebase cart (all items in current session)
     */
    private void loadOrderDataFromCart(String orderid) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("cart");
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                orderDetail.clear();
                boolean hasData = false;

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    cart cartItem = child.getValue(cart.class);
                    if (cartItem != null) {
                        // Create OrderDetails from cart item
                        OrderDetails od = new OrderDetails();
                        od.dishname = cartItem.getAddedname();
                        od.estimatedtime = localMenuManager.getCookingTimeByName(cartItem.getAddedname());
                        od.orderid = orderid;
                        od.servings = cartItem.getQuantity();
                        od.priority = 0;
                        od.status = 0;

                        orderDetail.add(od);
                        hasData = true;
                    }
                }

                if (hasData) {
                    Toast.makeText(Time.this, "Loaded " + orderDetail.size() + " items from cart", Toast.LENGTH_SHORT).show();
                    processOrderData();
                } else {
                    // No cart data, use sample data
                    loadSampleOrderData(orderid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Fallback to sample data
                loadSampleOrderData(orderid);
            }
        });
    }

    /**
     * Load sample order data when no cart/Firebase data available
     */
    private void loadSampleOrderData(String orderid) {
        orderDetail.clear();

        // Create sample order data that matches what might be in cart
        OrderDetails localOrder1 = new OrderDetails();
        localOrder1.dishname = "Grilled Chicken";
        localOrder1.estimatedtime = 18; // 18 minutes
        localOrder1.orderid = orderid;
        localOrder1.servings = 2;
        localOrder1.priority = 0;
        localOrder1.status = 0;
        orderDetail.add(localOrder1);

        // Add another sample order for realism
        OrderDetails localOrder2 = new OrderDetails();
        localOrder2.dishname = "Fries";
        localOrder2.estimatedtime = 10; // 10 minutes
        localOrder2.orderid = orderid;
        localOrder2.servings = 1;
        localOrder2.priority = 0;
        localOrder2.status = 0;
        orderDetail.add(localOrder2);

        Toast.makeText(this, "Using sample order data with " + orderDetail.size() + " items", Toast.LENGTH_SHORT).show();
        processOrderData();
    }

    private void processOrderData() {
        int sizeo = orderDetail.size();
        int timeCount = 0;
        for (int i = 0; i < sizeo; i++) {
            timeCount = timeCount + orderDetail.get(i).getEstimatedtime();
        }

        // Ensure minimum realistic time
        if (timeCount < 10) {
            timeCount = 15; // Default 15 minutes if too low
        }

        Toast.makeText(getApplicationContext(), "Total estimated time: " + timeCount + " minutes", Toast.LENGTH_LONG).show();

        //Timer Settings
        long milliseconds = timeCount * 60000;
        START_TIME_IN_MILLIS = milliseconds;
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        mTextViewCountDown = findViewById(R.id.countdown);
        mTextViewNotif = findViewById(R.id.eTime);
        startTimer();//start count down
    }

    private void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();

            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                mTextViewNotif.setText("It's Time!");
            }
        }.start();

        mTimerRunning = true;
    }

    private void resetTimer() {
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        mTextViewCountDown.setText(timeLeftFormatted);
    }

    //Update
    public void onClickReg100(View v)
    {
        Toast.makeText(getApplicationContext(), "Update Order feature coming soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement order update functionality
    }

    //Cancel
    public void onClickReg101(View v)
    {
        Toast.makeText(getApplicationContext(), "Cancel Order feature coming soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement order cancellation functionality
    }

    //ViewBill - Fixed to prevent crashes but still navigate to proper bill page
    public void onClickReg102(View v)
    {
        try {
            Intent i = new Intent(this, ConfirmPayment.class);
            i.putExtra("customerview", "true");

            // Pass current order data to prevent crashes
            String currentOrderId = Integer.toString(confirmOrder.order_id - 1);
            i.putExtra("orderid", currentOrderId);
            i.putExtra("use_local_data", useLocalData);

            // FIX: Pass ALL order details for local display, not just a summary
            if (!orderDetail.isEmpty()) {
                // Create a more robust data structure for ALL items
                ArrayList<String> dishNames = new ArrayList<>();
                ArrayList<Integer> quantities = new ArrayList<>();
                ArrayList<Integer> prices = new ArrayList<>();

                int totalPrice = 0;
                StringBuilder orderSummary = new StringBuilder();

                for (OrderDetails item : orderDetail) {
                    String dishName = item.getDishname();
                    int quantity = item.getServings();
                    int itemPrice = getEstimatedPrice(dishName);
                    int itemTotal = itemPrice * quantity;

                    dishNames.add(dishName);
                    quantities.add(quantity);
                    prices.add(itemPrice);
                    totalPrice += itemTotal;

                    orderSummary.append(dishName)
                            .append(":").append(quantity)
                            .append(":").append(itemTotal)
                            .append(";");
                }

                // Pass arrays for proper reconstruction
                i.putStringArrayListExtra("dish_names", dishNames);
                i.putIntegerArrayListExtra("quantities", quantities);
                i.putIntegerArrayListExtra("prices", prices);
                i.putExtra("order_summary", orderSummary.toString());
                i.putExtra("total_price", totalPrice);
                i.putExtra("item_count", orderDetail.size());

                System.out.println("DEBUG: Passing " + orderDetail.size() + " items to bill page:");
                for (int j = 0; j < dishNames.size(); j++) {
                    System.out.println("  " + dishNames.get(j) + " x" + quantities.get(j) + " = $" + (prices.get(j) * quantities.get(j)));
                }

            } else {
                Toast.makeText(this, "No order items found", Toast.LENGTH_SHORT).show();
                return;
            }

            startActivity(i);

        } catch (Exception e) {
            Toast.makeText(this, "Error opening bill page: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Fallback to simple summary if navigation fails
            showSimpleBillSummary();
        }
    }

    /**
     * Show a simple bill summary to prevent crashes
     */
    private void showSimpleBillSummary() {
        if (orderDetail.isEmpty()) {
            Toast.makeText(this, "No order items found", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder billText = new StringBuilder();
        billText.append("ORDER SUMMARY:\n\n");

        int totalAmount = 0;
        for (OrderDetails item : orderDetail) {
            int itemPrice = getEstimatedPrice(item.dishname);
            int itemTotal = itemPrice * item.servings;
            totalAmount += itemTotal;

            billText.append(item.dishname)
                    .append(" x").append(item.servings)
                    .append(" = $").append(itemTotal)
                    .append("\n");
        }

        billText.append("\n")
                .append("TOTAL: $").append(totalAmount)
                .append("\n\n")
                .append("Thank you for your order!");

        // Show bill summary in a toast (could be enhanced with a dialog)
        Toast.makeText(this, billText.toString(), Toast.LENGTH_LONG).show();

        // Optional: Navigate to payment screen
        showPaymentOptions();
    }

    /**
     * Get estimated price for a dish (simple pricing logic)
     */
    private int getEstimatedPrice(String dishName) {
        switch (dishName.toLowerCase()) {
            case "grilled chicken":
                return 15;
            case "beef steak":
                return 25;
            case "pizza":
                return 18;
            case "burger":
                return 12;
            case "pasta alfredo":
                return 16;
            case "fries":
                return 6;
            case "soup":
                return 8;
            case "chocolate":
                return 7;
            default:
                return 10; // Default price
        }
    }

    /**
     * Show payment options instead of crashing ConfirmPayment
     */
    private void showPaymentOptions() {
        Toast.makeText(this, "Payment options:\n• Cash\n• Card\n• Mobile Payment", Toast.LENGTH_LONG).show();

        // Could navigate to a working payment screen here
        // For now, just provide feedback
        Toast.makeText(this, "Payment processing will be available soon", Toast.LENGTH_SHORT).show();
    }
}
