package com.example.smartpantry.ui;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Shares the bottom navigation bar between the three main screens. */
public abstract class BaseActivity extends AppCompatActivity {

    protected void setupBottomNav(final int selectedItemId) {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(selectedItemId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true;
            }
            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = PantryListActivity.class;
            } else if (id == R.id.nav_suggested) {
                target = SuggestedRecipesActivity.class;
            } else {
                target = SettingsActivity.class;
            }
            Intent intent = new Intent(this, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            return true;
        });
    }
}
