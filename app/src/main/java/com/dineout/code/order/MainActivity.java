package com.dineout.code.order;
import com.dineout.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dineout.code.BaseActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/***********This java file is main file for handling the display of menu/dishes,
 ***********Handles event handling in case user clicks plus button i.e addToCart button
 ***********ArrayList<Menu> menuItems ,in this file holds all dishes read from Firebase*/
public class MainActivity extends BaseActivity
{
    ArrayList<Menu> menuItems=new ArrayList<Menu>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_menulist);//order_menulist corresponds to list view xml

        ListView listview=(ListView)findViewById(R.id.simpleListView);
        final CustomAdaptor cad=new CustomAdaptor(this,menuItems);
        listview.setAdapter(cad);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>parent,View view,int position,long id){

                startActivity(new Intent(MainActivity.this, setQuantity.class));
            }
        });


        /*Put listener upon any data change on Menu table(contains dishes) and fetch its children
        and notify adapter to update listview accordingly upon each fetch
         */
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Menu");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               menuItems.clear();
                for(DataSnapshot child:dataSnapshot.getChildren())
                {
                    Menu obj=child.getValue(Menu.class);
                    menuItems.add(obj);
                    System.out.print(obj.getDishName());
                    System.out.print(obj.getType());
                    System.out.print(obj.getPrice());
                    cad.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Handle call hall manager button click
     * Shows snackbar message to inform customer that hall manager is coming
     */
    public void callHallManager(View v) {
        try {
            // Show snackbar with message
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "Hall Manager is on the way to your table", Snackbar.LENGTH_LONG);

            // Customize snackbar appearance to match app theme
            snackbar.setBackgroundTint(getResources().getColor(R.color.appOrange));
            snackbar.setTextColor(getResources().getColor(R.color.white));

            // Add action button to dismiss snackbar
            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Snackbar will dismiss automatically
                }
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.white));

            snackbar.show();

            // Also show a toast as backup for older devices
            Toast.makeText(this, "Hall Manager is on the way to your table", Toast.LENGTH_LONG).show();

            // TODO: Future enhancement - send notification to hall manager system
            // For example: notifyHallManager(getCurrentTableNumber());
            // You could also log this request to Firebase for restaurant analytics

        } catch (Exception e) {
            // Fallback to toast if snackbar fails
            Toast.makeText(this, "Hall Manager is on the way to your table", Toast.LENGTH_LONG).show();
        }
    }

    public void redirectToQuantity(View v)
    {
        startActivity(new Intent(this,setQuantity.class));
    }
    public void redirectToCart(View v)
    {
        startActivity(new Intent(MainActivity.this,cartListView.class));
    }
    class CustomAdaptor extends BaseAdapter
    {
        private Activity mContext;
        private ArrayList<Menu> mItems;
        public CustomAdaptor(Activity context, ArrayList<Menu> list) {
            mContext = context;
            mItems = list;
        }
        @Override
        public int getCount() {
            return menuItems.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = getLayoutInflater().inflate(R.layout.order_item, null);

            TextView dishName = (TextView) convertView.findViewById(R.id.textView_dishName);
            TextView dishDesc = (TextView) convertView.findViewById(R.id.textView2_description);
            TextView price = (TextView) convertView.findViewById(R.id.dishPrice);
            ImageButton addcart = (ImageButton) convertView.findViewById(R.id.imageView);

            //Event Handling for click on plus sign in Menu Page handles here
            final String dishNamme=menuItems.get(position).getDishName();
            addcart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    //Like a Session variable ..it passes the dishName from the row that was clciked
                    SharedPreferences sharedPreferences=getSharedPreferences("shared preferences",MODE_PRIVATE);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("dishName",dishNamme);
                    editor.commit();
                    startActivity(new Intent(MainActivity.this,setQuantity.class));
                }
            });
            dishName.setText(menuItems.get(position).getDishName());
            String type="Its a "+menuItems.get(position).getType();
            dishDesc.setText(type);
            String pric="Price= "+menuItems.get(position).getPrice();
            price.setText(pric);
            return convertView;
        }
    }
    public class ViewHolder {

        ImageButton addcart;
        TextView dishName;
        TextView dishDesc;
        TextView price;
    }
}
