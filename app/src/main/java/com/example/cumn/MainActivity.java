package com.example.cumn;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cumn.adapters.SafeIngredientsAdapter;
import com.example.cumn.adapters.SearchIngredientAdapter;
import com.example.cumn.api.SpoonacularApi;
import com.example.cumn.models.Ingredient;
import com.example.cumn.models.IngredientR;
import com.example.cumn.models.IngredientsResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SafeIngredientsAdapter safeIngredientsAdapter;
    private List<IngredientR> cardTextList;
    private FirebaseDatabase database;
    private FirebaseAuth mauth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, SignInSignUpActivity.class));
            finish();
        }


        database = FirebaseDatabase.getInstance(BuildConfig.DB_URL);
        mauth = FirebaseAuth.getInstance();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationHandler.setupNavigation(this, bottomNav);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardTextList = new ArrayList<>();

        TextView textView = findViewById(R.id.title);
        textView.setText("Ingredients");

        DatabaseReference ref = database.getReference("/ingredients/").child(mauth.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cardTextList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: " + snapshot);
                    int id = snapshot.child("id").getValue(Integer.class);
                    String name = snapshot.child("name").getValue(String.class);
                    int quantity = snapshot.child("quantity").getValue(Integer.class);
                    String image = snapshot.child("image").getValue(String.class);

                    IngredientR ingredientR = new IngredientR(id, name, quantity, image);
                    cardTextList.add(ingredientR);

                }
                safeIngredientsAdapter.notifyDataSetChanged();
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "loadCardTextList:onCancelled", databaseError.toException());
                Toast.makeText(MainActivity.this, "Failed to load card text list.", Toast.LENGTH_SHORT).show();
            }
        });

        safeIngredientsAdapter = new SafeIngredientsAdapter(cardTextList);
        recyclerView.setAdapter(safeIngredientsAdapter);

        SearchView searchView = findViewById(R.id.search_view);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: " + query);
                searchIngredients(query);
                return false;
            }

            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: " + newText);
                
                return false;
            }
        });




        safeIngredientsAdapter.setOnEditClickListener(position -> {
            IngredientR ingredientR = cardTextList.get(position);
            int currentQuantity = ingredientR.getQuantity();
            showEditQuantityDialog(position, currentQuantity);
        });

        safeIngredientsAdapter.setOnTrashClickListener(position -> {
            deleteCard(position);
        });

    }
    private void searchIngredients(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spoonacular.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpoonacularApi api = retrofit.create(SpoonacularApi.class);
        Call<IngredientsResponse> call = api.searchIngredients(BuildConfig.API_TOKEN, query);
        Log.d(TAG, "searchIngredients: " + call.request().url());
        call.enqueue(new Callback<IngredientsResponse>() {
            @Override
            public void onResponse(Call<IngredientsResponse> call, Response<IngredientsResponse> response) {
                if (response.isSuccessful()) {
                    List<Ingredient> ingredients = response.body().getResults();
                    showSearchResultsDialog(ingredients);
                    Log.d(TAG, "onResponse: " + ingredients);
                }
            }

            @Override
            public void onFailure(Call<IngredientsResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSearchResultsDialog(List<Ingredient> ingredients) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_search_result, null);
        builder.setView(view);

        RecyclerView dialogRecyclerView = view.findViewById(R.id.dialog_recycler_view);
        TextView quantityTextView = view.findViewById(R.id.quantity_text_view);
        Button decreaseButton = view.findViewById(R.id.decrease_button);
        Button increaseButton = view.findViewById(R.id.increase_button);
        Button selectButton = view.findViewById(R.id.select_button);

        decreaseButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(quantityTextView.getText().toString());
            if (currentQuantity > 1) {
                currentQuantity--;
                quantityTextView.setText(String.valueOf(currentQuantity));
            }
        });

        increaseButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(quantityTextView.getText().toString());
            currentQuantity++;
            quantityTextView.setText(String.valueOf(currentQuantity));
        });

        SearchIngredientAdapter searchIngredientAdapter = new SearchIngredientAdapter(ingredients);
        dialogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dialogRecyclerView.setAdapter(searchIngredientAdapter);


        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        selectButton.setOnClickListener(v -> {
            int selectedPosition = searchIngredientAdapter.getSelectedPosition();
            if (selectedPosition != -1) {
                Ingredient selectedIngredient = ingredients.get(selectedPosition);
                int quantityValue = Integer.parseInt(quantityTextView.getText().toString());
                addCard(selectedIngredient, quantityValue);
                alertDialog.dismiss();
            } else {
                Toast.makeText(MainActivity.this, "Please select an ingredient", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void showEditQuantityDialog(int position, int currentQuantity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_quantity, null);
        builder.setView(view);

        Button decreaseButton = view.findViewById(R.id.decrease_button);
        Button increaseButton = view.findViewById(R.id.increase_button);
        Button saveButton = view.findViewById(R.id.save_button);
        TextView quantityTextView = view.findViewById(R.id.quantity_text_view);

        quantityTextView.setText(String.valueOf(currentQuantity));

        decreaseButton.setOnClickListener(v -> {
            int newQuantity = Integer.parseInt(quantityTextView.getText().toString());
            if (newQuantity > 1) {
                newQuantity--;
                quantityTextView.setText(String.valueOf(newQuantity));
            }
        });

        increaseButton.setOnClickListener(v -> {
            int newQuantity = Integer.parseInt(quantityTextView.getText().toString());
            newQuantity++;
            quantityTextView.setText(String.valueOf(newQuantity));
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        saveButton.setOnClickListener(v -> {
            int newQuantity = Integer.parseInt(quantityTextView.getText().toString());
            updateIngredientQuantity(position, newQuantity);
            alertDialog.dismiss();
        });
    }
    private void updateIngredientQuantity(int position, int newQuantity) {
        IngredientR ingredient = cardTextList.get(position);
        ingredient.setQuantity(newQuantity);
        safeIngredientsAdapter.notifyItemChanged(position);
        DatabaseReference ref = database.getReference("/ingredients").child(mauth.getUid()).child(String.valueOf(ingredient.getId()));
        ref.setValue(ingredient);
    }

    private void addCard(Ingredient selectedIngredient, double quantityValue) {
        IngredientR newIngredientR = new IngredientR(selectedIngredient, (int) quantityValue);
        cardTextList.add(newIngredientR);
        safeIngredientsAdapter.notifyItemInserted(cardTextList.size() - 1);
        DatabaseReference ref = database.getReference("/ingredients").child(mauth.getUid()).child(selectedIngredient.getId());
        ref.setValue(newIngredientR);
    }

    private void deleteCard(int position) {
        IngredientR ingredient = cardTextList.get(position);
        cardTextList.remove(position);
        safeIngredientsAdapter.notifyItemRemoved(position);
        DatabaseReference ref = database.getReference("/ingredients").child(mauth.getUid()).child(String.valueOf(ingredient.getId()));
        ref.removeValue();
    }
}
