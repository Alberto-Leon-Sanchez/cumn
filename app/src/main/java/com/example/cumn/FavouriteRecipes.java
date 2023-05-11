package com.example.cumn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;

import com.example.cumn.adapters.RecipeAdapter;
import com.example.cumn.adapters.RecipeIngredientsAdapter;
import com.example.cumn.models.IngredientRecipe;
import com.example.cumn.models.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavouriteRecipes extends AppCompatActivity implements RecipeAdapter.OnCardClickListener {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> savedRecipes = new ArrayList<>();
    private FirebaseDatabase database;
    private FirebaseAuth mauth;
    private Dialog dialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_recipes);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationHandler.setupNavigation(this, bottomNav);
        bottomNav.setSelectedItemId(R.id.button3);

        recyclerView = findViewById(R.id.favourite_recipes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        TextView textView = findViewById(R.id.title);
        textView.setText("Favourite Recipes");

        database = FirebaseDatabase.getInstance(BuildConfig.DB_URL);
        mauth = FirebaseAuth.getInstance();

        String userId = mauth.getCurrentUser().getUid();
        DatabaseReference userRecipesRef = database.getReference("/users_recipes").child(userId);

        userRecipesRef.addValueEventListener(new ValueEventListener() {

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                savedRecipes.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    savedRecipes.add(recipe);
                }
                List<Recipe> cloneRecipes = new ArrayList<>();
                cloneRecipes.addAll(savedRecipes);
                adapter = new RecipeAdapter(savedRecipes, FavouriteRecipes.this, FavouriteRecipes.this, cloneRecipes );
                recyclerView.setAdapter(adapter);
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FavouriteRecipes", "Error loading recipes: " + databaseError.getMessage());
            }
        });


        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filter(newText);
                }
                return false;
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
        Recipe recipe = savedRecipes.get(position);
        List<IngredientRecipe> ingredients = new ArrayList<>();

        if (recipe.getUsedIngredients() != null) {
            ingredients.addAll(recipe.getUsedIngredients());
        }
        if (recipe.getMissedIngredients() != null) {
            ingredients.addAll(recipe.getMissedIngredients());
        }
        if (recipe.getUnusedIngredients() != null) {
            ingredients.addAll(recipe.getUnusedIngredients());
        }

        showIngredientDialog(ingredients);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
