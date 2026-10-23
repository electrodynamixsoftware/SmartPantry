package com.example.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Local SQLite database (via SQLiteOpenHelper).
 *
 * Tables:
 *   pantry             - the user's ingredients (full CRUD)
 *   recipes            - seeded once, on first run
 *   recipe_ingredients - the ingredients each recipe needs (FK -> recipes)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    private static final String T_PANTRY = "pantry";
    private static final String T_RECIPES = "recipes";
    private static final String T_RECIPE_ING = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry TEXT)");
        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + T_RECIPE_ING + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES " + T_RECIPES + "(_id) ON DELETE CASCADE)");
        seedRecipes(db);   // pre-load the recipe collection on first run
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    // ------------------------------------------------------------------
    // Pantry CRUD
    // ------------------------------------------------------------------

    /** CREATE */
    public long addItem(PantryItem item) {
        return getWritableDatabase().insert(T_PANTRY, null, toValues(item));
    }

    /** READ (all) */
    public List<PantryItem> getAllItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_PANTRY, null, null, null, null, null, "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                items.add(itemFromCursor(c));
            }
        }
        return items;
    }

    /** READ (one) */
    public PantryItem getItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_PANTRY, null, "_id = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            return c.moveToFirst() ? itemFromCursor(c) : null;
        }
    }

    /** UPDATE */
    public int updateItem(PantryItem item) {
        return getWritableDatabase().update(T_PANTRY, toValues(item), "_id = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE */
    public int deleteItem(long id) {
        return getWritableDatabase().delete(T_PANTRY, "_id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteAllItems() {
        getWritableDatabase().delete(T_PANTRY, null, null);
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        if (item.getExpiry() == null || item.getExpiry().isEmpty()) {
            values.putNull("expiry");
        } else {
            values.put("expiry", item.getExpiry());
        }
        return values;
    }

    private PantryItem itemFromCursor(Cursor c) {
        int expiryIndex = c.getColumnIndexOrThrow("expiry");
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow("_id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.isNull(expiryIndex) ? null : c.getString(expiryIndex));
    }

    // ------------------------------------------------------------------
    // Recipes (read only after seeding)
    // ------------------------------------------------------------------

    /** All recipes with their ingredients, ordered by name. */
    public List<Recipe> getAllRecipes() {
        Map<Long, Recipe> recipes = new LinkedHashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RECIPES, null, null, null, null, null, "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow("_id"));
                recipes.put(id, new Recipe(id,
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("steps"))));
            }
        }
        try (Cursor c = db.query(T_RECIPE_ING, null, null, null, null, null, "recipe_id, _id")) {
            while (c.moveToNext()) {
                Recipe recipe = recipes.get(c.getLong(c.getColumnIndexOrThrow("recipe_id")));
                if (recipe != null) {
                    recipe.getIngredients().add(ingredientFromCursor(c));
                }
            }
        }
        return new ArrayList<>(recipes.values());
    }

    /** One recipe (with ingredients) or null if the id does not exist. */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Recipe recipe;
        try (Cursor c = db.query(T_RECIPES, null, "_id = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (!c.moveToFirst()) {
                return null;
            }
            recipe = new Recipe(id,
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("steps")));
        }
        try (Cursor c = db.query(T_RECIPE_ING, null, "recipe_id = ?", new String[]{String.valueOf(id)},
                null, null, "_id")) {
            while (c.moveToNext()) {
                recipe.getIngredients().add(ingredientFromCursor(c));
            }
        }
        return recipe;
    }

    private RecipeIngredient ingredientFromCursor(Cursor c) {
        return new RecipeIngredient(
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")));
    }

    // ------------------------------------------------------------------
    // Seed data (21 recipes). Quantity 0 = "to taste" (presence only).
    // ------------------------------------------------------------------

    private static RecipeIngredient ing(String name, double qty, String unit) {
        return new RecipeIngredient(name, qty, unit);
    }

    private void addRecipe(SQLiteDatabase db, String name, String steps, RecipeIngredient... ingredients) {
        ContentValues recipe = new ContentValues();
        recipe.put("name", name);
        recipe.put("steps", steps);
        long recipeId = db.insert(T_RECIPES, null, recipe);
        for (RecipeIngredient i : ingredients) {
            ContentValues row = new ContentValues();
            row.put("recipe_id", recipeId);
            row.put("name", i.getName());
            row.put("quantity", i.getQuantity());
            row.put("unit", i.getUnit());
            db.insert(T_RECIPE_ING, null, row);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Toast the bread.\n2. Whisk the eggs with a pinch of salt.\n3. Melt the butter in a pan on low heat.\n4. Stir the eggs gently until just set.\n5. Spoon over the toast and serve.",
                ing("egg", 3, "pcs"), ing("bread", 2, "pcs"), ing("butter", 10, "g"), ing("salt", 0, "g"));

        addRecipe(db, "Cheese Omelette",
                "1. Beat the eggs with salt.\n2. Melt butter in a pan over medium heat.\n3. Pour in the eggs and let the base set.\n4. Add grated cheese, fold in half and cook 1 more minute.",
                ing("egg", 3, "pcs"), ing("cheese", 50, "g"), ing("butter", 10, "g"), ing("salt", 0, "g"));

        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta in salted water until al dente.\n2. Chop tomatoes and garlic.\n3. Fry garlic in oil for 1 minute, add tomatoes and simmer 10 minutes.\n4. Toss the drained pasta in the sauce.",
                ing("pasta", 200, "g"), ing("tomato", 4, "pcs"), ing("garlic", 2, "pcs"),
                ing("olive oil", 0, "ml"), ing("salt", 0, "g"));

        addRecipe(db, "Egg Fried Rice",
                "1. Cook the rice and let it cool.\n2. Fry chopped onion in oil until soft.\n3. Push aside, scramble the eggs in the pan.\n4. Add rice and soy sauce and stir-fry for 3 minutes.",
                ing("rice", 200, "g"), ing("egg", 2, "pcs"), ing("onion", 1, "pcs"),
                ing("soy sauce", 20, "ml"), ing("oil", 0, "ml"));

        addRecipe(db, "Plain Pancakes",
                "1. Whisk flour, sugar, milk and egg into a smooth batter.\n2. Melt a little butter in a pan.\n3. Pour in a ladle of batter and cook until bubbles form.\n4. Flip and cook the other side until golden.",
                ing("flour", 150, "g"), ing("milk", 250, "ml"), ing("egg", 1, "pcs"),
                ing("sugar", 20, "g"), ing("butter", 20, "g"));

        addRecipe(db, "Creamy Mashed Potatoes",
                "1. Peel and cut the potatoes.\n2. Boil in salted water for 15-20 minutes until soft.\n3. Drain and mash with butter and warm milk.\n4. Season with salt.",
                ing("potato", 500, "g"), ing("butter", 30, "g"), ing("milk", 100, "ml"), ing("salt", 0, "g"));

        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of both slices of bread.\n2. Put the cheese between the slices.\n3. Toast in a pan for 2-3 minutes per side until golden.",
                ing("bread", 2, "pcs"), ing("cheese", 60, "g"), ing("butter", 10, "g"));

        addRecipe(db, "Vegetable Stir-Fry",
                "1. Slice the carrots, onion and bell pepper.\n2. Heat oil in a pan on high heat.\n3. Stir-fry the carrots first, then the rest for 5 minutes.\n4. Add soy sauce and toss for 1 minute.",
                ing("carrot", 2, "pcs"), ing("onion", 1, "pcs"), ing("bell pepper", 1, "pcs"),
                ing("soy sauce", 30, "ml"), ing("oil", 0, "ml"));

        addRecipe(db, "Tuna Pasta",
                "1. Boil the pasta until al dente.\n2. Fry chopped onion in olive oil until soft.\n3. Add the drained tuna and warm through.\n4. Toss with the pasta and season with salt.",
                ing("pasta", 200, "g"), ing("tuna", 1, "pcs"), ing("onion", 1, "pcs"),
                ing("olive oil", 0, "ml"), ing("salt", 0, "g"));

        addRecipe(db, "Chicken and Rice",
                "1. Dice the chicken and season with salt.\n2. Brown the chicken with chopped onion and garlic.\n3. Add rice and twice its volume of water.\n4. Cover and simmer for 15 minutes until the rice is cooked.",
                ing("chicken breast", 300, "g"), ing("rice", 200, "g"), ing("onion", 1, "pcs"),
                ing("garlic", 2, "pcs"), ing("salt", 0, "g"));

        addRecipe(db, "Simple Tomato Soup",
                "1. Fry chopped onion and garlic until soft.\n2. Add chopped tomatoes and stock.\n3. Simmer for 20 minutes.\n4. Blend until smooth and season with salt.",
                ing("tomato", 6, "pcs"), ing("onion", 1, "pcs"), ing("garlic", 2, "pcs"),
                ing("vegetable stock", 500, "ml"), ing("salt", 0, "g"));

        addRecipe(db, "Banana Pancakes",
                "1. Mash the bananas in a bowl.\n2. Whisk in the eggs and flour.\n3. Cook spoonfuls of batter in a lightly oiled pan.\n4. Flip when bubbles appear and cook until golden.",
                ing("banana", 2, "pcs"), ing("egg", 2, "pcs"), ing("flour", 60, "g"));

        addRecipe(db, "Banana Oatmeal",
                "1. Simmer the oats in milk for 5 minutes, stirring.\n2. Slice the banana on top.\n3. Drizzle with honey.",
                ing("oat", 80, "g"), ing("milk", 300, "ml"), ing("banana", 1, "pcs"), ing("honey", 15, "ml"));

        addRecipe(db, "Spanish Potato Omelette",
                "1. Thinly slice potatoes and onion.\n2. Fry them slowly in oil until soft.\n3. Mix with beaten eggs and salt.\n4. Cook in the pan until set, flipping once.",
                ing("potato", 300, "g"), ing("egg", 4, "pcs"), ing("onion", 1, "pcs"),
                ing("oil", 0, "ml"), ing("salt", 0, "g"));

        addRecipe(db, "Beans on Toast",
                "1. Heat the baked beans in a pan.\n2. Toast and butter the bread.\n3. Pour the beans over the toast.",
                ing("baked bean", 400, "g"), ing("bread", 2, "pcs"), ing("butter", 10, "g"));

        addRecipe(db, "Macaroni Cheese",
                "1. Boil the macaroni until tender.\n2. Melt butter, stir in flour, then slowly whisk in the milk to make a sauce.\n3. Add the grated cheese until melted.\n4. Stir in the macaroni.",
                ing("macaroni", 250, "g"), ing("cheese", 150, "g"), ing("milk", 300, "ml"),
                ing("butter", 30, "g"), ing("flour", 30, "g"));

        addRecipe(db, "Chicken Noodle Stir-Fry",
                "1. Cook the noodles and drain.\n2. Slice the chicken and carrot.\n3. Stir-fry the chicken until cooked, add the carrot.\n4. Add noodles and soy sauce and toss together.",
                ing("chicken breast", 250, "g"), ing("noodle", 200, "g"), ing("carrot", 1, "pcs"),
                ing("soy sauce", 30, "ml"));

        addRecipe(db, "French Toast",
                "1. Whisk eggs, milk and sugar in a shallow dish.\n2. Dip each slice of bread on both sides.\n3. Fry in butter until golden on both sides.",
                ing("bread", 4, "pcs"), ing("egg", 2, "pcs"), ing("milk", 100, "ml"),
                ing("sugar", 10, "g"), ing("butter", 10, "g"));

        addRecipe(db, "Spinach and Cheese Scramble",
                "1. Wilt the spinach in a hot pan for 1 minute.\n2. Pour in the beaten eggs and stir gently.\n3. Add cheese just before the eggs set and season with salt.",
                ing("spinach", 100, "g"), ing("egg", 3, "pcs"), ing("cheese", 30, "g"), ing("salt", 0, "g"));

        addRecipe(db, "Hearty Lentil Soup",
                "1. Fry chopped onion, carrots and garlic until soft.\n2. Add the lentils and stock.\n3. Simmer for 30 minutes until the lentils are tender.\n4. Season with salt.",
                ing("lentil", 200, "g"), ing("onion", 1, "pcs"), ing("carrot", 2, "pcs"), ing("garlic", 2, "pcs"),
                ing("vegetable stock", 750, "ml"), ing("salt", 0, "g"));

        addRecipe(db, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n2. Spread with peanut butter.\n3. Top with sliced banana.",
                ing("bread", 2, "pcs"), ing("peanut butter", 30, "g"), ing("banana", 1, "pcs"));
    }
}
