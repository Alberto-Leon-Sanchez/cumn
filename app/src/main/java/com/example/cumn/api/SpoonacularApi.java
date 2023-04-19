package com.example.cumn.api;

import com.example.cumn.models.IngredientsResponse;
import com.example.cumn.models.Recipe;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SpoonacularApi {

    @GET("food/ingredients/search")
    Call<IngredientsResponse> searchIngredients(@Query("apiKey") String apiKey, @Query("query") String query);

    @GET("recipes/findByIngredients")
    Call<List<Recipe>> findRecipesByIngredients(
            @Query("apiKey") String apiKey,
            @Query("ingredients") String ingredients,
            @Query("number") int number,
            @Query("limitLicense") boolean limitLicense,
            @Query("ranking") int ranking,
            @Query("ignorePantry") boolean ignorePantry
    );
}
