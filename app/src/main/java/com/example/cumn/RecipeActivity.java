package com.example.cumn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cumn.adapters.RecipeIngredientsAdapter;
import com.example.cumn.adapters.RecipeAdapter;
import com.example.cumn.api.SpoonacularApi;
import com.example.cumn.models.IngredientR;
import com.example.cumn.models.IngredientRecipe;
import com.example.cumn.models.IngredientsById;
import com.example.cumn.models.Recipe;
import com.example.cumn.models.RecipeByQuery;
import com.example.cumn.models.RecipeByQueryIngredients;
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
    private Retrofit retrofit;
    private SpoonacularApi api;

    private List<RecipeByQueryIngredients> recipeListDialog;
    private AlertDialog alertDialog;
    private Dialog dialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationHandler.setupNavigation(this, bottomNav);
        bottomNav.setSelectedItemId(R.id.button2);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

         database = FirebaseDatabase.getInstance(BuildConfig.DB_URL);
         mauth = FirebaseAuth.getInstance();

        recipeList = new ArrayList<>();
        recipeListDialog = new ArrayList<>();

        TextView textView = findViewById(R.id.title);
        textView.setText("Recipes");

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spoonacular.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(SpoonacularApi.class);


        fetchRecipes();

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(String query) {
                fetchRecipesByQuery(query);
                return false;
            }
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private void fetchRecipesByQuery(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spoonacular.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpoonacularApi api = retrofit.create(SpoonacularApi.class);

        Call<RecipeByQuery> call = api.searchRecipes( BuildConfig.API_TOKEN, query);

        call.enqueue(new Callback<RecipeByQuery>() {
            public void onResponse(Call<RecipeByQuery> call, Response<RecipeByQuery> response) {
                if (response.isSuccessful()) {
                    RecipeByQuery recipes = response.body();
                    Log.d("RecipeActivity", "onResponse: " + recipes);
                    recipeList.clear();

                    for (RecipeByQueryIngredients recipe : recipes.getResults()) {
                        Call<IngredientsById> ingredients = api.getRecipesById(String.valueOf(recipe.getId()), BuildConfig.API_TOKEN);

                        ingredients.enqueue(new Callback<IngredientsById>() {
                            public void onResponse(Call<IngredientsById> call, Response<IngredientsById> response) {
                                if(response.isSuccessful()){
                                    IngredientsById ingredientsById = response.body();
                                    Recipe recipe1 = new Recipe(ingredientsById.getExtendedIngredients(), recipe.getTitle(), recipe.getImage(), recipe.getId());
                                    recipeList.add(recipe1);
                                    adapter.notifyDataSetChanged();

                                }else{
                                    Log.e("RecipeActivity", "onResponse: Failed response with code " + response.code());
                                }
                            }
                            public void onFailure(Call<IngredientsById> call, Throwable t) {
                                Log.e("RecipeActivity", "get recipe onFailure: " + t.getMessage());
                            }
                        });
                    }
                    adapter.notifyDataSetChanged();
                }else{
                    Log.e("RecipeActivity", "onResponse: Failed response with code " + response.code());
                }
            }

            public void onFailure(Call<RecipeByQuery> call, Throwable t) {
                Log.e("RecipeActivity", "search recipes onFailure: " + t.getMessage());
                Log.e("RecipeActivity", "onFailure: " + call);

            }

        });


    }

    private void fetchRecipes() {

        List<String> ingredients = new ArrayList<>();

        database.getReference("/ingredients").child(mauth.getUid()).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("RecipeActivity", "onDataChange: " + snapshot.getValue());
                    IngredientR ingredientR = snapshot.getValue(IngredientR.class);
                    ingredients.add(ingredientR.getName());
                }

                String ingredientsString;

                if (ingredients.size() == 0) {
                    ingredientsString = "pork,rice,carrot,pepper,garlic,ginger,soy sauce,sesame oil,sesame seeds,green onion,egg,water";
                } else {
                    ingredientsString = String.join(",", ingredients);
                }

                Call<List<Recipe>> call = api.findRecipesByIngredients(BuildConfig.API_TOKEN, ingredientsString, 10, false, 2, false);
                call.enqueue(new Callback<List<Recipe>>() {

                    public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                        if (response.isSuccessful()) {
                            recipeList = response.body();
                            adapter = new RecipeAdapter(recipeList, RecipeActivity.this, RecipeActivity.this, null);
                            recyclerView.setAdapter(adapter);

                            for(Recipe recipe: recipeList){
                                for(IngredientRecipe ingredient: recipe.getMissedIngredients()){
                                    String ingredientName = ingredient.getName().substring(ingredient.getName().lastIndexOf("/") + 1);
                                    ingredient.setImage("https://spoonacular.com/cdn/ingredients_250x250/" + ingredientName);
                                }
                            }

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
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.ingredient_dialog);

        RecyclerView recyclerView = dialog.findViewById(R.id.ingredients_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        RecipeIngredientsAdapter adapter = new RecipeIngredientsAdapter(ingredients, this);
        recyclerView.setAdapter(adapter);

        dialog.show();
    }

    public void onCardClick(int position) {
        Recipe recipe = recipeList.get(position);
        List<IngredientRecipe> ingredients = new ArrayList<>();
        ingredients.addAll(recipe.getUsedIngredients());
        ingredients.addAll(recipe.getMissedIngredients());
        ingredients.addAll(recipe.getUnusedIngredients());
        showIngredientDialog(ingredients);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


}