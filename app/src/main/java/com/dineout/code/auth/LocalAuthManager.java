package com.dineout.code.auth;

import android.content.Context;
import android.content.SharedPreferences;
import com.dineout.code.admin.Employee;

import java.util.ArrayList;
import java.util.List;

public class LocalAuthManager {
    private static final String PREF_NAME = "restaurant_auth";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_USER_NAME = "user_name";

    private Context context;
    private SharedPreferences sharedPreferences;

    public LocalAuthManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Hardcoded employee database - replaces Firebase
     */
    private List<Employee> getLocalEmployees() {
        List<Employee> employees = new ArrayList<>();

        // Admin
        employees.add(new Employee("1", "Admin User", "admin@gmail.com", "123456", "None", "50000", "Admin"));

        // Hall Manager
        employees.add(new Employee("2", "Hall Manager", "hallmanager@gmail.com", "123456", "None", "40000", "Hall Manager"));

        // Head Chef
        employees.add(new Employee("3", "Head Chef", "chef@gmail.com", "5678910", "Italian", "45000", "Head Chef"));

        // Additional employees for testing
        employees.add(new Employee("4", "Waiter 1", "waiter1@gmail.com", "password", "None", "25000", "Waiter"));
        employees.add(new Employee("5", "Chef 2", "chef2@gmail.com", "password", "Chinese", "35000", "Chef"));
        employees.add(new Employee("6", "Server 1", "server@gmail.com", "password", "None", "28000", "Server"));

        return employees;
    }

    /**
     * Authenticate user with email and password
     */
    public AuthResult authenticateUser(String email, String password) {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            return new AuthResult(false, null, "Email and password are required");
        }

        List<Employee> employees = getLocalEmployees();

        for (Employee employee : employees) {
            if (employee.getEmail().equalsIgnoreCase(email.trim()) &&
                    employee.getPassword().equals(password.trim())) {

                // Save login state
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_LOGGED_IN, true);
                editor.putString(KEY_USER_EMAIL, employee.getEmail());
                editor.putString(KEY_USER_TYPE, employee.getType());
                editor.putString(KEY_USER_NAME, employee.getName());
                editor.apply();

                return new AuthResult(true, employee, "Login successful");
            }
        }

        return new AuthResult(false, null, "Invalid email or password");
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    /**
     * Get current user email
     */
    public String getCurrentUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Get current user type
     */
    public String getCurrentUserType() {
        return sharedPreferences.getString(KEY_USER_TYPE, "");
    }

    /**
     * Get current user name
     */
    public String getCurrentUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    /**
     * Logout user
     */
    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Get employee by email
     */
    public Employee getEmployeeByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        List<Employee> employees = getLocalEmployees();
        for (Employee employee : employees) {
            if (employee.getEmail().equalsIgnoreCase(email.trim())) {
                return employee;
            }
        }
        return null;
    }

    /**
     * Get all employees (for admin functions)
     */
    public List<Employee> getAllEmployees() {
        return getLocalEmployees();
    }

    /**
     * Add new employee (for admin functions)
     */
    public boolean addEmployee(Employee employee) {
        // In a real implementation, this would save to persistent storage
        // For now, just validate the employee data
        if (employee == null || employee.getEmail() == null || employee.getPassword() == null) {
            return false;
        }

        // Check if employee already exists
        if (getEmployeeByEmail(employee.getEmail()) != null) {
            return false; // Employee already exists
        }

        // In real implementation, save to database
        return true;
    }

    /**
     * Authentication result class
     */
    public static class AuthResult {
        public boolean success;
        public Employee employee;
        public String message;

        public AuthResult(boolean success, Employee employee, String message) {
            this.success = success;
            this.employee = employee;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public Employee getEmployee() {
            return employee;
        }

        public String getMessage() {
            return message;
        }
    }
}