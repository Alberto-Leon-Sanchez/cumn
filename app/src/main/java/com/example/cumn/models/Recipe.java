package com.example.cumn.models;

import com.example.cumn.models.IngredientRecipe;

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


    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }
}
