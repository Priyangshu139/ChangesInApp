package com.dineout.code.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity; // ✅ Updated import
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.dineout.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Activity to select and set the end-of-week day.
 */
public class EndOfWeekActivitiy extends AppCompatActivity {

    private Spinner days;
    private String selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_set_end_of_week);

        days = findViewById(R.id.SelectDayDropDown301);
    }

    public void onClickReg13(View v) {
        selectedItem = days.getSelectedItem().toString();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("EndOfWeek").setValue(selectedItem);

        Toast.makeText(this, "End of week set successfully.", Toast.LENGTH_SHORT).show();
    }
}
