package com.dineout.code;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.dineout.R;

/**
 * Base Activity class that provides common navigation functionality
 * All activities should extend this class to get consistent back button behavior
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        setupBackButton();
    }

    /**
     * Sets up the back button functionality
     * This method looks for common back button IDs and toolbar
     */
    private void setupBackButton() {
        // Enable ActionBar back button if toolbar exists
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Look for the main back button ID that we use consistently
        setupBackButtonById(R.id.back_button);
    }

    /**
     * Helper method to set up back button click listener for a given ID
     */
    private void setupBackButtonById(int buttonId) {
        View backButton = findViewById(buttonId);
        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    /**
     * Handle action bar back button clicks
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Override this method to customize back button behavior in specific activities
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}