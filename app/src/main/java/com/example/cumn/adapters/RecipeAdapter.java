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
import com.example.cumn.models.Recipe;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Callback;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private OnCardClickListener mListener;
    private static List<Recipe> recipes;
    private Context context;

    public RecipeAdapter(List<Recipe> recipes, Context context, OnCardClickListener listener) {
        this.recipes = recipes;
        this.context = context;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_card_item, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.recipeTitle.setText(recipe.getTitle());
        Picasso.get().load(recipe.getImage()).into(holder.recipeImage);

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCardClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView recipeTitle;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipe_image);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
        }

    }

    public interface OnCardClickListener {
        void onCardClick(int position);
    }


}
