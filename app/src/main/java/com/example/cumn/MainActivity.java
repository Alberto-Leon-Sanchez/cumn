package com.example.cumn;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cumn.api.SpoonacularApi;
import com.example.cumn.models.Ingredient;
import com.example.cumn.models.IngredientsResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CardItemAdapter cardItemAdapter;
    private List<Ingredient> cardTextList;
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

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationHandler.setupNavigation(this, bottomNav);

        FloatingActionButton addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(addButtonClickListener);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardTextList = new ArrayList<>();
        cardItemAdapter = new CardItemAdapter(cardTextList);
        recyclerView.setAdapter(cardItemAdapter);

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

        database = FirebaseDatabase.getInstance("https://cumn-cc19b-default-rtdb.europe-west1.firebasedatabase.app");
        mauth = FirebaseAuth.getInstance();
    }

    private View.OnClickListener addButtonClickListener = v -> {
        addCard("New Card Item");
    };

    private void addCard(String text) {
        //cardTextList.add(text);
        cardItemAdapter.notifyItemInserted(cardTextList.size() - 1);
        DatabaseReference ref = database.getReference("/users/" + mauth.getUid() + "/cards");
        ref.setValue(cardTextList);
    }

    private void addCar(String ingredient, String amount){
        cardItemAdapter.notifyItemInserted(cardTextList.size() - 1);
        DatabaseReference ref = database.getReference("/users/" + mauth.getUid() + "/cards");
        ref.setValue(cardTextList);
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
        EditText quantityEditText = view.findViewById(R.id.quantity_edit_text);
        Button selectButton = view.findViewById(R.id.select_button);

        CardItemAdapter dialogCardItemAdapter = new CardItemAdapter(ingredients);
        dialogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dialogRecyclerView.setAdapter(dialogCardItemAdapter);

        AlertDialog alertDialog = builder.create();

        selectButton.setOnClickListener(v -> {
            int selectedPosition = dialogCardItemAdapter.getSelectedPosition();
            if (selectedPosition != -1) {
                Ingredient selectedIngredient = ingredients.get(selectedPosition);
                String quantity = quantityEditText.getText().toString().trim();
                if (!quantity.isEmpty()) {
                    double quantityValue = Double.parseDouble(quantity);
                    addCard(selectedIngredient, quantityValue);
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a quantity", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Please select an ingredient", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }

    private void addCard(Ingredient selectedIngredient, double quantityValue) {
        cardTextList.add(selectedIngredient);
        cardItemAdapter.notifyItemInserted(cardTextList.size() - 1);
        DatabaseReference ref = database.getReference("/users/" + mauth.getUid() + "/cards");
        ref.setValue(cardTextList);
    }
}
