package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.util.AppPrefs;
import com.example.smartpantry.util.FormatUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/** Main screen: lists every pantry item (READ), and leads to add (CREATE), edit (UPDATE) and delete (DELETE). */
public class PantryListActivity extends BaseActivity {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView txtEmpty;
    private TextView txtBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);
        setTitle("My Pantry");

        db = new DatabaseHelper(this);
        txtEmpty = findViewById(R.id.txt_empty);
        txtBanner = findViewById(R.id.txt_expiry_banner);

        RecyclerView recyclerView = findViewById(R.id.recycler_pantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(new PantryAdapter.Listener() {
            @Override
            public void onItemClick(PantryItem item) {
                // Explicit Intent carrying the item id to the Add/Edit screen.
                Intent intent = new Intent(PantryListActivity.this, AddEditItemActivity.class);
                intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(PantryItem item) {
                confirmDelete(item);
            }
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditItemActivity.class)));

        setupBottomNav(R.id.nav_pantry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();   // data may have changed on the Add/Edit or Settings screen
    }

    private void refresh() {
        List<PantryItem> items = db.getAllItems();
        boolean alerts = AppPrefs.isExpiryAlertsEnabled(this);
        adapter.setItems(items, alerts);
        txtEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        int expiring = 0;
        if (alerts) {
            for (PantryItem item : items) {
                if (FormatUtils.daysUntil(item.getExpiry()) <= PantryAdapter.EXPIRING_SOON_DAYS) {
                    expiring++;
                }
            }
        }
        if (expiring > 0) {
            txtBanner.setText("Heads up: " + expiring + (expiring == 1 ? " item is" : " items are")
                    + " expired or expiring within " + PantryAdapter.EXPIRING_SOON_DAYS + " days.");
            txtBanner.setVisibility(View.VISIBLE);
        } else {
            txtBanner.setVisibility(View.GONE);
        }
    }

    private void confirmDelete(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deleteItem(item.getId());
                    Toast.makeText(this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
