package com.example.cumn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardItemAdapter extends RecyclerView.Adapter<CardItemAdapter.ViewHolder> {

    private List<String> cardTextList;

    public CardItemAdapter(List<String> cardTextList) {
        this.cardTextList = cardTextList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.cardText.setText(cardTextList.get(position));
    }

    @Override
    public int getItemCount() {
        return cardTextList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView cardText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardText = itemView.findViewById(R.id.card_text);
        }
    }
}
