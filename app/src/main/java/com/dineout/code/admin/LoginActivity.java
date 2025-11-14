package com.dineout.code.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dineout.R;
import com.dineout.code.BaseActivity;
import com.dineout.code.auth.LocalAuthManager;

public class LoginActivity extends BaseActivity {

    private EditText txtEmailLogin;
    private EditText txtPwd;
    private LocalAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_login);

        txtEmailLogin = (EditText) findViewById(R.id.EmployeeEmail301);
        txtPwd = (EditText) findViewById(R.id.EmployeePassword301);
        authManager = new LocalAuthManager(this);

        // Check if user is already logged in
        if (authManager.isLoggedIn()) {
            redirectToAppropriateInterface(authManager.getCurrentUserEmail(), authManager.getCurrentUserType());
        }
    }

    public void btnUserLogin_Click(View v) {
        boolean go = true;

        if (txtEmailLogin.getText().toString().trim().isEmpty()) {
            txtEmailLogin.setError("Email is required");
            go = false;
        }

        if (txtPwd.getText().toString().trim().isEmpty()) {
            txtPwd.setError("Password is required");
            go = false;
        }

        if (go) {
            final ProgressDialog progressDialog = ProgressDialog.show(
                    LoginActivity.this,
                    "Please wait...",
                    "Authenticating...",
                    true
            );

            // Simulate authentication delay for realistic experience
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();

                    String email = txtEmailLogin.getText().toString().trim();
                    String password = txtPwd.getText().toString().trim();

                    LocalAuthManager.AuthResult result = authManager.authenticateUser(email, password);

                    if (result.success) {
                        Toast.makeText(LoginActivity.this, result.message, Toast.LENGTH_SHORT).show();
                        redirectToAppropriateInterface(result.employee.getEmail(), result.employee.getType());
                    } else {
                        Toast.makeText(LoginActivity.this, result.message, Toast.LENGTH_LONG).show();
                    }
                }
            }, 1500); // 1.5 second delay to simulate authentication
        }
    }

    private void redirectToAppropriateInterface(String email, String userType) {
        Intent intent = null;

        switch (userType) {
            case "Admin":
                intent = new Intent(LoginActivity.this, AdminPanelActivity.class);
                break;

            case "Hall Manager":
                intent = new Intent(LoginActivity.this, com.dineout.code.hall.ManagerInterface.class);
                break;

            case "Head Chef":
                intent = new Intent(LoginActivity.this, com.dineout.code.kitchen.MainActivity.class);
                break;

            default:
                Toast.makeText(this, "User type not supported: " + userType, Toast.LENGTH_LONG).show();
                return;
        }

        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Add logout functionality that can be called from other activities
     */
    public void logout(View v) {
        authManager.logout();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        // Redirect to login if needed
    }
}
