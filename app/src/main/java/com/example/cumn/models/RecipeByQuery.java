package com.example.cumn.models;

import java.util.List;

public class RecipeByQuery {

    List<RecipeByQueryIngredients> results;
    private int offset;
    private int number;
    private int totalResults;

    public List<RecipeByQueryIngredients> getResults() {
        return results;
    }
}
