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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeIngredientsAdapter extends RecyclerView.Adapter<RecipeIngredientsAdapter.IngredientViewHolder> {
    private List<IngredientRecipe> ingredients;
    private Context context;

    public RecipeIngredientsAdapter(List<IngredientRecipe> ingredients, Context context) {
        this.ingredients = ingredients;
        this.context = context;
    }

    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_item, parent, false);
        return new IngredientViewHolder(view);
    }

    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        IngredientRecipe ingredient = ingredients.get(position);
        holder.name.setText(ingredient.getName());
        holder.original.setText(ingredient.getOriginal());
        String imageUrl = ingredient.getImage();

        Picasso.get().load(imageUrl)
                .into(holder.image, new Callback() {
                    public void onSuccess() {
                    }
                    public void onError(Exception e) {
                        ViewGroup.LayoutParams layoutParams = holder.image.getLayoutParams();
                        layoutParams.height = 0;
                        holder.image.setLayoutParams(layoutParams);
                    }
                });
    }

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
