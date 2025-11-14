package com.dineout.code.billing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dineout.R;
import com.dineout.code.BaseActivity;
import com.dineout.code.order.confirmOrder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.String.valueOf;

public class ConfirmPayment extends BaseActivity {
    private RecyclerView mRecyclerView;
    private BillAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private RecyclerView mRecyclerView2;
    private BillAdapter mAdapter2;
    private RecyclerView.LayoutManager mLayoutManager2;

    private OrderBill o;

    String oid;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference ref = db.getReference();

    HashMap<String, DishPrice> prices = new HashMap<>();

    DatabaseReference refOrder;
    DatabaseReference refMenu;
    DatabaseReference refOrderDetails;

    ArrayList<String> keys;
    ArrayList<DishOrder> splitlist;

    Context c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = this;

        splitlist = new ArrayList<>();
        keys = new ArrayList<>();

        setContentView(R.layout.billing_activity_confirm_payment);

        // get views
        TextView orderid = (TextView) findViewById(R.id.orderid_confirm);
        TextView table = (TextView) findViewById(R.id.tableid_confirm);
        TextView time = (TextView) findViewById(R.id.time_confirm);
        TextView total = (TextView) findViewById(R.id.total_bill1);
        Button confirm = (Button) findViewById(R.id.confirm_payment);
        Button makePayment = (Button) findViewById(R.id.make_payment_button);

        confirm.setOnClickListener(onConfirmClick);
        if (makePayment != null) {
            makePayment.setOnClickListener(onMakePaymentClick);
        }

        o = new OrderBill();
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            o = (OrderBill) extras.getSerializable("order");

            // Check if this is a customer view with local data
            boolean useLocalData = extras.getBoolean("use_local_data", false);
            String customOrderId = extras.getString("orderid");
            String orderSummary = extras.getString("order_summary");
            int totalPrice = extras.getInt("total_price", 0);

            //Show bill to customer
            if (o == null) {
                if (useLocalData && orderSummary != null) {
                    // Use local data passed from Time activity
                    populateFromLocalData(customOrderId, orderSummary, totalPrice);
                } else {
                    // Try original Firebase method
                    oid = customOrderId != null ? customOrderId : Integer.toString(confirmOrder.order_id);
                    populateForCustomer(oid);
                }
            }

