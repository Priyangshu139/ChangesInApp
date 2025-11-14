package com.dineout.code.data;

import android.content.Context;
import com.dineout.code.order.Menu;
import java.util.ArrayList;
import java.util.List;

public class LocalMenuManager {
    private Context context;

    public LocalMenuManager(Context context) {
        this.context = context;
    }

    /**
     * Get menu items with realistic cooking times
     */
    public List<Menu> getMenuWithEstimatedTimes() {
        List<Menu> menuItems = new ArrayList<>();

        // Appetizers (5-10 minutes)
        menuItems.add(new Menu("Caesar Salad", "8", "Appetizer", "180"));
        menuItems.add(new Menu("Garlic Bread", "5", "Appetizer", "120"));
        menuItems.add(new Menu("Chicken Wings", "12", "Appetizer", "250"));
        menuItems.add(new Menu("Soup of the Day", "7", "Appetizer", "150"));

        // Main Courses (15-25 minutes)
        menuItems.add(new Menu("Grilled Chicken", "18", "Main Course", "380"));
        menuItems.add(new Menu("Beef Steak", "22", "Main Course", "550"));
        menuItems.add(new Menu("Fish Fillet", "20", "Main Course", "420"));
        menuItems.add(new Menu("Pasta Alfredo", "15", "Main Course", "320"));
        menuItems.add(new Menu("Vegetarian Pizza", "16", "Main Course", "350"));
        menuItems.add(new Menu("Lamb Curry", "25", "Main Course", "480"));

        // Desserts (3-8 minutes)
        menuItems.add(new Menu("Chocolate Cake", "5", "Dessert", "180"));
        menuItems.add(new Menu("Ice Cream", "3", "Dessert", "100"));
        menuItems.add(new Menu("Tiramisu", "6", "Dessert", "200"));
        menuItems.add(new Menu("Fruit Salad", "4", "Dessert", "120"));

        // Beverages (2-5 minutes)
        menuItems.add(new Menu("Fresh Orange Juice", "3", "Beverage", "80"));
        menuItems.add(new Menu("Coffee", "4", "Beverage", "60"));
        menuItems.add(new Menu("Tea", "3", "Beverage", "50"));
        menuItems.add(new Menu("Smoothie", "5", "Beverage", "120"));

        return menuItems;
    }

    /**
     * Get cooking time for a specific dish by name
     */
    public int getCookingTimeByName(String dishName) {
        List<Menu> menu = getMenuWithEstimatedTimes();
        for (Menu dish : menu) {
            if (dish.getDishName().equalsIgnoreCase(dishName)) {
                try {
                    return Integer.parseInt(dish.getEstimatedTime());
                } catch (NumberFormatException e) {
                    return 15; // Default time if parsing fails
                }
            }
        }
        return 15; // Default time if dish not found
    }

    /**
     * Calculate total estimated time for multiple dishes
     */
    public int calculateTotalTime(List<String> dishNames) {
        int totalTime = 0;
        for (String dishName : dishNames) {
            totalTime += getCookingTimeByName(dishName);
        }
        return totalTime;
    }

    /**
     * Get dish by name
     */
    public Menu getDishByName(String dishName) {
        List<Menu> menu = getMenuWithEstimatedTimes();
        for (Menu dish : menu) {
            if (dish.getDishName().equalsIgnoreCase(dishName)) {
                return dish;
            }
        }
        return null;
    }
}