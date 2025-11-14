package com.dineout.code.admin;

import com.dineout.R;
import com.dineout.code.BaseActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/*
Making a dish using ingredients
Setting items from drop down
Making object
Saving to Firebase
*/

public class AddMenuItemActivity extends BaseActivity {

    DatabaseReference mDatabase;

    EditText name, time, type, price, quantity;
    List<String> items = new ArrayList<>();
    Spinner ingredientSpn;
    Button addMenu, addIngredient;
    ArrayList<MenuItem> menuItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_add_menu_item);

        name = findViewById(R.id.AddDishName300);
        type = findViewById(R.id.AddDishType300);
        price = findViewById(R.id.AddDishPrice300);
        time = findViewById(R.id.AddDishEstimatedTime300);
        addIngredient = findViewById(R.id.AddIngredientQuantityButton300);
        addMenu = findViewById(R.id.AddMenuItemButton300);
        quantity = findViewById(R.id.AddQuantity300);
        ingredientSpn = findViewById(R.id.IngredientsDropDown300);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ingredientSpn.setAdapter(adapter);

        // Load ingredients from Firebase
        mDatabase.child("Inventory").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String prevChildKey) {
                if (dataSnapshot.exists()) {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null) {
                        adapter.add(item.name);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        // Add ingredient to dish
        addIngredient.setOnClickListener(v -> {
            if (quantity.getText().toString().isEmpty()) {
                quantity.setError("Quantity is Required");
            } else {
                MenuItem menuItem = new MenuItem(
                        name.getText().toString(),
                        ingredientSpn.getSelectedItem().toString(),
                        quantity.getText().toString()
                );
                menuItems.add(menuItem);
                quantity.setText("");
                Toast.makeText(AddMenuItemActivity.this, "Ingredient Added Successfully", Toast.LENGTH_SHORT).show();
            }
        });

        // Add menu item to Firebase
        addMenu.setOnClickListener(v -> {
            boolean go = true;

            if (name.getText().toString().isEmpty()) {
                name.setError("Dish Name is Required");
                go = false;
            } else if (time.getText().toString().isEmpty()) {
                time.setError("Est. Time is Required");
                go = false;
            } else if (type.getText().toString().isEmpty()) {
                type.setError("Dish Type is Required");
                go = false;
            } else if (price.getText().toString().isEmpty()) {
                price.setError("Price is Required");
                go = false;
            }

            if (!price.getText().toString().isEmpty() &&
                    Integer.parseInt(price.getText().toString()) < 1) {
                price.setError("Price cannot be 0");
                go = false;
            }

            if (menuItems.isEmpty()) {
                quantity.setError("No Ingredients Added");
                go = false;
            }

            if (go) {
                Menu m = new Menu(
                        name.getText().toString(),
                        time.getText().toString(),
                        type.getText().toString(),
                        price.getText().toString()
                );

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.child("Menu").child(m.getDishName()).setValue(m);

                for (MenuItem item : menuItems) {
                    ref.child("MenuItem")
                            .child(item.dishName)
                            .child(item.ingredientName)
                            .setValue(item);
                }

                Toast.makeText(AddMenuItemActivity.this, "Menu Item Added Successfully", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
