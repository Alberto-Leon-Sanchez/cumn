package com.example.cumn.models;

public class IngredientR {
    private int id;
    private String name;

    private int quantity;
    private String image;

    public IngredientR(Ingredient ingredient, int quantity) {
        this.id = Integer.valueOf(ingredient.getId());
        this.name = ingredient.getName();
        this.quantity = quantity;
        this.image = ingredient.getImage();
    }

    public IngredientR() {
    }

    public IngredientR(int id, String name, int quantity, String image) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}
