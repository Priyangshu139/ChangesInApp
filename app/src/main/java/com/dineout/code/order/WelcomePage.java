package com.dineout.code.order;
import com.dineout.R;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.dineout.code.BaseActivity;
import com.dineout.code.data.LocalOrderManager;

public class WelcomePage extends BaseActivity {

    private LocalOrderManager orderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity_welcome_page);

        orderManager = new LocalOrderManager(this);

        // Ensure orders are cleared for new customer session
        if (orderManager.isFreshSession()) {
            orderManager.startNewCustomerSession();
            Toast.makeText(getApplicationContext(), "Welcome! Fresh order session started", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Continue for Menu!", Toast.LENGTH_LONG).show();
        }
    }

    public void redirectToMenu(View v) {
        startActivity(new Intent(WelcomePage.this, MainActivity.class));
    }
}
