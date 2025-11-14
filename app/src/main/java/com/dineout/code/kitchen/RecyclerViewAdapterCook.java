/**
 * This adapter is used to show the chefs and their orders in
 * main screen of kitchen module (Head Chef)
* */

package com.dineout.code.kitchen;

import android.content.Context;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dineout.R;
import com.dineout.code.kitchen.models.Chef;

import java.util.ArrayList;

public class RecyclerViewAdapterCook extends RecyclerView.Adapter<RecyclerViewAdapterCook.ViewHolder>
{
    private ArrayList<Chef> mChefs = new ArrayList<>();
    private Context mContext;
    public static ArrayList<RecyclerViewAdapterOrdersOfCook> adapters = new ArrayList<>();

    // Array of different colored borders for chefs
    private int[] chefBorderColors = {
            R.drawable.chef_border_red,     // Chef 1 - Red
            R.drawable.chef_border_blue,    // Chef 2 - Blue
            R.drawable.chef_border_green,   // Chef 3 - Green
            R.drawable.chef_border_purple   // Chef 4 - Purple
    };

    // Array of matching text colors for chef names
    private int[] chefTextColors = {
            0xFFFF5722,  // Red for Chef 1
            0xFF2196F3,  // Blue for Chef 2
            0xFF4CAF50,  // Green for Chef 3
            0xFF9C27B0   // Purple for Chef 4
    };

    public RecyclerViewAdapterCook(Context context, ArrayList<Chef> chefs)
    {
        this.mChefs = chefs;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kitchen_layout_cooks_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        holder.name.setText(mChefs.get(position).getName());

        // Apply different colored border based on chef position
        CardView cardView = holder.cardView;
        if (position < chefBorderColors.length) {
            cardView.setBackgroundResource(chefBorderColors[position]);
            holder.name.setTextColor(chefTextColors[position]);
        } else {
            // For any additional chefs beyond 4, cycle through colors
            int colorIndex = position % chefBorderColors.length;
            cardView.setBackgroundResource(chefBorderColors[colorIndex]);
            holder.name.setTextColor(chefTextColors[colorIndex]);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        final RecyclerView recyclerView = holder.mRecyclerView;
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewAdapterOrdersOfCook adapter2 = new RecyclerViewAdapterOrdersOfCook(mContext, mChefs.get(position).getChefQueue(),position);
        adapters.add(adapter2);
        recyclerView.setAdapter(adapters.get(adapters.size()-1));

        if(!mChefs.get(position).isPresent()){
            holder.name.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);
            holder.mRecyclerView.setVisibility(View.GONE);
            // Hide the entire card for absent chefs
            cardView.setVisibility(View.GONE);
        }
        else{
            holder.name.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.mRecyclerView.setVisibility(View.VISIBLE);
            cardView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount()
    {
        return mChefs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView name;
        ImageView imageView;
        RecyclerView mRecyclerView;
        CardView cardView;

        public ViewHolder(View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            imageView = itemView.findViewById(R.id.imageView);
            mRecyclerView = itemView.findViewById(R.id.recyclerview5);
            cardView = (CardView) itemView; // The root view is the CardView
        }
    }
}