package com.example.quizfragments.ui.adapters;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.entities.Flashcard;

import java.util.List;

public class FlashcardPagerAdapter extends RecyclerView.Adapter<FlashcardPagerAdapter.FlashcardViewHolder> {

    private final List<Flashcard> flashcards;
    private final AnimatorSet showFrontAnimator;
    private final AnimatorSet showBackAnimator;


    public FlashcardPagerAdapter(List<Flashcard> flashcards, AnimatorSet showFrontAnimator, AnimatorSet showBackAnimator) {
        this.flashcards = flashcards;
        this.showFrontAnimator = showFrontAnimator;
        this.showBackAnimator = showBackAnimator;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);
        holder.tvQuestion.setText(flashcard.getQuestion());
        holder.tvAnswer.setText(flashcard.getAnswer());

        // Reset to showing question
        holder.isShowingQuestion = true;
        holder.cardFrontView.setVisibility(View.VISIBLE);
        holder.cardBackView.setVisibility(View.GONE);

        // Handle card click to flip
        holder.itemView.setOnClickListener(v -> {
            if (holder.isShowingQuestion) {
                holder.flipToBack();
            } else {
                holder.flipToFront(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    class FlashcardViewHolder extends RecyclerView.ViewHolder {
        View cardFrontView;
        View cardBackView;
        TextView tvQuestion;
        TextView tvAnswer;
        boolean isShowingQuestion = true;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardFrontView = itemView.findViewById(R.id.card_front);
            cardBackView = itemView.findViewById(R.id.card_back);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvAnswer = itemView.findViewById(R.id.tv_answer);
        }

        void flipToFront(boolean animate) {
            isShowingQuestion = true;
            if (animate) {
                AnimatorSet frontAnimator = new AnimatorSet();
                AnimatorSet backAnimator = new AnimatorSet();

                frontAnimator.setTarget(cardFrontView);
                backAnimator.setTarget(cardBackView);

                frontAnimator.setDuration(300);
                backAnimator.setDuration(300);

                frontAnimator.play((AnimatorInflater.loadAnimator(itemView.getContext(), R.animator.card_flip_front)));
                backAnimator.play((AnimatorInflater.loadAnimator(itemView.getContext(), R.animator.card_flip_back)));

                frontAnimator.start();
                backAnimator.start();
            } else {
                cardFrontView.setVisibility(View.VISIBLE);
                cardBackView.setVisibility(View.GONE);
            }
        }

        void flipToBack() {
            isShowingQuestion = false;
            AnimatorSet frontAnimator = new AnimatorSet();
            AnimatorSet backAnimator = new AnimatorSet();

            frontAnimator.setTarget(cardBackView);
            backAnimator.setTarget(cardFrontView);

            frontAnimator.setDuration(300);
            backAnimator.setDuration(300);

            frontAnimator.play((AnimatorInflater.loadAnimator(itemView.getContext(), R.animator.card_flip_front)));
            backAnimator.play((AnimatorInflater.loadAnimator(itemView.getContext(), R.animator.card_flip_back)));

            frontAnimator.start();
            backAnimator.start();
        }
    }
}
