package com.example.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Local unit tests for the strict-matching rule (Section 2.3 of the brief). */
public class RecipeMatcherTest {

    private static PantryItem item(String name, double qty, String unit) {
        return new PantryItem(0, name, qty, unit, null);
    }

    private static Recipe recipe(RecipeIngredient... ingredients) {
        Recipe r = new Recipe(1, "Test", "steps");
        r.getIngredients().addAll(Arrays.asList(ingredients));
        return r;
    }

    private static List<Recipe> single(Recipe r) {
        List<Recipe> list = new ArrayList<>();
        list.add(r);
        return list;
    }

    @Test
    public void allIngredientsPresent_isSuggested() {
        Recipe r = recipe(new RecipeIngredient("egg", 2, "pcs"), new RecipeIngredient("butter", 10, "g"));
        RecipeMatcher m = new RecipeMatcher(Arrays.asList(item("Eggs", 6, "pcs"), item("Butter", 250, "g")));
        assertEquals(1, m.findStrictMatches(single(r)).size());
    }

    @Test
    public void fiveNeeded_fourPresent_isNotSuggested() {
        Recipe r = recipe(new RecipeIngredient("a", 1, "pcs"), new RecipeIngredient("b", 1, "pcs"),
                new RecipeIngredient("c", 1, "pcs"), new RecipeIngredient("d", 1, "pcs"),
                new RecipeIngredient("e", 1, "pcs"));
        RecipeMatcher m = new RecipeMatcher(Arrays.asList(item("a", 1, "pcs"), item("b", 1, "pcs"),
                item("c", 1, "pcs"), item("d", 1, "pcs")));
        assertTrue(m.findStrictMatches(single(r)).isEmpty());
        assertEquals(1, m.findAlmostThere(single(r)).size());
    }

    @Test
    public void singularPluralAndCase_areEquivalent() {
        Recipe r = recipe(new RecipeIngredient("tomato", 2, "pcs"));
        assertTrue(new RecipeMatcher(Arrays.asList(item("  TOMATOES ", 3, "pcs")))
                .evaluate(r).isStrictMatch());
    }

    @Test
    public void unitDifferences_areConverted() {
        Recipe r = recipe(new RecipeIngredient("flour", 150, "g"));
        assertTrue(new RecipeMatcher(Arrays.asList(item("Flour", 1, "kg"))).evaluate(r).isStrictMatch());
    }

    @Test
    public void insufficientQuantity_isNotSuggested() {
        Recipe r = recipe(new RecipeIngredient("milk", 250, "ml"));
        assertFalse(new RecipeMatcher(Arrays.asList(item("milk", 100, "ml"))).evaluate(r).isStrictMatch());
    }

    @Test
    public void duplicateEntries_areAddedTogether() {
        Recipe r = recipe(new RecipeIngredient("rice", 200, "g"));
        RecipeMatcher m = new RecipeMatcher(Arrays.asList(item("rice", 120, "g"), item("Rice", 100, "g")));
        assertTrue(m.evaluate(r).isStrictMatch());
    }

    @Test
    public void toTasteIngredient_onlyNeedsPresence() {
        Recipe r = recipe(new RecipeIngredient("salt", 0, "g"));
        assertTrue(new RecipeMatcher(Arrays.asList(item("salt", 1, "tsp"))).evaluate(r).isStrictMatch());
        assertFalse(new RecipeMatcher(new ArrayList<PantryItem>()).evaluate(r).isStrictMatch());
    }

    @Test
    public void emptyPantry_suggestsNothing() {
        Recipe r = recipe(new RecipeIngredient("egg", 1, "pcs"));
        assertTrue(new RecipeMatcher(new ArrayList<PantryItem>()).findStrictMatches(single(r)).isEmpty());
    }
}
