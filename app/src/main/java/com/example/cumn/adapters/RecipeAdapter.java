package com.example.cumn.adapters;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cumn.BuildConfig;
import com.example.cumn.R;
import com.example.cumn.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private OnCardClickListener mListener;
    private List<Recipe> recipes;
    private final Context context;
    private FirebaseDatabase database;
    private FirebaseAuth mauth;


    private List<Recipe> allRecipes;

    public RecipeAdapter(List<Recipe> recipes, Context context, OnCardClickListener listener, List<Recipe> allRecipes) {
        this.recipes = recipes;
        this.context = context;
        this.mListener = listener;
        this.allRecipes = allRecipes;

        this.database = FirebaseDatabase.getInstance(BuildConfig.DB_URL);
        this.mauth = FirebaseAuth.getInstance();
    }

    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_card_item, parent, false);
        return new RecipeViewHolder(view);
    }

    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.recipeTitle.setText(recipe.getTitle());
        String imageUrl = recipe.getImage();

        Picasso.get().load(imageUrl)
                .into(holder.recipeImage, new Callback() {
                    public void onSuccess() {
                        Log.d("RecipeAdapter", "Image loaded successfully");
                    }

                    public void onError(Exception e) {
                        ViewGroup.LayoutParams layoutParams = holder.recipeImage.getLayoutParams();
                        layoutParams.height = 0;
                        holder.recipeImage.setLayoutParams(layoutParams);
                        Log.e("RecipeAdapter", "Error loading image: " + e.getMessage());
                    }
                });

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCardClick(position);
            }
        });

        String userId = mauth.getCurrentUser().getUid();
        DatabaseReference userRecipesRef = database.getReference("/users_recipes").child(userId);

        userRecipesRef.child(String.valueOf(recipe.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.buttonIcon.setSelected(dataSnapshot.exists());
            }
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("RecipeAdapter", "Error checking recipe in database: " + databaseError.getMessage());
            }
        });

        holder.buttonIcon.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            if (v.isSelected()) {
                userRecipesRef.child(String.valueOf(recipe.getId())).setValue(recipe)
                        .addOnSuccessListener(aVoid -> Log.d("RecipeAdapter", "Recipe saved successfully"))
                        .addOnFailureListener(e -> Log.e("RecipeAdapter", "Failed to save recipe", e));
            } else {
                userRecipesRef.child(String.valueOf(recipe.getId())).removeValue()
                        .addOnSuccessListener(aVoid -> Log.d("RecipeAdapter", "Recipe removed successfully"))
                        .addOnFailureListener(e -> Log.e("RecipeAdapter", "Failed to remove recipe", e));
            }
        });
    }


    public int getItemCount() {
        return recipes.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageButton buttonIcon;
        ImageView recipeImage;
        TextView recipeTitle;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipe_image);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
            buttonIcon = itemView.findViewById(R.id.button_icon);
        }

    }

    public void filter(String text) {
        List<Recipe> filteredList = new ArrayList<>();
        if (text.isEmpty()) {
            filteredList.addAll(allRecipes);
        } else {
            text = text.toLowerCase();
            for (Recipe recipe : allRecipes) {
                if (recipe.getTitle().toLowerCase().contains(text)) {
                    filteredList.add(recipe);
                }
            }
        }
        recipes.clear();
        recipes.addAll(filteredList);
        notifyDataSetChanged();
    }



    public interface OnCardClickListener {
        void onCardClick(int position);
    }
}
