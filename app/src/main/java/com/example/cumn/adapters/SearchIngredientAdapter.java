package com.example.cumn.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cumn.R;
import com.example.cumn.models.Ingredient;

import java.util.List;

public class SearchIngredientAdapter extends RecyclerView.Adapter<SearchIngredientAdapter.IngredientViewHolder> {

    private List<Ingredient> ingredients;
    private int selectedPosition = -1;

    public SearchIngredientAdapter(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_api, parent, false);
        return new IngredientViewHolder(view);
    }

    public void onBindViewHolder(@NonNull IngredientViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.title.setText(ingredient.getName());
        holder.itemView.setOnClickListener(v -> {
            notifyItemChanged(selectedPosition);
            selectedPosition = position;
            notifyItemChanged(selectedPosition);
        });

        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.parseColor("#E0E0E0"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public int getItemCount() {
        return ingredients.size();
    }

    public class IngredientViewHolder extends RecyclerView.ViewHolder {

        TextView title;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
        }
    }
}
