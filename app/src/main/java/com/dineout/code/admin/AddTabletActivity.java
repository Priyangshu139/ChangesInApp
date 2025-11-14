package com.dineout.code.admin;

import com.dineout.R;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddTabletActivity extends AppCompatActivity {

    private Spinner spinner1;
    Spinner spnStatus;
    static ArrayList<Long> eid = new ArrayList<>();
    ArrayList<Tablet> t = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_add_tablet);
        addItemsOnSpinner();
    }

    // Populate the spinner with status options
    public void addItemsOnSpinner() {
        spinner1 = findViewById(R.id.TabletStatusDropDown300);
        List<String> list = new ArrayList<>();
        list.add("In Use");
        list.add("Not Use");
        list.add("Not Working");
        list.add("Broken");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, list
        );
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(dataAdapter);
    }

    public void btnBlick(View v) {
        spnStatus = findViewById(R.id.TabletStatusDropDown300);

        DatabaseReference mDatabase1 = FirebaseDatabase.getInstance().getReference();
        mDatabase1.child("Ids").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                if (dataSnapshot.exists()) {
                    Long value = dataSnapshot.getValue(Long.class);
                    if (value != null) {
                        Toast.makeText(AddTabletActivity.this,
                                "IDZ = " + value, Toast.LENGTH_SHORT).show();
                        eid.add(value);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference();
        if (t.size() == 0) {
            ref1.child("Ids").child("Tabletid").setValue(1);
        }

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Tablet").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!eid.isEmpty()) {
                    long nextId = eid.get(eid.size() - 1); // safer than hardcoded eid.get(2)
                    Tablet tablet = new Tablet(
                            String.valueOf(nextId),
                            spnStatus.getSelectedItem().toString()
                    );

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    ref.child("Tablet").child(String.valueOf(nextId)).setValue(tablet);
                    ref.child("Ids").child("Tabletid").setValue(nextId + 1);

                    Toast.makeText(AddTabletActivity.this,
                            "Tablet has been added successfully",
                            Toast.LENGTH_SHORT).show();

                    Intent it = new Intent(AddTabletActivity.this, AdminPanelActivity.class);
                    eid.clear();
                    startActivity(it);
                } else {
                    Toast.makeText(AddTabletActivity.this,
                            "No Tablet ID found in Firebase.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        mDatabase.child("Tablet").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousKey) {
                Tablet item = dataSnapshot.getValue(Tablet.class);
                if (item != null) {
                    t.add(item);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
