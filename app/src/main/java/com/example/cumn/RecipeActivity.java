package com.example.cumn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import com.example.cumn.adapters.RecipeIngredientsAdapter;
import com.example.cumn.adapters.RecipeAdapter;
import com.example.cumn.api.SpoonacularApi;
import com.example.cumn.models.IngredientR;
import com.example.cumn.models.IngredientRecipe;
import com.example.cumn.models.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecipeActivity extends AppCompatActivity implements RecipeAdapter.OnCardClickListener{

    private RecyclerView recyclerView;
    private List<Recipe> recipeList;
    private RecipeAdapter adapter;
    private FirebaseDatabase database;
    private FirebaseAuth mauth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationHandler.setupNavigation(this, bottomNav);
        bottomNav.setSelectedItemId(R.id.button2);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

         database = FirebaseDatabase.getInstance("https://cumn-cc19b-default-rtdb.europe-west1.firebasedatabase.app");
         mauth = FirebaseAuth.getInstance();

        recipeList = new ArrayList<>();
        fetchRecipes();

    }
    private void fetchRecipes() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spoonacular.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpoonacularApi api = retrofit.create(SpoonacularApi.class);

        List<String> ingredients = new ArrayList<>();

        database.getReference("/users/" + mauth.getUid() + "/cards").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    IngredientR ingredientR = snapshot.getValue(IngredientR.class);
                    ingredients.add(ingredientR.getName());
                }

                String ingredientsString;

                if (ingredients.size() == 0) {
                    ingredientsString = "pork,rice,carrot,pepper,garlic,ginger,soy sauce,sesame oil,sesame seeds,green onion,egg,water";
                } else {
                    ingredientsString = String.join(",", ingredients);
                }

                Call<List<Recipe>> call = api.findRecipesByIngredients("05b03e6e38be4044854648b70d8b126e", ingredientsString, 10, false, 2, false);
                call.enqueue(new Callback<List<Recipe>>() {

                    public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                        if (response.isSuccessful()) {
                            recipeList = response.body();
                            adapter = new RecipeAdapter(recipeList, RecipeActivity.this, RecipeActivity.this);
                            recyclerView.setAdapter(adapter);

                        } else {
                            Log.e("TAG", "Error fetching recipes: " + response.code());
                        }
                    }

                    public void onFailure(Call<List<Recipe>> call, Throwable t) {
                        Log.e("TAG", "Error fetching recipes: " + t.getMessage());
                    }
                });
            }

            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("TAG", "Failed to read value.", error.toException());
            }
        });
    }

    private void showIngredientDialog(List<IngredientRecipe> ingredients) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.ingredient_dialog);

        RecyclerView recyclerView = dialog.findViewById(R.id.ingredients_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        RecipeIngredientsAdapter adapter = new RecipeIngredientsAdapter(ingredients, this);
        recyclerView.setAdapter(adapter);

        dialog.show();
    }

    @Override
    public void onCardClick(int position) {
        Recipe recipe = recipeList.get(position);
        List<IngredientRecipe> ingredients = new ArrayList<>();
        ingredients.addAll(recipe.getUsedIngredients());
        ingredients.addAll(recipe.getMissedIngredients());
        ingredients.addAll(recipe.getUnusedIngredients());
        showIngredientDialog(ingredients);
    }


}