package com.example.cumn.api;

import com.example.cumn.models.IngredientsById;
import com.example.cumn.models.IngredientsResponse;
import com.example.cumn.models.Recipe;
import com.example.cumn.models.RecipeByQuery;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
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

    @GET("recipes/complexSearch")
    Call<RecipeByQuery> searchRecipes(
            @Query("apiKey") String apiKey,
            @Query("query") String query
    );

    @GET("recipes/{id}/information")
    Call<IngredientsById> getRecipesById(
            @Path("id") String id,
            @Query("apiKey") String apiKey
    );

}
