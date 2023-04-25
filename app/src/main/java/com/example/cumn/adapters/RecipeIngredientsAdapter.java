package com.example.cumn.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cumn.R;
import com.example.cumn.models.IngredientRecipe;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeIngredientsAdapter extends RecyclerView.Adapter<RecipeIngredientsAdapter.IngredientViewHolder> {
    private List<IngredientRecipe> ingredients;
    private Context context;

    public RecipeIngredientsAdapter(List<IngredientRecipe> ingredients, Context context) {
        this.ingredients = ingredients;
        this.context = context;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_item, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        IngredientRecipe ingredient = ingredients.get(position);
        holder.name.setText(ingredient.getName());
        holder.original.setText(ingredient.getOriginal());
        Picasso.get().load(ingredient.getImage()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView original;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ingredient_image);
            name = itemView.findViewById(R.id.ingredient_name);
            original = itemView.findViewById(R.id.ingredient_original);
        }
    }
}
