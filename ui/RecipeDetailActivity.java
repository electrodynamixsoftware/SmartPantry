package com.example.smartpantry.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

/** Shows the full ingredient list (with a tick/cross against the pantry) and the method for one recipe. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Recipe");

        DatabaseHelper db = new DatabaseHelper(this);
        Recipe recipe = db.getRecipe(getIntent().getLongExtra(EXTRA_RECIPE_ID, -1));
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RecipeMatcher.MatchResult result = new RecipeMatcher(db.getAllItems()).evaluate(recipe);

        ((TextView) findViewById(R.id.txt_recipe_title)).setText(recipe.getName());
        ((TextView) findViewById(R.id.txt_recipe_status)).setText(result.isStrictMatch()
                ? "You have everything you need to cook this now."
                : "Missing " + result.getMissing().size() + " ingredient(s) - not cookable yet.");

        StringBuilder ingredients = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            boolean missing = result.getMissing().contains(ingredient);
            ingredients.append(missing ? "\u2718  " : "\u2714  ")
                    .append(ingredient.describe()).append('\n');
        }
        ((TextView) findViewById(R.id.txt_ingredients)).setText(ingredients.toString().trim());
        ((TextView) findViewById(R.id.txt_steps)).setText(recipe.getSteps());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
