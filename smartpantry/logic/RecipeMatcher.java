package com.example.smartpantry.logic;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THE CORE BUSINESS LOGIC: the strict-matching rule.
 *
 * A recipe is "suggested" only if EVERY ingredient it needs is in the pantry in at
 * least the required quantity. One missing or insufficient ingredient = not suggested.
 *
 * Robustness: names are normalised (singular/plural, case, synonyms) and quantities are
 * converted to base units (g / ml / pcs) so 1 kg in the pantry covers 200 g in a recipe.
 * Duplicate pantry entries for the same ingredient are added together.
 */
public class RecipeMatcher {

    /** Result of checking one recipe against the pantry. */
    public static class MatchResult {
        private final Recipe recipe;
        private final List<RecipeIngredient> missing;

        MatchResult(Recipe recipe, List<RecipeIngredient> missing) {
            this.recipe = recipe;
            this.missing = missing;
        }

        public Recipe getRecipe() { return recipe; }

        /** Ingredients that are absent or present in too small a quantity. */
        public List<RecipeIngredient> getMissing() { return missing; }

        public boolean isStrictMatch() { return missing.isEmpty(); }
    }

    /** normalised name -> total available in base units [mass g, volume ml, count pcs]. */
    private final Map<String, double[]> stock = new HashMap<>();

    public RecipeMatcher(List<PantryItem> pantry) {
        for (PantryItem item : pantry) {
            if (item.getQuantity() <= 0) {
                continue;
            }
            String key = IngredientNameNormalizer.normalise(item.getName());
            double[] totals = stock.get(key);
            if (totals == null) {
                totals = new double[UnitConverter.Dimension.values().length];
                stock.put(key, totals);
            }
            int dimension = UnitConverter.dimensionOf(item.getUnit()).ordinal();
            totals[dimension] += UnitConverter.toBaseUnits(item.getQuantity(), item.getUnit());
        }
    }

    /** Checks every ingredient of the recipe and records the ones that are not covered. */
    public MatchResult evaluate(Recipe recipe) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!isCovered(needed)) {
                missing.add(needed);
            }
        }
        return new MatchResult(recipe, missing);
    }

    private boolean isCovered(RecipeIngredient needed) {
        double[] available = stock.get(IngredientNameNormalizer.normalise(needed.getName()));
        if (available == null) {
            return false;                       // ingredient not in pantry at all
        }
        if (needed.getQuantity() <= 0) {
            return true;                        // "to taste": presence is enough
        }
        int dimension = UnitConverter.dimensionOf(needed.getUnit()).ordinal();
        double required = UnitConverter.toBaseUnits(needed.getQuantity(), needed.getUnit());
        return available[dimension] + 1e-9 >= required;
    }

    /** Recipes the user can cook right now: no missing ingredient at all. */
    public List<MatchResult> findStrictMatches(List<Recipe> recipes) {
        List<MatchResult> results = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.getIngredients().isEmpty()) {
                continue;
            }
            MatchResult result = evaluate(recipe);
            if (result.isStrictMatch()) {
                results.add(result);
            }
        }
        return results;
    }

    /** Bonus: recipes missing exactly one ingredient (always shown separately). */
    public List<MatchResult> findAlmostThere(List<Recipe> recipes) {
        List<MatchResult> results = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult result = evaluate(recipe);
            if (result.getMissing().size() == 1) {
                results.add(result);
            }
        }
        return results;
    }
}
