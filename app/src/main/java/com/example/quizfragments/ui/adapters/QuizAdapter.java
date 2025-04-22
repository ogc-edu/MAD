package com.example.quizfragments.ui.fragments.quiz;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.entities.Quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<Quiz> quizzes = new ArrayList<>();
    private final Context context;
    private final String[] COLORS = {"#4C6EF5", "#20C997", "#FD7E14", "#DC3545"}; // Blue, Green, Orange, Red

    public QuizAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = quizzes.get(position);

        // Generate random progress for demonstration (replace with actual progress tracking)
        Random random = new Random();
        int progress = random.nextInt(quiz.questionCount + 1);

        holder.titleTextView.setText(quiz.title);
        holder.detailsTextView.setText(quiz.questionCount + " questions • " + quiz.description);
        holder.progressLabelTextView.setText("Progress: " + progress + "/" + quiz.questionCount);
        holder.progressBar.setMax(quiz.questionCount);
        holder.progressBar.setProgress(progress);

        // Set letter from the first character of title
        String letter = quiz.title.substring(0, 1).toUpperCase();
        holder.letterTextView.setText(letter);

        // Set color based on position
        String color = COLORS[position % COLORS.length];
        holder.colorIndicator.setBackgroundColor(Color.parseColor(color));
        holder.letterTextView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(color)));
        holder.progressLabelTextView.setTextColor(Color.parseColor(color));

        // Set checkbox based on progress
        holder.completedCheckBox.setChecked(progress == quiz.questionCount);
    }

    @Override
    public int getItemCount() {
        return quizzes.size();
    }

    public void setQuizzes(List<Quiz> quizzes) {
        this.quizzes = quizzes;
        notifyDataSetChanged();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        final TextView letterTextView;
        final TextView titleTextView;
        final TextView detailsTextView;
        final TextView progressLabelTextView;
        final ProgressBar progressBar;
        final CheckBox completedCheckBox;
        final View colorIndicator;

        QuizViewHolder(View itemView) {
            super(itemView);
            letterTextView = itemView.findViewById(R.id.text_letter);
            titleTextView = itemView.findViewById(R.id.text_quiz_title);
            detailsTextView = itemView.findViewById(R.id.text_quiz_details);
            progressLabelTextView = itemView.findViewById(R.id.text_progress_label);
            progressBar = itemView.findViewById(R.id.progress_bar);
            completedCheckBox = itemView.findViewById(R.id.checkbox_completed);
            colorIndicator = itemView.findViewById(R.id.view_color_indicator);
        }
    }
}
