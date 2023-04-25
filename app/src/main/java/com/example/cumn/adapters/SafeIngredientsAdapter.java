package com.example.cumn.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cumn.R;
import com.example.cumn.models.IngredientR;

import java.util.List;

public class SafeIngredientsAdapter extends RecyclerView.Adapter<SafeIngredientsAdapter.CardViewHolder> {

    private List<IngredientR> ingredients;
    private int selectedPosition = -1;

    public SafeIngredientsAdapter(List<IngredientR> ingredients) {
        this.ingredients = ingredients;
    }

    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new CardViewHolder(view);
    }

    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        IngredientR ingredient = ingredients.get(position);
        holder.title.setText(ingredient.getName());
        holder.quantity.setText(String.valueOf(ingredient.getQuantity()));
        holder.itemView.setBackgroundColor(selectedPosition == position ? Color.parseColor("#FFCDD2") : Color.parseColor("#FFFFFF"));
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public int getItemCount() {
        return ingredients.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView quantity;
        ImageView editIcon;
        ImageView trashIcon;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
            quantity = itemView.findViewById(R.id.card_quantity);
            editIcon = itemView.findViewById(R.id.edit_icon);
            trashIcon = itemView.findViewById(R.id.trash_icon);

            editIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    editListener.onEditClick(position);
                }
            });

            trashIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    trashListener.onTrashClick(position);
                }
            });
        }
    }

    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    public interface OnTrashClickListener {
        void onTrashClick(int position);
    }

    private OnTrashClickListener trashListener;
    private OnEditClickListener editListener;

    public void setOnEditClickListener(OnEditClickListener listener) {
        editListener = listener;
    }

    public void setOnTrashClickListener(OnTrashClickListener listener) {
        trashListener = listener;
    }

}

