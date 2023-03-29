package com.example.cumn;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CardItemAdapter cardItemAdapter;
    private List<String> cardTextList;
    private FirebaseDatabase database;
    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseAuth.getInstance().signOut();
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

        database = FirebaseDatabase.getInstance("https://cumn-cc19b-default-rtdb.europe-west1.firebasedatabase.app/");
        mauth = FirebaseAuth.getInstance();
    }

    private View.OnClickListener addButtonClickListener = v -> {
        addCard("New Card Item");
    };

    private void addCard(String text) {
        cardTextList.add(text);
        cardItemAdapter.notifyItemInserted(cardTextList.size() - 1);
        DatabaseReference ref = database.getReference("/users/" + mauth.getUid() + "/cards");
        ref.setValue(cardTextList);
    }

}
