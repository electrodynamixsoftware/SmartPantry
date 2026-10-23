package com.example.smartpantry.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.smartpantry.R;
import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.util.AppPrefs;
import com.google.android.material.switchmaterial.SwitchMaterial;

/** Settings screen: expiring-soon alerts, optional "Almost There" list, and clearing the pantry. */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");

        SwitchMaterial switchExpiry = findViewById(R.id.switch_expiry);
        SwitchMaterial switchAlmost = findViewById(R.id.switch_almost);
        Button btnClear = findViewById(R.id.btn_clear_pantry);

        switchExpiry.setChecked(AppPrefs.isExpiryAlertsEnabled(this));
        switchAlmost.setChecked(AppPrefs.isAlmostThereEnabled(this));

        switchExpiry.setOnCheckedChangeListener((button, checked) ->
                AppPrefs.setExpiryAlertsEnabled(this, checked));
        switchAlmost.setOnCheckedChangeListener((button, checked) ->
                AppPrefs.setAlmostThereEnabled(this, checked));

        btnClear.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Clear pantry")
                .setMessage("Delete ALL ingredients from your pantry? This cannot be undone.")
                .setPositiveButton("Clear all", (dialog, which) -> {
                    new DatabaseHelper(this).deleteAllItems();
                    Toast.makeText(this, "Pantry cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show());

        setupBottomNav(R.id.nav_settings);
    }
}
