package com.example.smartpantry.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.logic.UnitConverter;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.util.FormatUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

/** Form used for both adding a new ingredient and editing an existing one. Includes input validation. */
public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    private DatabaseHelper db;
    private long itemId = -1;

    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputEditText editName;
    private TextInputEditText editQuantity;
    private TextInputEditText editExpiry;
    private Spinner spinnerUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        layoutName = findViewById(R.id.layout_name);
        layoutQuantity = findViewById(R.id.layout_quantity);
        editName = findViewById(R.id.edit_name);
        editQuantity = findViewById(R.id.edit_quantity);
        editExpiry = findViewById(R.id.edit_expiry);
        spinnerUnit = findViewById(R.id.spinner_unit);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnClearExpiry = findViewById(R.id.btn_clear_expiry);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UnitConverter.UNITS);
        spinnerUnit.setAdapter(unitAdapter);

        // If an id was passed in the Intent we are editing; otherwise we are adding.
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            setTitle("Edit Ingredient");
            PantryItem item = db.getItem(itemId);
            if (item == null) {
                Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            editName.setText(item.getName());
            editQuantity.setText(FormatUtils.formatQuantity(item.getQuantity()));
            editExpiry.setText(item.getExpiry());
            for (int i = 0; i < UnitConverter.UNITS.length; i++) {
                if (UnitConverter.UNITS[i].equals(item.getUnit())) {
                    spinnerUnit.setSelection(i);
                }
            }
            btnSave.setText("Update ingredient");
        } else {
            setTitle("Add Ingredient");
        }

        editExpiry.setOnClickListener(v -> showDatePicker());
        btnClearExpiry.setOnClickListener(v -> editExpiry.setText(""));
        btnSave.setOnClickListener(v -> save());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                editExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        layoutName.setError(null);
        layoutQuantity.setError(null);

        String name = editName.getText() == null ? "" : editName.getText().toString().trim();
        String quantityText = editQuantity.getText() == null ? "" : editQuantity.getText().toString().trim();
        String expiry = editExpiry.getText() == null ? "" : editExpiry.getText().toString().trim();

        // ---- input validation ----
        boolean valid = true;
        if (name.isEmpty()) {
            layoutName.setError("Please enter an ingredient name");
            valid = false;
        } else if (!name.matches("[A-Za-z][A-Za-z '\\-]*")) {
            layoutName.setError("Use letters only (spaces and hyphens allowed)");
            valid = false;
        } else if (name.length() > 40) {
            layoutName.setError("Name is too long (max 40 characters)");
            valid = false;
        }

        double quantity = 0;
        if (quantityText.isEmpty()) {
            layoutQuantity.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    layoutQuantity.setError("Quantity must be greater than 0");
                    valid = false;
                } else if (quantity > 100000) {
                    layoutQuantity.setError("Quantity is too large");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                layoutQuantity.setError("Enter a valid number");
                valid = false;
            }
        }
        if (!valid) {
            return;
        }

        PantryItem item = new PantryItem(itemId, name, quantity,
                (String) spinnerUnit.getSelectedItem(), expiry.isEmpty() ? null : expiry);

        if (itemId == -1) {
            db.addItem(item);                       // CREATE
            Toast.makeText(this, name + " added to pantry", Toast.LENGTH_SHORT).show();
        } else {
            db.updateItem(item);                    // UPDATE
            Toast.makeText(this, name + " updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
