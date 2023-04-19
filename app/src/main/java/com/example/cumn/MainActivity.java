package com.example.cumn;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cumn.api.SpoonacularApi;
import com.example.cumn.models.Ingredient;
import com.example.cumn.models.IngredientR;
import com.example.cumn.models.IngredientsResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private CardIngredientAdapter cardIngredientAdapter;
    private List<IngredientR> cardTextList;
    private FirebaseDatabase database;
    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, AuthenticationActivity.class));
            finish();
        }

        database = FirebaseDatabase.getInstance("https://cumn-cc19b-default-rtdb.europe-west1.firebasedatabase.app");
        mauth = FirebaseAuth.getInstance();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationHandler.setupNavigation(this, bottomNav);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardTextList = new ArrayList<>();

        DatabaseReference ref = database.getReference("/users/" + mauth.getUid() + "/cards");
        ref.addValueEventListener(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cardTextList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    IngredientR ingredientR = snapshot.getValue(IngredientR.class);
                    cardTextList.add(ingredientR);
                }
                cardIngredientAdapter.notifyDataSetChanged();
            }
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "loadCardTextList:onCancelled", databaseError.toException());
                Toast.makeText(MainActivity.this, "Failed to load card text list.", Toast.LENGTH_SHORT).show();
            }
        });

        cardIngredientAdapter = new CardIngredientAdapter(cardTextList);
        recyclerView.setAdapter(cardIngredientAdapter);

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: " + query);
                searchIngredients(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: " + newText);
                return false;
            }
        });

        cardIngredientAdapter.setOnEditClickListener(position -> {
            IngredientR ingredientR = cardTextList.get(position);
            int currentQuantity = ingredientR.getQuantity();
            showEditQuantityDialog(position, currentQuantity);
        });

        cardIngredientAdapter.setOnTrashClickListener(position -> {
            deleteCard(position);
        });

    }
    private void searchIngredients(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spoonacular.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpoonacularApi api = retrofit.create(SpoonacularApi.class);
        Call<IngredientsResponse> call = api.searchIngredients("05b03e6e38be4044854648b70d8b126e", query);
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

        IngredientAdapter ingredientAdapter = new IngredientAdapter(ingredients);
        dialogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dialogRecyclerView.setAdapter(ingredientAdapter);


        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        selectButton.setOnClickListener(v -> {
            int selectedPosition = ingredientAdapter.getSelectedPosition();
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
        cardIngredientAdapter.notifyItemChanged(position);
        DatabaseReference ref = database.getReference("/users/" + mauth.getUid() + "/cards");
        ref.setValue(cardTextList);
    }

    private void addCard(Ingredient selectedIngredient, double quantityValue) {
        cardTextList.add(new IngredientR(selectedIngredient, (int) quantityValue));
        cardIngredientAdapter.notifyItemInserted(cardTextList.size() - 1);
        DatabaseReference ref = database.getReference("/users/" + mauth.getUid() + "/cards");
        ref.setValue(cardTextList);
    }

    private void deleteCard(int position) {
        cardTextList.remove(position);
        cardIngredientAdapter.notifyItemRemoved(position);
        DatabaseReference ref = database.getReference("/users/" + mauth.getUid() + "/cards");
        ref.setValue(cardTextList);
    }
}
