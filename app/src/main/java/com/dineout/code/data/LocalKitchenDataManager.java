package com.dineout.code.data;

import android.content.Context;
import com.dineout.code.kitchen.models.Chef;
import com.dineout.code.kitchen.models.DishDb;
import com.dineout.code.kitchen.models.EmployeeDb;
import com.dineout.code.kitchen.models.OrderDetailsDb;
import com.dineout.code.kitchen.models.Order;
import com.dineout.code.kitchen.models.AttendanceDb;

import java.util.ArrayList;

public class LocalKitchenDataManager {
    private Context context;

    public LocalKitchenDataManager(Context context) {
        this.context = context;
    }

    /**
     * Get chef data from Firebase backup - creates 4 chefs with proper specialties
     */
    public ArrayList<Chef> getLocalChefs() {
        ArrayList<Chef> chefs = new ArrayList<>();

        // Based on Firebase backup Employee data
        // Chef ID 7: Head Chef (xyz specialty) - but we'll make him Continental for load balancing
        chefs.add(new Chef("Head Chef", "7", new ArrayList<Order>(), "Continental", new ArrayList<OrderDetailsDb>(), true));

        // Chef ID 9: Ali (Continental specialty)
        chefs.add(new Chef("Ali", "9", new ArrayList<Order>(), "Continental", new ArrayList<OrderDetailsDb>(), true));

        // Chef ID 10: Haroon (Desi specialty)  
        chefs.add(new Chef("Haroon", "10", new ArrayList<Order>(), "Desi", new ArrayList<OrderDetailsDb>(), true));

        // Chef ID 11: Hamza (Desi specialty)
        chefs.add(new Chef("Hamza", "11", new ArrayList<Order>(), "Desi", new ArrayList<OrderDetailsDb>(), true));

        // Chef ID 12: Rashid (Desi specialty) - we'll make him Dessert specialist
        chefs.add(new Chef("Rashid", "12", new ArrayList<Order>(), "Dessert", new ArrayList<OrderDetailsDb>(), true));

        return chefs;
    }

    /**
     * Get attendance data - all chefs present by default
     */
    public ArrayList<AttendanceDb> getLocalAttendance() {
        ArrayList<AttendanceDb> attendance = new ArrayList<>();

        // Based on Firebase backup Attendance data
        attendance.add(new AttendanceDb("7", true));  // Head Chef
        attendance.add(new AttendanceDb("9", true));  // Ali
        attendance.add(new AttendanceDb("10", true)); // Haroon  
        attendance.add(new AttendanceDb("11", true)); // Hamza
        attendance.add(new AttendanceDb("12", true)); // Rashid

        return attendance;
    }

    /**
     * Get menu dishes with proper cooking times and types
     */
    public ArrayList<DishDb> getLocalDishes() {
        ArrayList<DishDb> dishes = new ArrayList<>();

        // Based on Firebase backup Menu data with realistic times
        dishes.add(new DishDb("Burger", "20", "500", "Main Course"));
        dishes.add(new DishDb("Chiken baryani", "45", "450", "Desi"));
        dishes.add(new DishDb("Pizza", "20", "800", "Main Course"));
        dishes.add(new DishDb("Fries", "15", "150", "Starter"));
        dishes.add(new DishDb("Hot Dog", "20", "150", "Appetizer"));
        dishes.add(new DishDb("Soup", "20", "500", "Starter"));
        dishes.add(new DishDb("Fish", "30", "300", "sea food"));
        dishes.add(new DishDb("Chocolate", "10", "500", "Desert"));
        dishes.add(new DishDb("Pineapple", "8", "250", "Desert"));
        dishes.add(new DishDb("Strawberries", "5", "100", "Desert"));
        dishes.add(new DishDb("Coconut", "6", "250", "salad"));
        dishes.add(new DishDb("Test dish", "20", "300", "xyz"));

        // Add some from our local menu for consistency
        dishes.add(new DishDb("Grilled Chicken", "18", "380", "Main Course"));
        dishes.add(new DishDb("Beef Steak", "22", "550", "Main Course"));
        dishes.add(new DishDb("Pasta Alfredo", "15", "320", "Main Course"));
        dishes.add(new DishDb("Lamb Curry", "25", "480", "Desi"));

        return dishes;
    }

