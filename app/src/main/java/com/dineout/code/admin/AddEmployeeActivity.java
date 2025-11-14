package com.dineout.code.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dineout.R;
import com.dineout.code.BaseActivity;
import com.dineout.code.auth.LocalAuthManager;
import com.dineout.code.data.LocalDataManager;

import java.util.ArrayList;

public class AddEmployeeActivity extends BaseActivity {

    EditText name, email, password, specialty, salary;
    Spinner type;
    TextView special;
    Button addButton;
    Long it;
    static ArrayList<Long> eid = new ArrayList<>();
    boolean check = false;
    boolean check1 = true;
    ArrayList<Employee> E = new ArrayList<>();
    String e;
    String p;

    private ListView listView;
    private LocalDataManager dataManager;
    private LocalAuthManager authManager;
    private IngredientsListAdapter ingredientsListAdapter;
    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<IngredientRow> ingredients = new ArrayList<>();
    private ArrayList<String> notifications1 = new ArrayList<>();
    private ArrayList<String> notifications2 = new ArrayList<>();

    int idz = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_add_employee);

        // Initialize local managers
        dataManager = new LocalDataManager(this);
        authManager = new LocalAuthManager(this);

        name = findViewById(R.id.AddEmployeeName300);
        type = findViewById(R.id.DropDownMenu300);
        email = findViewById(R.id.AddEmployeeEmail300);
        password = findViewById(R.id.AddEmployeePassword300);
        specialty = findViewById(R.id.AddSpeciality300);
        salary = findViewById(R.id.AddEmployeeSalary300);
        special = findViewById(R.id.specialityLabel300);
        addButton = findViewById(R.id.AddNewEmployeeButton300);

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Chef") || selectedItem.equals("Head Chef")) {
                    specialty.setEnabled(true);
                    specialty.setInputType(InputType.TYPE_CLASS_TEXT);
                    specialty.setFocusable(true);
                    specialty.setFocusableInTouchMode(true);
                    special.setEnabled(true);
                } else {
                    specialty.setEnabled(false);
                    specialty.setInputType(InputType.TYPE_NULL);
                    specialty.setFocusable(false);
                    special.setEnabled(false);
                }

                if (selectedItem.equals("Hall Manager") || selectedItem.equals("Head Chef")) {
                    email.setEnabled(true);
                    email.setInputType(InputType.TYPE_CLASS_TEXT);
                    email.setFocusable(true);
                    email.setFocusableInTouchMode(true);

                    password.setEnabled(true);
                    password.setInputType(InputType.TYPE_CLASS_TEXT);
                    password.setFocusable(true);
                    password.setFocusableInTouchMode(true);
                    check = true;
                } else {
                    email.setEnabled(false);
                    email.setInputType(InputType.TYPE_NULL);
                    email.setFocusable(false);

                    password.setEnabled(false);
                    password.setInputType(InputType.TYPE_NULL);
                    password.setFocusable(false);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) { }
        });

        addButton.setOnClickListener(v -> {
            String specialtyText;
            boolean go = true;

            if (!type.getSelectedItem().toString().equals("Chef")) {
                specialtyText = "None";
            } else {
                specialtyText = specialty.getText().toString();
            }

            if (name.getText().toString().isEmpty()) {
                name.setError("Name is Required");
                go = false;
            }

            if (email.getText().toString().isEmpty() &&
                    (type.getSelectedItem().toString().equals("Head Chef") ||
                            type.getSelectedItem().toString().equals("Hall Manager"))) {
                email.setError("Email Address is Required");
                go = false;
            }

            if (password.getText().toString().isEmpty() &&
                    (type.getSelectedItem().toString().equals("Head Chef") ||
                            type.getSelectedItem().toString().equals("Hall Manager"))) {
                password.setError("Password is Required");
                go = false;
            }

            if (!salary.getText().toString().isEmpty() &&
                    Integer.parseInt(salary.getText().toString()) < 1) {
                salary.setError("Salary must be greater than 0");
                go = false;
            }

            if (specialty.getText().toString().isEmpty() &&
                    type.getSelectedItem().toString().equals("Chef")) {
                specialty.setError("Specialty is Required");
                go = false;
            }

            if (go) {
                // Create new employee using local storage
                String employeeId = dataManager.generateUniqueId();

                Employee newEmployee = new Employee(
                        employeeId,
                        name.getText().toString(),
                        email.getText().toString(),
                        password.getText().toString(),
                        specialtyText,
                        salary.getText().toString(),
                        type.getSelectedItem().toString()
                );

                // Save employee to local storage
                ArrayList<Employee> employees = (ArrayList<Employee>) dataManager.loadData("employees", Employee.class);
                if (employees == null) {
                    employees = new ArrayList<>();
                }
                employees.add(newEmployee);
                dataManager.saveData("employees", employees);

                Toast.makeText(AddEmployeeActivity.this, "Employee Added Successfully", Toast.LENGTH_SHORT).show();

                // Return to admin panel
                Intent intent = new Intent(AddEmployeeActivity.this, AdminPanelActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
