package com.example.cumn.models;

import java.util.List;

public class IngredientsResponse {
    private List<Ingredient> results;
    private int offset;
    private int number;
    private int totalResults;

    public List<Ingredient> getResults() {
        return results;
    }

    public void setResults(List<Ingredient> results) {
        this.results = results;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
}
