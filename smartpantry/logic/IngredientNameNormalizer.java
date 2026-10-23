package com.example.smartpantry.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Makes ingredient names comparable: lower-case, no punctuation, singular form and
 * a few common synonyms. "Tomatoes", "tomato " and "TOMATO" all become "tomato".
 */
public final class IngredientNameNormalizer {

    /** Words that end in s/us/is but are already singular (or irregular). */
    private static final Set<String> KEEP_AS_IS = new HashSet<>(Arrays.asList(
            "hummus", "couscous", "asparagus", "molasses", "lemongrass", "swiss", "citrus", "grits"));

    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        SYNONYMS.put("capsicum", "bell pepper");
        SYNONYMS.put("pepper capsicum", "bell pepper");
        SYNONYMS.put("aubergine", "eggplant");
        SYNONYMS.put("courgette", "zucchini");
        SYNONYMS.put("spring onion", "scallion");
        SYNONYMS.put("green onion", "scallion");
        SYNONYMS.put("coriander", "cilantro");
        SYNONYMS.put("minced meat", "mince");
        SYNONYMS.put("ground beef", "mince");
        SYNONYMS.put("plain flour", "flour");
        SYNONYMS.put("all purpose flour", "flour");
        SYNONYMS.put("caster sugar", "sugar");
        SYNONYMS.put("white sugar", "sugar");
        SYNONYMS.put("white rice", "rice");
        SYNONYMS.put("cooking oil", "oil");
        SYNONYMS.put("vegetable oil", "oil");
        SYNONYMS.put("sunflower oil", "oil");
    }

    private IngredientNameNormalizer() { }

    public static String normalise(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (s.isEmpty()) {
            return s;
        }
        // Only the last word carries the plural: "cherry tomatoes" -> "cherry tomato".
        int lastSpace = s.lastIndexOf(' ');
        String head = lastSpace < 0 ? "" : s.substring(0, lastSpace + 1);
        String last = singularise(s.substring(lastSpace + 1));
        s = head + last;

        String synonym = SYNONYMS.get(s);
        return synonym != null ? synonym : s;
    }

    static String singularise(String word) {
        int n = word.length();
        if (n <= 3 || KEEP_AS_IS.contains(word)) {
            return word;
        }
        if (word.endsWith("ies")) {
            return word.substring(0, n - 3) + "y";          // berries -> berry
        }
        if (word.endsWith("oes")) {
            return word.substring(0, n - 2);                // tomatoes -> tomato
        }
        if (word.endsWith("ches") || word.endsWith("shes") || word.endsWith("xes")
                || word.endsWith("zes") || word.endsWith("sses")) {
            return word.substring(0, n - 2);                // peaches -> peach
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;
        }
        if (word.endsWith("s")) {
            return word.substring(0, n - 1);                // eggs -> egg
        }
        return word;
    }
}
