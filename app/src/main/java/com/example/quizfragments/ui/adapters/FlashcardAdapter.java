package com.example.quizfragments.ui.adapters;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.entities.Flashcard;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private final List<Flashcard> flashcards = new ArrayList<>();
    private final List<Boolean> isShowingQuestion = new ArrayList<>(); // Track question/answer state for each card
    private final Context context;

    public FlashcardAdapter(Context context) {
        this.context = context;
    }

    public void setFlashcards(List<Flashcard> flashcardList) {
        flashcards.clear();
        isShowingQuestion.clear();

        if (flashcardList != null) {
            flashcards.addAll(flashcardList);
            // Initialize all cards to show question side
            for (int i = 0; i < flashcardList.size(); i++) {
                isShowingQuestion.add(true); // true means showing question
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard, parent, false);
        return new FlashcardViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        final int adapterPosition = position;
        Flashcard flashcard = flashcards.get(adapterPosition);
        boolean showingQuestion = isShowingQuestion.get(adapterPosition);

        // Set up card with current data and state
        holder.bind(flashcard, showingQuestion);

        // Set click listeners for flipping
        holder.setOnCardClickListener(v -> {
            // First update the state
            boolean currentlyShowingQuestion = isShowingQuestion.get(adapterPosition);
            isShowingQuestion.set(adapterPosition, !currentlyShowingQuestion);

            // Then perform the animation based on the CURRENT state (before we changed it)
            if (currentlyShowingQuestion) {
                holder.flipToBack();
            } else {
                holder.flipToFront();
            }
        });
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardFrontView;
        private final MaterialCardView cardBackView;
        private final TextView tvQuestion;
        private final TextView tvAnswer;
        private final AnimatorSet showFrontAnimator;
        private final AnimatorSet showBackAnimator;

        public FlashcardViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            cardFrontView = itemView.findViewById(R.id.card_front);
            cardBackView = itemView.findViewById(R.id.card_back);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvAnswer = itemView.findViewById(R.id.tv_answer);

            // Set up animations
            showFrontAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_front);
            showBackAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_back);
        }

        public void setOnCardClickListener(View.OnClickListener listener) {
            cardFrontView.setOnClickListener(listener);
            cardBackView.setOnClickListener(listener);
        }

        public void bind(Flashcard flashcard, boolean isShowingQuestion) {
            tvQuestion.setText(flashcard.getQuestion());
            tvAnswer.setText(flashcard.getAnswer());

            // Set correct visibility based on the card's state without animation
            if (isShowingQuestion) {
                cardFrontView.setVisibility(View.VISIBLE);
                cardBackView.setVisibility(View.GONE);
            } else {
                cardFrontView.setVisibility(View.GONE);
                cardBackView.setVisibility(View.VISIBLE);
            }
        }

        public void flipToFront() {
            // Flip from back to front
            showFrontAnimator.setTarget(cardFrontView);
            showBackAnimator.setTarget(cardBackView);
            showFrontAnimator.start();
            showBackAnimator.start();
            cardFrontView.setVisibility(View.VISIBLE);
        }

        public void flipToBack() {
            // Flip from front to back
            showFrontAnimator.setTarget(cardBackView);
            showBackAnimator.setTarget(cardFrontView);
            showFrontAnimator.start();
            showBackAnimator.start();
            cardBackView.setVisibility(View.VISIBLE);
        }
    }
}