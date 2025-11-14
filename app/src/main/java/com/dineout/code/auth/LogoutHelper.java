package com.dineout.code.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.dineout.code.admin.LoginActivity;

public class LogoutHelper {

    /**
     * Logout current user and redirect to login screen
     */
    public static void logout(Context context) {
        LocalAuthManager authManager = new LocalAuthManager(context);
        authManager.logout();

        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to login screen
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        // Finish current activity if it's an Activity
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    /**
     * Check if user is logged in
     */
    public static boolean isLoggedIn(Context context) {
        LocalAuthManager authManager = new LocalAuthManager(context);
        return authManager.isLoggedIn();
    }

    /**
     * Get current user info
     */
    public static String getCurrentUserEmail(Context context) {
        LocalAuthManager authManager = new LocalAuthManager(context);
        return authManager.getCurrentUserEmail();
    }

    /**
     * Get current user type
     */
    public static String getCurrentUserType(Context context) {
        LocalAuthManager authManager = new LocalAuthManager(context);
        return authManager.getCurrentUserType();
    }

    /**
     * Get current user name
     */
    public static String getCurrentUserName(Context context) {
        LocalAuthManager authManager = new LocalAuthManager(context);
        return authManager.getCurrentUserName();
    }
}