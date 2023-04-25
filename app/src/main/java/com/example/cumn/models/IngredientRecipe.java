package com.example.cumn.models;

import java.util.List;

public class IngredientRecipe {
    private int id;
    private double amount;
    private String unit;
    private String unitLong;
    private String unitShort;
    private String aisle;
    private String name;
    private String original;
    private String originalName;
    private List<String> meta;
    private String image;

    public String getName() {
        return name;
    }

    public String getOriginal() {
        return original;
    }

    public String getImage() {
        return image;
    }
}
