package com.example.smartpantry.model;

import com.example.smartpantry.util.FormatUtils;

/**
 * One ingredient line of a recipe. A quantity of 0 means "to taste"
 * (salt, oil, pepper...), so only the presence of the ingredient is required.
 */
public class RecipeIngredient {
    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }

    /** Human readable text, e.g. "200 g pasta" or "salt (to taste)". */
    public String describe() {
        if (quantity <= 0) {
            return name + " (to taste)";
        }
        if ("pcs".equals(unit)) {
            return FormatUtils.formatQuantity(quantity) + " x " + name;
        }
        return FormatUtils.formatQuantity(quantity) + " " + unit + " " + name;
    }
}
