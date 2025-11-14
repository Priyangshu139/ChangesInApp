package com.dineout.code.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.dineout.R;
import com.dineout.code.BaseActivity;
import com.dineout.code.data.LocalOrderManager;
import com.dineout.code.data.FirebaseDataCleaner;

public class HomeActivity extends BaseActivity {

    private LocalOrderManager orderManager;
    private FirebaseDataCleaner firebaseCleaner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_home);

        orderManager = new LocalOrderManager(this);
        firebaseCleaner = new FirebaseDataCleaner(this);
    }

    //login for admin, chef, hall manager
    public void onClickReg(View v) {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    //customer's interface - now clears previous orders
    public void onClickReg2(View v) {
        // Clear all previous orders from both local and Firebase storage
        orderManager.startNewCustomerSession();
        firebaseCleaner.clearFirebaseDataWithFeedback();

        // Show confirmation to user
        Toast.makeText(this, "Welcome! All previous orders cleared - fresh start!", Toast.LENGTH_LONG).show();

        // Start customer flow
        Intent i = new Intent(this, com.dineout.code.order.WelcomePage.class);
        startActivity(i);
    }
}
