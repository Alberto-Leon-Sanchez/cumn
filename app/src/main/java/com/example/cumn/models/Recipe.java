package com.example.cumn.models;

import com.example.cumn.models.IngredientRecipe;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private int id;
    private String title;
    private String image;
    private String imageType;
    private int usedIngredientCount;
    private int missedIngredientCount;
    private List<IngredientRecipe> missedIngredients;
    private List<IngredientRecipe> usedIngredients;
    private List<IngredientRecipe> unusedIngredients;
    private int likes;
    public Recipe() {
    }

    public Recipe(int id, String title, String image) {
        this.id = id;
        this.title = title;
        this.image = image;
    }

    public Recipe(List<IngredientRecipe> ingredients, String title, String image, int id) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.missedIngredients = new ArrayList<>();
        this.unusedIngredients = new ArrayList<>();
        this.usedIngredientCount = ingredients.size();
        this.missedIngredientCount = ingredients.size();

        for(IngredientRecipe ingredient : ingredients) {
            ingredient.setImage("https://spoonacular.com/cdn/ingredients_250x250/" + ingredient.getImage());
        }
        this.usedIngredients = ingredients;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public List<IngredientRecipe> getMissedIngredients() {
        return missedIngredients;
    }

    public List<IngredientRecipe> getUsedIngredients() {
        return usedIngredients;
    }

    public List<IngredientRecipe> getUnusedIngredients() {
        return unusedIngredients;
    }

    public int getLikes() {
        return likes;
    }
}
