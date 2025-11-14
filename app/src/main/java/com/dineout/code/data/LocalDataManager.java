package com.dineout.code.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class LocalDataManager {
    private static final String PREF_NAME = "restaurant_data";
    private Context context;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public LocalDataManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Save data to local storage
     */
    public <T> void saveData(String key, List<T> data) {
        String json = gson.toJson(data);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, json);
        editor.apply();
    }

    /**
     * Save single object to local storage
     */
    public <T> void saveObject(String key, T object) {
        String json = gson.toJson(object);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, json);
        editor.apply();
    }

    /**
     * Load data from local storage
     */
    public <T> List<T> loadData(String key, Class<T> classType) {
        String json = sharedPreferences.getString(key, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Type listType = TypeToken.getParameterized(List.class, classType).getType();
            List<T> result = gson.fromJson(json, listType);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Load single object from local storage
     */
    public <T> T loadObject(String key, Class<T> classType) {
        String json = sharedPreferences.getString(key, "");
        if (json.isEmpty()) {
            return null;
        }

        try {
            return gson.fromJson(json, classType);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Save key-value pair
     */
    public void saveString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Get string value
     */
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Save integer value
     */
    public void saveInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Get integer value
     */
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    /**
     * Save boolean value
     */
    public void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Get boolean value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Check if key exists
     */
    public boolean containsKey(String key) {
        return sharedPreferences.contains(key);
    }

    /**
     * Remove specific key
     */
    public void removeKey(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * Clear all data
     */
    public void clearAll() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Get all keys
     */
    public java.util.Set<String> getAllKeys() {
        return sharedPreferences.getAll().keySet();
    }

    /**
     * Simulate Firebase push() - generate unique ID
     */
    public String generateUniqueId() {
        return "local_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }

    /**
     * Save data with auto-generated key (like Firebase push)
     */
    public <T> String saveWithGeneratedKey(String prefix, T object) {
        String key = prefix + "_" + generateUniqueId();
        saveObject(key, object);
        return key;
    }
}