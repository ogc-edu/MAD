package com.example.quizfragments.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.entities.FlashcardDeck;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FlashcardDeckAdapter extends RecyclerView.Adapter<FlashcardDeckAdapter.DeckViewHolder> {

    private final List<FlashcardDeck> deckList;
    private final OnDeckClickListener listener;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public interface OnDeckClickListener {
        void onDeckClick(FlashcardDeck deck);
    }

    public FlashcardDeckAdapter(List<FlashcardDeck> deckList, OnDeckClickListener listener) {
        this.deckList = deckList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard_deck, parent, false);
        return new DeckViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        FlashcardDeck deck = deckList.get(position);
        holder.tvDeckTitle.setText(deck.getTitle());

        // Get the card count for each deck from the database
        executor.execute(() -> {
            AppDatabase db = DatabaseClient.getInstance(holder.itemView.getContext()).getAppDatabase();

             int cardCount = db.flashcardDao().getFlashcardCountByDeckId(deck.getId());

            holder.itemView.post(() -> {
                holder.tvCardCount.setText(cardCount + " cards");
            });
        });

        holder.tvDeckDescription.setText(deck.getDescription());
        if (holder.tvDeckDescription.getText().toString().isEmpty()) {
            holder.tvDeckDescription.setText("Tap to study");
        }

        // Cycle through colors for the deck icons
        int[] colors = {
                R.color.deck_color_1,
                R.color.deck_color_2,
                R.color.deck_color_3,
                R.color.deck_color_4
        };

        int colorIndex = position % 4;
        try {
            int colorRes = colors[colorIndex];
            holder.ivDeckIcon.setColorFilter(holder.itemView.getContext().getResources().getColor(colorRes));
        } catch (Exception e) {
            int[] fallbackColors = {0xFF3498DB, 0xFF2ECC71, 0xFF9B59B6, 0xFFE74C3C};
            holder.ivDeckIcon.setColorFilter(fallbackColors[colorIndex]);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeckClick(deck);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deckList.size();
    }

    static class DeckViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDeckIcon;
        TextView tvDeckTitle;
        TextView tvDeckDescription;
        TextView tvCardCount;

        DeckViewHolder(View itemView) {
            super(itemView);
            ivDeckIcon = itemView.findViewById(R.id.iv_deck_icon);
            tvDeckTitle = itemView.findViewById(R.id.tv_deck_title);
            tvDeckDescription = itemView.findViewById(R.id.tv_deck_description);
            tvCardCount = itemView.findViewById(R.id.tv_card_count);
        }
    }

    // Helper method to update the deck list
    public void updateDeckList(List<FlashcardDeck> newDeckList) {
        this.deckList.clear();
        this.deckList.addAll(newDeckList);
        notifyDataSetChanged();
    }
}