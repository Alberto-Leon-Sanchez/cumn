package com.example.cumn.models;

public class Ingredient {
    private int id;
    private String name;

    private String image;

    public Ingredient(String name) {
        this.name = name;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

}