            //Show bill to hall manager
            else if (o.id != null) {
                oid = o.id;
                orderid.setText("Order ID: " + o.id);
                table.setText("Table: " + o.table);
                time.setText("Time: " + o.time);
                total.setText("Total Price: " + o.getTotal());
                populateForManager();
            }
        } else {
            // Fallback when no extras provided
            oid = Integer.toString(confirmOrder.order_id);
            populateForCustomer(oid);
        }
    }


    public void onFeedbackClick(View v) {

        //check if the bill of that order has been paid, only then allow the customer to give feedback
        //here the customer is being allowed to give feedback even if the bill has not been paid yet

        Intent i = new Intent(c, Feedback.class);
        i.putExtra("orderid", o.id);
        i.putExtra("order", o);
        startActivity(i);
    }


    //Populates the view to let customer view his order and give feedback. (Gets data from firebase)
    public void populateForCustomer(String id) {
        findViewById(R.id.confirm_payment).setVisibility(View.INVISIBLE);
        findViewById(R.id.payment_feedback).setVisibility(View.VISIBLE);

        refMenu = ref.child("Menu");
        refOrder = ref.child("Order");
        refOrderDetails = ref.child("OrderDetails");

        final TextView orderid = (TextView) findViewById(R.id.orderid_confirm);
        final TextView table = (TextView) findViewById(R.id.tableid_confirm);
        final TextView time = (TextView) findViewById(R.id.time_confirm);
        final TextView total = (TextView) findViewById(R.id.total_bill1);

        //Almost same as refresh() function in PendingPayments but doesnt get the data for each order.
        //Only for the orderid that the customer ui sends to this activity

        refMenu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated
                Log.e("Price", valueOf(dataSnapshot.getChildrenCount()));
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    DishPrice post = postSnapshot.getValue(DishPrice.class);
                    Log.e("Get Data", post.toString());
                    prices.put(post.dishName, post);
                }

                refOrder.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            String id = postSnapshot.child("id").getValue(String.class);
                            int status = postSnapshot.child("status").getValue(int.class);
                            String time = postSnapshot.child("timestamp").getValue(String.class);
                            String table = postSnapshot.child("tableID").getValue(String.class);
                            if (status != 4 && oid.equals(id)) {
                                o = new OrderBill();
                                o.table = table;
                                o.id = id;
                                o.time = time;
                                o.D = new ArrayList<>();
                                Log.e("ID", id);
                            }
                        }

                        refOrderDetails.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // This method is called once with the initial value and again
                                // whenever data at this location is updated
                                Log.e("OrderDetails", valueOf(dataSnapshot.getChildrenCount()));

                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    String id = postSnapshot.child("orderid").getValue(String.class);
                                    String dishname = postSnapshot.child("dishname").getValue(String.class);
                                    int quantity = postSnapshot.child("servings").getValue(int.class);
                                    int status = postSnapshot.child("status").getValue(int.class);
                                    Log.e("Dish", id);
                                    Log.e("Dish", dishname);
                                    if (status != 4 && id.equals(oid)) {
                                        DishOrder d = new DishOrder();
                                        d.quantity = quantity;
                                        DishPrice p = new DishPrice();
                                        if (prices.containsKey(dishname)) {
                                            p = prices.get(dishname);
                                            d.price = p.price;
                                            d.dishName = p.dishName;
                                            o.D.add(d);
                                        } else {
                                            d.quantity = quantity;
                                            d.dishName = dishname;
                                            o.D.add(d);
                                        }
                                    }
                                }

                                orderid.setText("Order ID: " + o.id);
                                table.setText("Table: " + o.table);
                                time.setText("Time: " + o.time);
                                total.setText("Total Price: " + o.getTotal());

                                if (mAdapter != null) {
                                    mAdapter.mDataset = o.getD();
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    mRecyclerView = (RecyclerView) findViewById(R.id.billRecView);

                                    mLayoutManager = new LinearLayoutManager(c);
                                    mRecyclerView.setLayoutManager(mLayoutManager);
                                    mAdapter = new BillAdapter(o.getD(), R.layout.billing_bill_view, c);
                                    mRecyclerView.setAdapter(mAdapter);

                                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(c);
                                    DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(
                                            mRecyclerView.getContext(),
                                            mLayoutManager.getOrientation()
                                    );

                                    mRecyclerView.addItemDecoration(mDividerItemDecoration);

                                    mRecyclerView2 = (RecyclerView) findViewById(R.id.billRecView2);

                                    mLayoutManager2 = new LinearLayoutManager(c);
                                    mRecyclerView2.setLayoutManager(mLayoutManager2);
                                    mAdapter2 = new BillAdapter(splitlist, R.layout.billing_bill_view, c);
                                    mRecyclerView2.setAdapter(mAdapter2);

                                    mRecyclerView2.setItemAnimator(new DefaultItemAnimator());
                                    LinearLayoutManager mLayoutManager2 = new LinearLayoutManager(c);
                                    DividerItemDecoration mDividerItemDecoration2 = new DividerItemDecoration(
                                            mRecyclerView2.getContext(),
                                            mLayoutManager2.getOrientation()
                                    );
                                    mRecyclerView2.addItemDecoration(mDividerItemDecoration2);

                                    mAdapter.setAdapter2(mAdapter2);
                                    mAdapter2.setAdapter2(mAdapter);
                                    mAdapter.setBill1((TextView) findViewById(R.id.bill1));
                                    mAdapter.setBill2((TextView) findViewById(R.id.bill2));
                                    mAdapter2.setBill1((TextView) findViewById(R.id.bill2));
                                    mAdapter2.setBill2((TextView) findViewById(R.id.bill1));
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                // Failed to read value
                                Log.w("Test", "Failed to read value.", error.toException());
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("Test", "Failed to read value.", error.toException());
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Test", "Failed to read value.", error.toException());
            }
        });

    }


    //Populates the view to let the hallmanager confirm payment
    public void populateForManager() {
        mRecyclerView = (RecyclerView) findViewById(R.id.billRecView);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new BillAdapter(o.getD(), R.layout.billing_bill_view, this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(
                mRecyclerView.getContext(),
                mLayoutManager.getOrientation()
        );

        mRecyclerView.addItemDecoration(mDividerItemDecoration);

        mRecyclerView2 = (RecyclerView) findViewById(R.id.billRecView2);

        mLayoutManager2 = new LinearLayoutManager(this);
        mRecyclerView2.setLayoutManager(mLayoutManager2);
        mAdapter2 = new BillAdapter(splitlist, R.layout.billing_bill_view, this);
        mRecyclerView2.setAdapter(mAdapter2);

        mRecyclerView2.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager mLayoutManager2 = new LinearLayoutManager(this);
        DividerItemDecoration mDividerItemDecoration2 = new DividerItemDecoration(
                mRecyclerView2.getContext(),
                mLayoutManager2.getOrientation()
        );
        mRecyclerView2.addItemDecoration(mDividerItemDecoration2);

        mAdapter.setAdapter2(mAdapter2);
        mAdapter2.setAdapter2(mAdapter);
        mAdapter.setBill1((TextView) findViewById(R.id.bill1));
        mAdapter.setBill2((TextView) findViewById(R.id.bill2));
        mAdapter2.setBill1((TextView) findViewById(R.id.bill2));
        mAdapter2.setBill2((TextView) findViewById(R.id.bill1));
    }


    //Enables 2nd recycler view and lets user/manager split the bill
    public void onClickSplit(View v) {
        mAdapter2.tap = true;
        mAdapter.tap = true;
        findViewById(R.id.billRecView2).setVisibility(View.VISIBLE);
        findViewById(R.id.split_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.payment_merge).setVisibility(View.VISIBLE);
        findViewById(R.id.bill2).setVisibility(View.VISIBLE);

    }


    //Resets view of split items
    public void onClickMerge(View v) {
        mAdapter2.tap = false;
        mAdapter.tap = false;
        findViewById(R.id.billRecView2).setVisibility(View.INVISIBLE);
        findViewById(R.id.split_button).setVisibility(View.VISIBLE);
        findViewById(R.id.payment_merge).setVisibility(View.INVISIBLE);
        findViewById(R.id.bill2).setVisibility(View.INVISIBLE);
        mAdapter.mDataset = o.D;
        mAdapter.notifyDataSetChanged();
        mAdapter2.mDataset.clear();
        mAdapter2.notifyDataSetChanged();
        int total = o.getTotal();
        TextView bill = (TextView) findViewById(R.id.bill1);
        bill.setText(String.valueOf(total));
    }

    //Updates 3 tables.
    //The status of the the order in the order table
    //The status of each orderdetail item of the associated order.
    //Creates a receipt for the orderid in firebase.
    public View.OnClickListener onConfirmClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            refOrderDetails = ref.child("OrderDetails");
            v.setEnabled(false);
            refOrderDetails.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated
                    Log.e("OrderDetails", valueOf(dataSnapshot.getChildrenCount()));

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String id = postSnapshot.child("orderid").getValue(String.class);
                        int status = postSnapshot.child("status").getValue(int.class);
                        if (status != 4 && id.equals(oid)) {
                            keys.add(postSnapshot.getKey());
                            //The firebase keys of orderdetails items that need to be updated in firebase
                            //Log.e("KEYS", postSnapshot.getKey());
                        }
                    }

                    if (!keys.isEmpty()) {
                        for (int i = 0; i < keys.size(); i++) {
                            //Change status of orderdetails items.
                            ref.child("OrderDetails").child(keys.get(i)).child("status").setValue(4);
                        }
                    }

                    //Update status to 4
                    ref.child("Order").child(oid).child("status").setValue(4);
                    Toast.makeText(c, "Payment successfuly saved in database", Toast.LENGTH_SHORT);

                    ref.child("Receipt").child(oid).child("paid").setValue(1);
                    ref.child("Receipt").child(oid).child("orderid").setValue(String.valueOf(o.id));
                    ref.child("Receipt").child(oid).child("totalamount").setValue(o.getTotal());
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("Test", "Failed to read value.", error.toException());
                }
            });
        }
    };

    /**
     * Handle make payment button click
     * Shows success snackbar message for customer payment processing
     */
    public View.OnClickListener onMakePaymentClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                // Show success snackbar with app theme styling
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Payment is successful", Snackbar.LENGTH_LONG);

                // Customize snackbar appearance to match app theme
                snackbar.setBackgroundTint(getResources().getColor(R.color.appOrange));
                snackbar.setTextColor(getResources().getColor(R.color.white));

                // Add action button
                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Snackbar will dismiss automatically
                    }
                });
                snackbar.setActionTextColor(getResources().getColor(R.color.white));

                snackbar.show();

                // Also show toast as backup
                Toast.makeText(c, "Payment is successful", Toast.LENGTH_LONG).show();

                // Disable the button to prevent multiple clicks
                v.setEnabled(false);
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        v.setEnabled(true);
                    }
                }, 3000); // Re-enable after 3 seconds

                // TODO: Future enhancement - integrate with actual payment gateway
                // For example: processPaymentGateway(o.getTotal(), oid);

            } catch (Exception e) {
                // Fallback to toast if snackbar fails
                Toast.makeText(c, "Payment is successful", Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     * Alternative method that can be called from XML onClick attribute
     */
    public void onMakePaymentClick(View v) {
        onMakePaymentClick.onClick(v);
    }

    /**
     * Populate bill view using local data passed from Time activity
     */
    private void populateFromLocalData(String orderId, String orderSummary, int totalPrice) {
        try {
            findViewById(R.id.confirm_payment).setVisibility(View.INVISIBLE);
            findViewById(R.id.payment_feedback).setVisibility(View.VISIBLE);

            final TextView orderidView = (TextView) findViewById(R.id.orderid_confirm);
            final TextView table = (TextView) findViewById(R.id.tableid_confirm);
            final TextView time = (TextView) findViewById(R.id.time_confirm);
            final TextView total = (TextView) findViewById(R.id.total_bill1);

            // Get enhanced data from Intent
            ArrayList<String> dishNames = getIntent().getStringArrayListExtra("dish_names");
            ArrayList<Integer> quantities = getIntent().getIntegerArrayListExtra("quantities");
            ArrayList<Integer> prices = getIntent().getIntegerArrayListExtra("prices");
            int itemCount = getIntent().getIntExtra("item_count", 0);

            System.out.println("DEBUG: ConfirmPayment received " + itemCount + " items");

            // Set basic order info
            orderidView.setText("Order ID: " + orderId);
            table.setText("Table: 4"); // Default table
            time.setText("Time: " + getCurrentTime());
            total.setText("Total Price: $" + totalPrice);

            // Create bill data structure
            o = new OrderBill();
            o.id = orderId;
            o.table = "4";
            o.time = getCurrentTime();
            o.D = new ArrayList<>();

            // Use enhanced data structure if available
            if (dishNames != null && quantities != null && prices != null &&
                    dishNames.size() > 0 && dishNames.size() == quantities.size() && quantities.size() == prices.size()) {

                System.out.println("DEBUG: Using enhanced data structure with " + dishNames.size() + " items");

                // Create DishOrder objects from the passed arrays
                for (int i = 0; i < dishNames.size(); i++) {
                    DishOrder d = new DishOrder();
                    d.dishName = dishNames.get(i);
                    d.quantity = quantities.get(i);
                    d.price = String.valueOf(prices.get(i));
                    o.D.add(d);

                    System.out.println("DEBUG: Added " + d.dishName + " x" + d.quantity + " @ $" + d.price);
                }

            } else {
                // Fallback to parsing order summary string
                System.out.println("DEBUG: Falling back to order summary parsing");
                parseOrderSummary(orderSummary);
            }

            // Set up RecyclerView with local data
            setupRecyclerViewWithLocalData();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading local bill data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            // Fallback to Firebase method
            populateForCustomer(orderId);
        }
    }

    /**
     * Parse order summary string as fallback method
     */
    private void parseOrderSummary(String orderSummary) {
        if (orderSummary == null || orderSummary.isEmpty()) {
            System.out.println("DEBUG: No order summary to parse");
            return;
        }

        // Parse the order summary string
        String[] items = orderSummary.split(";");
        System.out.println("DEBUG: Parsing " + items.length + " items from summary");

        for (String item : items) {
            if (!item.trim().isEmpty()) {
                String[] parts = item.split(":");
                if (parts.length >= 3) {
                    DishOrder d = new DishOrder();
                    d.dishName = parts[0];
                    d.quantity = Integer.parseInt(parts[1]);
                    // Fix: Convert int result to String for price field
                    int unitPrice = Integer.parseInt(parts[2]) / d.quantity;
                    d.price = String.valueOf(unitPrice);
                    o.D.add(d);

                    System.out.println("DEBUG: Parsed " + d.dishName + " x" + d.quantity + " @ $" + d.price);
                }
            }
        }
    }

    /**
     * Setup RecyclerView with local data to avoid Firebase dependency
     */
    private void setupRecyclerViewWithLocalData() {
        try {
            mRecyclerView = (RecyclerView) findViewById(R.id.billRecView);

            mLayoutManager = new LinearLayoutManager(c);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new BillAdapter(o.getD(), R.layout.billing_bill_view, c);
            mRecyclerView.setAdapter(mAdapter);

            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(c);
            DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(
                    mRecyclerView.getContext(),
                    mLayoutManager.getOrientation()
            );

            mRecyclerView.addItemDecoration(mDividerItemDecoration);

            mRecyclerView2 = (RecyclerView) findViewById(R.id.billRecView2);

            mLayoutManager2 = new LinearLayoutManager(c);
            mRecyclerView2.setLayoutManager(mLayoutManager2);
            mAdapter2 = new BillAdapter(splitlist, R.layout.billing_bill_view, c);
            mRecyclerView2.setAdapter(mAdapter2);

            mRecyclerView2.setItemAnimator(new DefaultItemAnimator());
            LinearLayoutManager mLayoutManager2 = new LinearLayoutManager(c);
            DividerItemDecoration mDividerItemDecoration2 = new DividerItemDecoration(
                    mRecyclerView2.getContext(),
                    mLayoutManager2.getOrientation()
            );
            mRecyclerView2.addItemDecoration(mDividerItemDecoration2);

            mAdapter.setAdapter2(mAdapter2);
            mAdapter2.setAdapter2(mAdapter);
            mAdapter.setBill1((TextView) findViewById(R.id.bill1));
            mAdapter.setBill2((TextView) findViewById(R.id.bill2));
            mAdapter2.setBill1((TextView) findViewById(R.id.bill2));
            mAdapter2.setBill2((TextView) findViewById(R.id.bill1));

            Toast.makeText(this, "Bill loaded successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error setting up bill display: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Get current time as string
     */
    private String getCurrentTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }
}
