package com.dineout.code.admin;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dineout.R;
import com.dineout.code.BaseActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/*
Admin interface
All functionalities corresponding to buttons
*/

public class AdminPanelActivity extends BaseActivity {
    private Button notificationButton;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;

    private String date1 = null;
    private Date d1;
    private Date d2;
    private final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private final String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
    private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    static ArrayList<NotificationClass> notf = new ArrayList<>();
    static ArrayList<Item> itm = new ArrayList<>();
    static ArrayList<String> keys = new ArrayList<>();
    static Boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_admin_menu);

        notificationButton = findViewById(R.id.ViewNotificationsButton301);
        checkdate();

        notificationButton.setBackgroundResource(R.drawable.mybutton);
        notificationButton.setTextColor(getResources().getColor(R.color.black));

        notificationButton.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), Notifications.class);
            startActivity(i);
        });

        // Firebase database reference
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("notification");

        // Listen for notification changes
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    NotificationClass notification = dsp.getValue(NotificationClass.class);
                    if (notification != null && notification.getTime() != null && notification.getItemName() != null) {
                        if (!notification.isRead()) {
                            notificationButton.setText("New Notification");
                            notificationButton.setBackgroundColor(getResources().getColor(R.color.red));
                            notificationButton.setTextColor(getResources().getColor(R.color.white));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Database Error: ", databaseError.toString());
            }
        });
    }

    // Navigation methods
    public void onClickReg(View v) { startActivity(new Intent(this, AddItem.class)); }
    public void onClickReg1(View v) { startActivity(new Intent(this, IngredientsList.class)); }
    public void onClickReg2(View v) { startActivity(new Intent(this, AddEmployeeActivity.class)); }
    public void onClickReg3(View v) { startActivity(new Intent(this, AddTabletActivity.class)); }
    public void onClickReg4(View v) { startActivity(new Intent(this, AddTableActivity.class)); }
    public void onClickReg5(View v) { startActivity(new Intent(this, AddMenuItemActivity.class)); }
    public void onClickReg6(View v) { startActivity(new Intent(this, EndOfWeekActivitiy.class)); }
    public void onClickReg7(View v) { startActivity(new Intent(this, Notifications.class)); }

    @Override
    protected void onResume() {
        super.onResume();
        notificationButton.setBackgroundResource(R.drawable.mybutton);
        notificationButton.setTextColor(getResources().getColor(R.color.black));
        notificationButton.setText("Notifications");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    NotificationClass notification = dsp.getValue(NotificationClass.class);
                    if (notification != null && notification.getTime() != null && notification.getItemName() != null) {
                        if (!notification.isRead()) {
                            notificationButton.setText("New Notification");
                            notificationButton.setBackgroundColor(getResources().getColor(R.color.red));
                            notificationButton.setTextColor(getResources().getColor(R.color.white));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            // Use local logout helper instead of Firebase
            com.dineout.code.auth.LogoutHelper.logout(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Date checking logic
    public void checkdate() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Date");

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousKey) {
                date1 = dataSnapshot.getValue(String.class);
                DateFormat f = new SimpleDateFormat("dd-MM-yyyy");
                d1 = f.parse(date, new ParsePosition(0));
                d2 = f.parse(date1, new ParsePosition(0));

                if (d1.compareTo(d2) == 0) {
                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference();
                    ref1.child("Date").child("date").setValue(date);
                    checkdb();
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // Inventory & notification checks
    public void checkdb() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("notification");

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousKey) {
                keys.add(dataSnapshot.getKey());
                notf.add(dataSnapshot.getValue(NotificationClass.class));
            }

            @Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        databaseReference = firebaseDatabase.getReference("Inventory");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                removenotif();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousKey) {
                itm.add(dataSnapshot.getValue(Item.class));
            }

            @Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // Remove resolved notifications
    public void removenotif() {
        for (int i = 0; i < notf.size(); i++) {
            for (int j = 0; j < itm.size(); j++) {
                if (notf.get(i).getItemName().equals(itm.get(j).getName())) {
                    if (Integer.parseInt(itm.get(j).getQuantity()) > Integer.parseInt(itm.get(j).getThreshold())) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("notification")
                                .child(keys.get(i))
                                .removeValue();
                    }
                }
            }
        }
    }
}