    /**
     * Create sample orders for testing - multiple orders per chef with realistic distribution
     */
    public ArrayList<OrderDetailsDb> getSampleOrders() {
        ArrayList<OrderDetailsDb> orders = new ArrayList<>();

        // Continental Chef Orders (Head Chef - ID: 7)
        OrderDetailsDb order1 = new OrderDetailsDb("Grilled Chicken", 18, "101", 0, 2, 0);
        order1.setNodeId("node101");
        orders.add(order1);

        OrderDetailsDb order2 = new OrderDetailsDb("Beef Steak", 22, "102", 1, 1, 1);
        order2.setNodeId("node102");
        orders.add(order2);

        OrderDetailsDb order3 = new OrderDetailsDb("Pasta Alfredo", 15, "103", 0, 3, 0);
        order3.setNodeId("node103");
        orders.add(order3);

        // Continental Chef Orders (Ali - ID: 9)
        OrderDetailsDb order4 = new OrderDetailsDb("Burger", 20, "201", 0, 2, 0);
        order4.setNodeId("node201");
        orders.add(order4);

        OrderDetailsDb order5 = new OrderDetailsDb("Pizza", 20, "202", 0, 1, 1);
        order5.setNodeId("node202");
        orders.add(order5);

        OrderDetailsDb order6 = new OrderDetailsDb("Fish", 30, "203", 1, 2, 0);
        order6.setNodeId("node203");
        orders.add(order6);

        OrderDetailsDb order7 = new OrderDetailsDb("Grilled Chicken", 18, "204", 0, 1, 2);
        order7.setNodeId("node204");
        orders.add(order7);

        // Desi Chef Orders (Haroon - ID: 10)
        OrderDetailsDb order8 = new OrderDetailsDb("Chiken baryani", 45, "301", 0, 2, 0);
        order8.setNodeId("node301");
        orders.add(order8);

        OrderDetailsDb order9 = new OrderDetailsDb("Lamb Curry", 25, "302", 1, 1, 1);
        order9.setNodeId("node302");
        orders.add(order9);

        OrderDetailsDb order10 = new OrderDetailsDb("Chiken baryani", 45, "303", 0, 1, 0);
        order10.setNodeId("node303");
        orders.add(order10);

        // Desi Chef Orders (Hamza - ID: 11)
        OrderDetailsDb order11 = new OrderDetailsDb("Lamb Curry", 25, "401", 0, 3, 0);
        order11.setNodeId("node401");
        orders.add(order11);

        OrderDetailsDb order12 = new OrderDetailsDb("Chiken baryani", 45, "402", 0, 1, 1);
        order12.setNodeId("node402");
        orders.add(order12);

        // Dessert Chef Orders (Rashid - ID: 12)
        OrderDetailsDb order13 = new OrderDetailsDb("Chocolate", 10, "501", 0, 2, 0);
        order13.setNodeId("node501");
        orders.add(order13);

        OrderDetailsDb order14 = new OrderDetailsDb("Strawberries", 5, "502", 1, 3, 1);
        order14.setNodeId("node502");
        orders.add(order14);

        OrderDetailsDb order15 = new OrderDetailsDb("Pineapple", 8, "503", 0, 1, 2);
        order15.setNodeId("node503");
        orders.add(order15);

        // Additional Mixed Orders for Load Balancing Testing
        OrderDetailsDb order16 = new OrderDetailsDb("Soup", 20, "601", 0, 2, 0);
        order16.setNodeId("node601");
        orders.add(order16);

        OrderDetailsDb order17 = new OrderDetailsDb("Fries", 15, "602", 0, 4, 0);
        order17.setNodeId("node602");
        orders.add(order17);

        OrderDetailsDb order18 = new OrderDetailsDb("Hot Dog", 20, "603", 1, 2, 1);
        order18.setNodeId("node603");
        orders.add(order18);

        OrderDetailsDb order19 = new OrderDetailsDb("Pizza", 20, "604", 0, 1, 0);
        order19.setNodeId("node604");
        orders.add(order19);

        OrderDetailsDb order20 = new OrderDetailsDb("Coconut", 6, "605", 0, 2, 0);
        order20.setNodeId("node605");
        orders.add(order20);

        return orders;
    }

    /**
     * Apply attendance to chefs
     */
    public void applyAttendanceToChefs(ArrayList<Chef> chefs, ArrayList<AttendanceDb> attendance) {
        for (Chef chef : chefs) {
            for (AttendanceDb att : attendance) {
                if (chef.getId().equals(att.getId())) {
                    chef.setPresent(att.getPresent());
                }
            }
        }
    }

    /**
     * Get employee data for chefs
     */
    public ArrayList<EmployeeDb> getChefEmployees() {
        ArrayList<EmployeeDb> employees = new ArrayList<>();

        // Based on Firebase backup Employee data
        employees.add(new EmployeeDb("7", "Head Chef", "chef@gmail.com", "123456", "50000", "Continental", "Head Chef"));
        employees.add(new EmployeeDb("9", "Ali", "", "", "45000", "Continental", "Chef"));
        employees.add(new EmployeeDb("10", "Haroon", "", "", "50000", "Desi", "Chef"));
        employees.add(new EmployeeDb("11", "Hamza", "", "", "35000", "Desi", "Chef"));
        employees.add(new EmployeeDb("12", "Rashid", "", "", "38000", "Dessert", "Chef"));

        return employees;
    }
}