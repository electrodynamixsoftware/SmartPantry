package com.smartpantry.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recipes")
public class Recipe {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String ingredients;
    private String instructions;
    private String prepTime;

    public Recipe(String title, String ingredients, String instructions, String prepTime) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.prepTime = prepTime;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getPrepTime() { return prepTime; }
    public void setPrepTime(String prepTime) { this.prepTime = prepTime; }
}
