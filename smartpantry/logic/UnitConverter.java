package com.example.smartpantry.logic;

import java.util.Locale;

/**
 * Converts the units used in the app to a base unit so quantities can be compared
 * (e.g. 1 kg of rice in the pantry satisfies a recipe that needs 200 g).
 * Base units: grams (mass), millilitres (volume) and pieces (count).
 */
public final class UnitConverter {

    public enum Dimension { MASS, VOLUME, COUNT }

    /** Units offered in the Add/Edit spinner. */
    public static final String[] UNITS = {"g", "kg", "ml", "l", "cup", "tbsp", "tsp", "pcs"};

    private UnitConverter() { }

    private static String clean(String unit) {
        if (unit == null) {
            return "pcs";
        }
        String u = unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "gram": case "grams": case "gm":
                return "g";
            case "kilogram": case "kilograms": case "kgs":
                return "kg";
            case "milliliter": case "milliliters": case "millilitre": case "millilitres": case "mls":
                return "ml";
            case "liter": case "liters": case "litre": case "litres":
                return "l";
            case "cups":
                return "cup";
            case "tablespoon": case "tablespoons": case "tbs":
                return "tbsp";
            case "teaspoon": case "teaspoons":
                return "tsp";
            case "piece": case "pieces": case "pc": case "whole": case "unit": case "units": case "":
                return "pcs";
            default:
                return u;
        }
    }

    public static Dimension dimensionOf(String unit) {
        switch (clean(unit)) {
            case "g": case "kg":
                return Dimension.MASS;
            case "ml": case "l": case "cup": case "tbsp": case "tsp":
                return Dimension.VOLUME;
            default:
                return Dimension.COUNT;
        }
    }

    /** Quantity expressed in the base unit of its dimension (g, ml or pcs). */
    public static double toBaseUnits(double quantity, String unit) {
        switch (clean(unit)) {
            case "kg": case "l":
                return quantity * 1000;
            case "cup":
                return quantity * 240;
            case "tbsp":
                return quantity * 15;
            case "tsp":
                return quantity * 5;
            default:
                return quantity;
        }
    }
}
