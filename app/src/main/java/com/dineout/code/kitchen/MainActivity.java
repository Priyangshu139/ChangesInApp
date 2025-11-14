package com.dineout.code.kitchen;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dineout.R;
import com.dineout.code.BaseActivity;
import com.dineout.code.admin.LoginActivity;
import com.dineout.code.data.LocalKitchenDataManager;
import com.dineout.code.kitchen.models.AttendanceDb;
import com.dineout.code.kitchen.models.Chef;
import com.dineout.code.kitchen.models.DishDb;
import com.dineout.code.kitchen.models.EmployeeDb;
import com.dineout.code.kitchen.models.Order;
import com.dineout.code.kitchen.models.OrderDb;
import com.dineout.code.kitchen.models.OrderDetailsDb;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends BaseActivity
{
    public static int WAITING = 0;
    public static int COOKING = 1;
    public static int READY = 2;
    public static int HIGHPRIORITY = 1;
    public static int LOWPRIORITY = 0;
  //  RecyclerViewAdapterCook adapter;

    public static ArrayList<OrderDetailsDb> orderDetails = new ArrayList<>();
    static ArrayList<AttendanceDb> attendanceDbs = new ArrayList<>();
    static ArrayList<Chef> mChefs;
    static ArrayList<DishDb> dishes = new ArrayList<>();
    static RecyclerViewAdapterCook adapter;
    public static ArrayList<OrderDb> orders = new ArrayList<>();
    //public static ArrayList<Chef> allChefs = new ArrayList<>();
    int x = 1;
    static int chefNo = 0;
    static FirebaseDatabase mDatabase;
    static Boolean selected = false;
    DatabaseReference myDbRef;
    ActionBar actionBar;
    ProgressDialog progressDialog;
    RecyclerView recyclerView;
    private LocalKitchenDataManager localKitchenDataManager;
    private boolean useLocalData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show();

        setContentView(R.layout.kitchen_activity_main);
        actionBar=getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffff8800")));
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>AROS </font>"));
        progressDialog = new ProgressDialog(MainActivity.this);

        // Initialize local data manager
        localKitchenDataManager = new LocalKitchenDataManager(this);

        //Initializing Database
        try {
            mDatabase = FirebaseDatabase.getInstance();
            myDbRef = mDatabase.getReference("Employee");
        } catch (Exception e) {
            Toast.makeText(this, "Firebase unavailable, using local data", Toast.LENGTH_LONG).show();
            useLocalData = true;
        }

        // mChefs.clear();
        // dishes.clear();

        //getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.cardview_dark_background)));
        /*if(!selected)
        {*/
        if (useLocalData) {
            loadLocalData();
        } else {
            ExtractChefsFromDb();
        }
            mChefs = new ArrayList<>();
            selected = false;
        // }
    }

    /**
     * Load local data when Firebase is unavailable
     */
    private void loadLocalData() {
        Toast.makeText(this, "Loading local chef data...", Toast.LENGTH_SHORT).show();

        // Load chefs from local data
        mChefs = localKitchenDataManager.getLocalChefs();

        // Load dishes from local data
        dishes = localKitchenDataManager.getLocalDishes();

        // Apply attendance
        ArrayList<AttendanceDb> attendance = localKitchenDataManager.getLocalAttendance();
        localKitchenDataManager.applyAttendanceToChefs(mChefs, attendance);

        // Initialize RecyclerView
        initRecyclerView();

        // Process any customer orders that were placed before kitchen was ready
        processTemporaryCustomerOrders();

        // IMPORTANT: Load fake orders FIRST to make kitchen look busy from the start
        addFakeOrdersToKitchen();

        Toast.makeText(this, "Kitchen loaded: " + mChefs.size() + " chefs with busy order queue", Toast.LENGTH_SHORT).show();
    }

    /**
     * Add fake orders to kitchen immediately - creates busy restaurant atmosphere
     * Customer orders will be added on top of these fake orders
     */
    private void addFakeOrdersToKitchen() {
        ArrayList<OrderDetailsDb> fakeOrders = localKitchenDataManager.getSampleOrders();

        // Add fake orders to create busy kitchen environment
        for (OrderDetailsDb fakeOrder : fakeOrders) {
            // Try to assign to appropriate chef based on specialty
            if (!assignDishToChef(fakeOrder)) {
                // If can't assign to specific chef, add to general order queue
                orderDetails.add(fakeOrder);
            }
        }

        // Refresh adapter to show busy kitchen
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        System.out.println("Busy kitchen loaded: " + fakeOrders.size() + " orders distributed across chefs");
    }

    public static int getDishCookingTime(String dish){
        for(DishDb dis:dishes){
            if(dis.getDishName().equals(dish))
                return Integer.parseInt(dis.getEstimatedTime());
        }

        return 0;
    }

    public static String getDishType(String dish){
        for(DishDb dis:dishes){
            if(dis.getDishName().equals(dish))
                return dis.getType();
        }

        return "";
    }

    public void onLoadBalancingClicked(View view){
        for(Chef chef:mChefs){
            for (OrderDetailsDb dish:chef.getChefQueue()){
                if(dish.getStatus() == WAITING)
                    orderDetails.add(dish);
            }

        }

        for (Chef chef:mChefs){
            boolean removed = true;
            while(removed){
                removed = false;
                for (int i=0;i<chef.getChefQueue().size();i++){
                    if (chef.getChefQueue().get(i).getStatus() == WAITING){
                        chef.getChefQueue().remove(i);
                        removed = true;
                    }
                }
            }
        }

        boolean assigned = true;

        for(OrderDetailsDb dish:orderDetails){
            assigned = assignDishToChef(dish);
        }
        if(assigned)
            orderDetails = new ArrayList<>();

        adapter.notifyDataSetChanged();
    }

    private boolean assignDishToChef(OrderDetailsDb dish){
        Chef temp = null;
        int time = -1;

        for(Chef chef:mChefs){
            if (chef.isPresent()){
                if(chef.getSpecialty().equals(getDishType(dish.getDishname()))){
                    if (time == -1){
                        temp = chef;
                        time = chef.returnWaitingTime();
                    }
                    else{
                        if(chef.returnWaitingTime() < temp.returnWaitingTime()){
                            time = chef.returnWaitingTime();
                            temp = chef;
                        }
                    }
                }
            }
        }

        if(time != -1){
            temp.addDish(dish);
            //temp.addOrder(new Order(( new Integer(dish.getStatus())).toString(), dish.getDishname()));
            updateDishTime(dish, temp.returnCookingTime() + temp.returnWaitingTime() + getDishCookingTime(dish.getDishname()));
            return true;
        }


        for(Chef chef:mChefs){
            if (chef.isPresent()){

                if (time == -1){
                    temp = chef;
                    time = chef.returnWaitingTime();
                }
                else{
                    if(chef.returnWaitingTime() < temp.returnWaitingTime()){
                        time = chef.returnWaitingTime();
                        temp = chef;
                    }
                }

            }
        }

        if (time != -1)
        {
            temp.addDish(dish);
            //temp.addOrder(new Order(( new Integer(dish.getStatus())).toString(), dish.getDishname()));
            updateDishTime(dish, temp.returnCookingTime() + temp.returnWaitingTime() + getDishCookingTime(dish.getDishname()));
            return true;
        }
        else
            return false;
    }

    public void reinitializeCookAdapter()
    {
        adapter = new RecyclerViewAdapterCook(this, mChefs);
        recyclerView.setAdapter(adapter);
    }

    public static void removeOrderDetailsFromDb(OrderDetailsDb dish){
        try {
            if (mDatabase != null) {
                mDatabase.getReference("OrderDetails").child(dish.getNodeId()).removeValue();
            }
        } catch (Exception e) {
            System.out.println("Local mode: Cannot remove from Firebase");
        }
    }

    public static void updateDishStatus(OrderDetailsDb dish, int newStatus)
    {
        try {
            if (mDatabase != null) {
                mDatabase.getReference("OrderDetails").child(dish.getNodeId()).child("status").setValue(newStatus);
            }
        } catch (Exception e) {
            System.out.println("Local mode: Cannot update Firebase");
        }
        dish.setStatus(newStatus);
    }

    public static void updateDishServings(OrderDetailsDb dish, int newServings)
    {
        try {
            if (mDatabase != null) {
                mDatabase.getReference("OrderDetails").child(dish.getNodeId()).child("servings").setValue(newServings);
            }
        } catch (Exception e) {
            System.out.println("Local mode: Cannot update Firebase");
        }
        dish.setServings(newServings);
    }

    public static void updateDishTime(OrderDetailsDb dish, int newTime)
    {
        try {
            if (mDatabase != null) {
                mDatabase.getReference("OrderDetails").child(dish.getNodeId()).child("estimatedtime").setValue(newTime);
            }
        } catch (Exception e) {
            System.out.println("Local mode: Cannot update Firebase");
        }
        dish.setEstimatedtime(newTime);
    }

    private void ExtractOrders()
    {
        if (useLocalData) {
            // Skip Firebase operations in local mode
            return;
        }

        mDatabase.getReference("OrderDetails").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //startProgressBar("Adding new dish...");

                OrderDetailsDb dish = dataSnapshot.getValue(OrderDetailsDb.class);
                dish.setNodeId(dataSnapshot.getKey());

                if(dish.getStatus() == WAITING || dish.getStatus() == COOKING)
                {
                    if (!assignDishToChef(dish))
                    {
                        // If dish is in waiting or cooking state then show it
                        orderDetails.add(dish);
                    }
                    else
                    {
                        reinitializeCookAdapter();
                        // if (dish.getDishname().equals("Soup"))
                        //   updateDishStatus(dish, 9);
                    }
                }

                //dismissProgressBar();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startProgressBar(String message)
    {
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void dismissProgressBar()
    {
        progressDialog.dismiss();
    }

    public void ExtractMenuFromDb()
    {
        if (useLocalData) {
            // Menu already loaded in loadLocalData()
            return;
        }

        mDatabase.getReference("Menu").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Toast.makeText(getApplicationContext(), "Loading Menu...", Toast.LENGTH_SHORT).show();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    DishDb dish = postSnapshot.getValue(DishDb.class);
                        dishes.add(dish);
                }

                //dismissProgressBar();

                ExtractOrders();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Fallback to local data if Firebase fails
                Toast.makeText(getApplicationContext(), "Firebase failed, using local menu", Toast.LENGTH_SHORT).show();
                useLocalData = true;
                loadLocalData();
            }
        });
    }

    public void openAttendance(View view)
    {
        Intent intent = new Intent(MainActivity.this, AttendanceActivity.class);
        intent.putExtra("chefs", new ArrayList<>(mChefs));
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == 123){
                boolean assigned = true;
                for (OrderDetailsDb dish:orderDetails){
                    assigned = assignDishToChef(dish);
                }
                orderDetails = new ArrayList<>();
                recyclerView.notify();
            }

            recyclerView.notify();
        }
    }

    private void loadAttendance(){
        if (useLocalData) {
            // Attendance already applied in loadLocalData()
            return;
        }

        Toast.makeText(this, "Loading Availability Info", Toast.LENGTH_SHORT).show();
        //progressDialog.setMessage("Loading availability info...");
        mDatabase.getReference("Attendance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                for (DataSnapshot postSnapshot : datasnapshot.getChildren()) {
                    AttendanceDb attendance = postSnapshot.getValue(AttendanceDb.class);

                    for (Chef chef: mChefs){
                        if(chef.getId().equals(attendance.getId()))
                            chef.setPresent(new Boolean(attendance.getPresent().booleanValue()));
                    }
                }

                //progressDialog.dismiss();
                initRecyclerView();

                ExtractMenuFromDb();
                getOrdersFromDb();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Fallback to local data
                Toast.makeText(getApplicationContext(), "Firebase failed, using local attendance", Toast.LENGTH_SHORT).show();
                useLocalData = true;
                loadLocalData();
            }
        });
    }

    private void ExtractChefsFromDb()
    {
        /*
        // Testing
        mChefs.clear();
        mChefs.add(new Chef("Chef 1"));
        mChefs.add(new Chef("Chef 2"));
        mChefs.add(new Chef("Chef 3"));
        mChefs.add(new Chef("Chef 4"));
        mChefs.add(new Chef("Chef 5"));
        mChefs.add(new Chef("Chef 6"));
        mChefs.add(new Chef("Chef 7"));
        mChefs.add(new Chef("Chef 8"));
        mChefs.add(new Chef("Chef 9"));
        mChefs.add(new Chef("Chef 10"));*/

        progressDialog.setMessage("Loading Chefs...");
        progressDialog.show();

        myDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                    EmployeeDb employee = postSnapshot.getValue(EmployeeDb.class);

                    if (employee.getType().equals("Chef")){
                        mChefs.add(new Chef(employee.getName(), employee.getId(), new ArrayList<Order>(), employee.getSpeciality(), new ArrayList<OrderDetailsDb>(), true));
                    }
                }
                //progressDialog.dismiss();
                ExtractOrdersOfChefsFromDb();
                loadAttendance();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
                progressDialog.dismiss();

                // Fallback to local data
                Toast.makeText(getApplicationContext(), "Firebase failed, loading local chef data", Toast.LENGTH_LONG).show();
                useLocalData = true;
                loadLocalData();
            }
        });
    }

    private void ExtractOrdersOfChefsFromDb()
    {
        /*
        // Testing
        mChefs.get(0).addOrder(new Order("Ready","Burger"));
        mChefs.get(0).addOrder(new Order("Ready","Fries"));
        mChefs.get(0).addOrder(new Order("Cooking","Rice"));
        mChefs.get(0).addOrder(new Order("Cooking","Chicken Maslaa"));
        mChefs.get(0).addOrder(new Order("Cooking","Egg Fried Rice"));
        mChefs.get(0).addOrder(new Order("Cooking","Chinese Rice"));
        mChefs.get(0).addOrder(new Order("Cooking","0"));
        mChefs.get(0).addOrder(new Order("Cooking","1"));
        mChefs.get(0).addOrder(new Order("Cooking","2"));
        mChefs.get(0).addOrder(new Order("Cooking","3"));
        mChefs.get(0).addOrder(new Order("Cooking","8"));
        mChefs.get(0).addOrder(new Order("Waiting","Pasta"));
        mChefs.get(0).addOrder(new Order("Waiting","Mutton handi"));*/
       /* mChefs.get(1).addOrder(new Order("Cooking","Zinger"));
        mChefs.get(1).addOrder(new Order("Cooking","Fries"));
        mChefs.get(2).addOrder(new Order("Cooking","Biryani"));
        mChefs.get(2).addOrder(new Order("Waiting","Egg"));
        mChefs.get(3).addOrder(new Order("Cooking","Pizza"));
        mChefs.get(4).addOrder(new Order("Cooking","Rice"));
        mChefs.get(5).addOrder(new Order("Cooking","Chicken Maslaa"));
        mChefs.get(6).addOrder(new Order("Cooking","Egg Fried Rice"));
        mChefs.get(6).addOrder(new Order("Cooking","Chinese Rice"));
        mChefs.get(7).addOrder(new Order("Ready","Chinese Rice"));
        mChefs.get(7).addOrder(new Order("Ready","Chinese Rice"));
        mChefs.get(7).addOrder(new Order("Waiting","Chinese Rice"));*/
    }

    private void initRecyclerView()
    {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerViewAdapterCook(this, mChefs);
        recyclerView.setAdapter(adapter);
    }

    public static void updateOrderStatus(OrderDb order, int newStatus){
        try {
            if (mDatabase != null) {
                mDatabase.getReference("Order").child(order.getNodeid()).child("status").setValue(newStatus);
            }
        } catch (Exception e) {
            System.out.println("Local mode: Cannot update Firebase");
        }
        order.setStatus(newStatus);
    }

    private void getOrdersFromDb(){
        if (useLocalData) {
            // Orders already handled in local mode
            return;
        }

        mDatabase.getReference("Order").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Toast.makeText(getApplicationContext(), "Adding Orders...", Toast.LENGTH_SHORT).show();

                //startProgressBar("Adding orders...");

                OrderDb order = dataSnapshot.getValue(OrderDb.class);
                order.setNodeid(dataSnapshot.getKey());

                if(order.getStatus() == WAITING || order.getStatus() == COOKING){
                    orders.add(order);
                }

                //dismissProgressBar();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static boolean isOrderCompleted(String orderId){
        for(Chef chef:mChefs){
            for(OrderDetailsDb dish:chef.getChefQueue()){

                if(dish.getOrderid().equals(orderId) && (dish.getStatus() == WAITING || dish.getStatus() == COOKING))
                    return false;
            }
        }

        return true;
    }

    public static OrderDb getOrderFromId(String id){
        for (OrderDb order:orders){
            if(order.getId().equals(id))
                return order;
        }

        return null;
    }

    public static void updateOrderStatusCooking(String orderId){
        for(OrderDb order:orders){
            if(order.getStatus() == WAITING){
                updateOrderStatus(order, COOKING);
                order.setStatus(COOKING);
            }
        }
    }

    /**
     * Static method to safely add customer orders to kitchen system
     * Customer orders are added to the already busy kitchen (on top of fake orders)
     */
    public static void addCustomerOrderToKitchen(OrderDetailsDb customerOrder) {
        try {
            if (orderDetails != null) {
                orderDetails.add(customerOrder);
                System.out.println("CUSTOMER ORDER added to busy kitchen: " + customerOrder.getDishname() + " x" + customerOrder.getServings());

                // Try to refresh adapter if available
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                return;
            }
        } catch (Exception e) {
            System.out.println("Could not add customer order to kitchen: " + e.getMessage());
        }

        // If direct addition fails, store in a temporary list for later processing
        addToTemporaryCustomerOrders(customerOrder);
    }

    /**
     * Temporary storage for customer orders when kitchen system isn't ready
     */
    private static ArrayList<OrderDetailsDb> temporaryCustomerOrders = new ArrayList<>();

    private static void addToTemporaryCustomerOrders(OrderDetailsDb order) {
        temporaryCustomerOrders.add(order);
        System.out.println("Customer order stored temporarily: " + order.getDishname());
    }

    /**
     * Process any temporarily stored customer orders
     */
    private void processTemporaryCustomerOrders() {
        if (!temporaryCustomerOrders.isEmpty()) {
            System.out.println("Processing " + temporaryCustomerOrders.size() + " temporary customer orders");
            for (OrderDetailsDb tempOrder : temporaryCustomerOrders) {
                orderDetails.add(tempOrder);
                // Try to assign to appropriate chef
                assignDishToChef(tempOrder);
            }
            temporaryCustomerOrders.clear();

            // Refresh adapter
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    //options menu for logout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    //
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            // Use local logout helper instead of Firebase
            com.dineout.code.auth.LogoutHelper.logout(this);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
