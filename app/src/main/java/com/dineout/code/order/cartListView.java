package com.dineout.code.order;

import com.dineout.R;
import com.dineout.code.BaseActivity;
import com.dineout.code.data.LocalMenuManager;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class cartListView extends BaseActivity {

    String dummy = null;

    ArrayList<cart> cartItems=new ArrayList<cart>();
    ArrayList<Menu>dishDetails=new ArrayList<Menu>();
    final CustomAdaptor cad=new CustomAdaptor();
    private LocalMenuManager localMenuManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity_cart_list_view);

        // Initialize local menu manager for pricing
        localMenuManager = new LocalMenuManager(this);

        Toast.makeText(getApplicationContext(), "Current Selected Items in Cart!", Toast.LENGTH_LONG).show();

        ListView listview=(ListView)findViewById(R.id.cartListView);
        listview.setAdapter(cad);
        //Listene for confirm order button click
        Button confirmBtn=(Button)findViewById(R.id.confrimOrder);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creates Pop Up That upon yes redirects to Timer page
                confirmOrder confirmorder=new confirmOrder();
                confirmorder.show(getSupportFragmentManager(),"Order Confirmation");

            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent,View view,int position,long id){

                //startActivity(new Intent(dummyActivity.this, setQuantity.class));
            }
        });

        // Try Firebase first, fallback to local data if needed
        try {
            loadCartFromFirebase();
        } catch (Exception e) {
            Toast.makeText(this, "Firebase unavailable, using local cart data", Toast.LENGTH_SHORT).show();
            loadCartFromLocal();
        }
    }

    private void loadCartFromFirebase() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("cart");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cartItems.clear();
                for(DataSnapshot child:dataSnapshot.getChildren())
                {
                    cart obj = child.getValue(cart.class);
                    cartItems.add(obj);
                    dummy=obj.getAddedname();
                    System.out.print(obj.getQuantity());
                    System.out.print(obj.getAddedname());
                    cad.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Fallback to local cart data if Firebase fails
                Toast.makeText(cartListView.this, "Firebase failed, using local cart", Toast.LENGTH_SHORT).show();
                loadCartFromLocal();
            }
        });
    }

    private void loadCartFromLocal() {
        // Create sample cart items for testing when Firebase is unavailable
        cartItems.clear();

        // Add sample cart items
        cart item1 = new cart();
        item1.setAddedname("Grilled Chicken");
        item1.setQuantity(2);
        cartItems.add(item1);

        cart item2 = new cart();
        item2.setAddedname("Fries");
        item2.setQuantity(1);
        cartItems.add(item2);

        cad.notifyDataSetChanged();
        Toast.makeText(this, "Local cart loaded with sample items", Toast.LENGTH_SHORT).show();
    }

    /**
     * Get price for a dish from local menu
     */
    private String getDishPrice(String dishName) {
        Menu dish = localMenuManager.getDishByName(dishName);
        if (dish != null) {
            return dish.getPrice();
        }

        // Fallback pricing if dish not found in local menu
        switch (dishName.toLowerCase()) {
            case "grilled chicken":
                return "380";
            case "beef steak":
                return "550";
            case "pizza":
                return "350";
            case "burger":
                return "300";
            case "pasta alfredo":
                return "320";
            case "fries":
                return "150";
            case "soup":
                return "200";
            case "chocolate":
                return "180";
            case "chicken wings":
                return "250";
            case "caesar salad":
                return "180";
            default:
                return "200"; // Default price
        }
    }

    // }
    public void openPopup(View v)//extra function ..redirects to Timer upon confirm order
    {
        //startActivity(new Intent(cartListView.this,Time.class));
       confirmOrder confirmorder=new confirmOrder();
       confirmorder.show(getSupportFragmentManager(),"Order Confirmation");
    }

    class CustomAdaptor extends BaseAdapter {

        @Override
        public int getCount() {
            return cartItems.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            //fetches listview particular row items and sets them and handles events upon buttons as well
            convertView =getLayoutInflater().inflate(R.layout.order_cartobjects,null);
            TextView dishName = (TextView) convertView.findViewById(R.id.cartDishName);
            final TextView dishDesc = (TextView) convertView.findViewById(R.id.cartDishDesc);
            TextView price = (TextView) convertView.findViewById(R.id.cartDishPrice);
            ImageButton removeBtn = (ImageButton) convertView.findViewById(R.id.remove);

            //set values in Cart page
            dishName.setText(cartItems.get(position).getAddedname());
            String quant=String.format("QUANTITY= %d",cartItems.get(position).getQuantity());
            dishDesc.setText(quant);

            // FIX: Get actual price instead of hardcoded 100
            String dishPrice = getDishPrice(cartItems.get(position).getAddedname());
            int quantity = cartItems.get(position).getQuantity();
            int totalPrice = Integer.parseInt(dishPrice) * quantity;
            price.setText("$" + totalPrice);

            //dishClicked contains name of dish that was clicked and is passed to listeners below
            final String dishClicked=cartItems.get(position).getAddedname();

            //*****When user clicks delete icon on cart the following listener executes*****
            removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add delete order_item logic here
                    final DatabaseReference ref3= FirebaseDatabase.getInstance().getReference().child("cart");
                    ref3.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                            for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {

                                final cart dummy=childSnapShot.getValue(cart.class);
                                if(dummy.getAddedname().equals(dishClicked))
                                {
                                    final String key = childSnapShot.getKey();
                                    ref3.child(key).setValue(null);
                                    DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("MenuItem").child("Soup");
                                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                                            for(DataSnapshot ds: dataSnapshot.getChildren())
                                            {
                                                //Decrement the ingridients in Inventory corresponding to the dish that was removed from cart
                                                final MenuItem MI = ds.getValue(MenuItem.class);
                                                String menuItem= MI.getDishName();//unique identifier
                                                if(dummy.getAddedname().equals(MI.getDishName())){
                                                    final DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("Inventory");
                                                    ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        boolean invalid=false;
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                                Item i = ds.getValue(Item.class);
                                                                int count=0;
                                                                if (MI.getIngredientName().equals(i.getName())) {
                                                                    int requiredamount = (Integer.parseInt(MI.getQuantity())) * (dummy.getQuantity());
                                                                    int newQ = Integer.parseInt(i.quantity) + requiredamount;
                                                                    Item item = new Item(i.getName(), i.getPrice(), String.valueOf(newQ), i.getThreshold());
                                                                    ref2.child(i.getName()).setValue(item);
                                                                }

                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });


                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    //Remove the dish that was deleted from cart from list and notify adapter to change
                    cartItems.remove(position);
                    cad.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Item Removed from cart!", Toast.LENGTH_LONG).show();

                }
            });
            return convertView;
        }
    }
    public class ViewHolder
    {

        ImageButton removeBtn;
        TextView dishName;
        TextView dishDesc;
        TextView price;
    }
}