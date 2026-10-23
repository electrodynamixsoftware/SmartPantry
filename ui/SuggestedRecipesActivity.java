package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.logic.RecipeMatcher.MatchResult;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.util.AppPrefs;

import java.util.List;

/**
 * Runs the strict-matching rule against the current pantry and lists only the recipes
 * the user can make right now. The optional "Almost There" list is separate.
 */
public class SuggestedRecipesActivity extends BaseActivity {

    private DatabaseHelper db;
    private RecipeAdapter strictAdapter;
    private RecipeAdapter almostAdapter;

    private TextView txtEmpty;
    private RecyclerView recyclerStrict;
    private TextView txtAlmostHeader;
    private RecyclerView recyclerAlmost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setTitle("Suggested Recipes");

        db = new DatabaseHelper(this);
        txtEmpty = findViewById(R.id.txt_empty_recipes);
        recyclerStrict = findViewById(R.id.recycler_strict);
        txtAlmostHeader = findViewById(R.id.txt_almost_header);
        recyclerAlmost = findViewById(R.id.recycler_almost);

        RecipeAdapter.Listener listener = this::openRecipe;
        strictAdapter = new RecipeAdapter(listener);
        almostAdapter = new RecipeAdapter(listener);

        recyclerStrict.setLayoutManager(new LinearLayoutManager(this));
        recyclerStrict.setAdapter(strictAdapter);
        recyclerAlmost.setLayoutManager(new LinearLayoutManager(this));
        recyclerAlmost.setAdapter(almostAdapter);

        setupBottomNav(R.id.nav_suggested);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();   // re-run the matching every time the screen is shown
    }

    private void refresh() {
        List<PantryItem> pantry = db.getAllItems();
        List<Recipe> recipes = db.getAllRecipes();
        RecipeMatcher matcher = new RecipeMatcher(pantry);

        List<MatchResult> strict = matcher.findStrictMatches(recipes);
        strictAdapter.setResults(strict);
        boolean hasStrict = !strict.isEmpty();
        recyclerStrict.setVisibility(hasStrict ? View.VISIBLE : View.GONE);
        txtEmpty.setVisibility(hasStrict ? View.GONE : View.VISIBLE);   // zero-match feedback

        boolean showAlmost = AppPrefs.isAlmostThereEnabled(this);
        List<MatchResult> almost = showAlmost ? matcher.findAlmostThere(recipes) : null;
        boolean hasAlmost = showAlmost && !almost.isEmpty();
        if (hasAlmost) {
            almostAdapter.setResults(almost);
        }
        txtAlmostHeader.setVisibility(hasAlmost ? View.VISIBLE : View.GONE);
        recyclerAlmost.setVisibility(hasAlmost ? View.VISIBLE : View.GONE);
    }

    private void openRecipe(long recipeId) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipeId);
        startActivity(intent);
    }
}
