package com.dineout.code.admin;

import com.dineout.R;
import com.dineout.code.BaseActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Spinner to add table
 * Add capacity of table
 * Set state of the table
 */

public class AddTableActivity extends BaseActivity {

    private Spinner spinner1;
    ArrayList<Table> t = new ArrayList<>();
    Spinner spnStatus;
    static ArrayList<Long> eid = new ArrayList<>();
    EditText txtCapacity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_add_table);
        spnStatus = findViewById(R.id.TableStatusDropDown300);
        txtCapacity = findViewById(R.id.AddTableCapacity300);
        addItemsOnSpinner();
    }

    // Add items into spinner dynamically
    public void addItemsOnSpinner() {
        spinner1 = findViewById(R.id.TableStatusDropDown300);
        List<String> list = new ArrayList<>();
        list.add("Free");
        list.add("Booked");
        list.add("Occupied");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, list
        );
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(dataAdapter);
    }

    public void btnBlick(View v) {
        boolean go = true;

        if (txtCapacity.getText().toString().length() <= 0) {
            txtCapacity.setError("Capacity is required");
            go = false;
        }

        if (txtCapacity.getText().toString().length() > 0 &&
                Integer.parseInt(txtCapacity.getText().toString()) <= 0) {
            txtCapacity.setError("Capacity should be greater than 0");
            go = false;
        }

        if (go) {
            DatabaseReference mDatabase1 = FirebaseDatabase.getInstance().getReference();
            mDatabase1.child("Ids").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                    if (dataSnapshot != null) {
                        Long idValue = dataSnapshot.getValue(Long.class);
                        if (idValue != null) {
                            Toast.makeText(AddTableActivity.this,
                                    "IDZ = " + idValue, Toast.LENGTH_SHORT).show();
                            eid.add(idValue);
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
                ref1.child("Ids").child("Tableid").setValue(1);
            }

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("Table").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (eid.size() > 1) {
                        Table table = new Table(
                                String.valueOf(eid.get(1)),
                                spnStatus.getSelectedItem().toString(),
                                txtCapacity.getText().toString()
                        );

                        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference();
                        ref1.child("Table").child(String.valueOf(eid.get(1))).setValue(table);
                        ref1.child("Ids").child("Tableid").setValue(eid.get(1) + 1);

                        Toast.makeText(AddTableActivity.this,
                                "Table has been added successfully", Toast.LENGTH_SHORT).show();
                        Intent it = new Intent(AddTableActivity.this, AdminPanelActivity.class);
                        eid.clear();
                        startActivity(it);
                    } else {
                        Toast.makeText(AddTableActivity.this,
                                "Failed to retrieve table ID", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });

            mDatabase.child("Table").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousKey) {
                    Table item = dataSnapshot.getValue(Table.class);
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
}
