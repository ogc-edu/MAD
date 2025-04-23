package com.example.quizfragments.ui.adapters;

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
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.dao.QuizDao;
import com.example.quizfragments.data.db.entities.Quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<Quiz> quizzes = new ArrayList<>();
    private final Context context;
    private final String[] COLORS = {"#4C6EF5", "#20C997", "#FD7E14", "#DC3545"}; // Blue, Green, Orange, Red
    private final OnQuizClickListener listener;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AppDatabase db;

    // Interface for quiz click events
    public interface OnQuizClickListener {
        void onQuizClick(Quiz quiz);
    }

    public QuizAdapter(Context context, OnQuizClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.db = DatabaseClient.getInstance(context).getAppDatabase();
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

        holder.titleTextView.setText(quiz.title);
        holder.detailsTextView.setText(quiz.questionCount + " questions  \n" + quiz.description);

        // Set initial values while we load from database
        holder.progressLabelTextView.setText("Loading progress...");
        holder.progressBar.setMax(quiz.questionCount);
        holder.progressBar.setProgress(0);

        // Load progress from database
        updateProgress(holder, quiz);

        // Set letter from the first character of title
        String letter = quiz.title.substring(0, 1).toUpperCase();
        holder.letterTextView.setText(letter);

        // Set color based on position
        String color = COLORS[position % COLORS.length];
        holder.colorIndicator.setBackgroundColor(Color.parseColor(color));
        holder.letterTextView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(color)));
        holder.progressLabelTextView.setTextColor(Color.parseColor(color));

        // Set click listener on the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuizClick(quiz);
            }
        });
    }

    private void updateProgress(QuizViewHolder holder, Quiz quiz) {
        executor.execute(() -> {
            // Get actual progress from database
            QuizDao quizDao = db.quizDao();
            int questionCount = quizDao.getQuizQuestionCount(quiz.quizId);
            int numAttempt = quizDao.getNumberOfAttemptedQuestion(quiz.quizId);

            // Update UI on main thread
            holder.itemView.post(() -> {
                holder.progressLabelTextView.setText("Progress: " + numAttempt + "/" + quiz.questionCount);
                holder.progressBar.setMax(quiz.questionCount);
                holder.progressBar.setProgress(numAttempt);

            });
        });
    }

    @Override
    public int getItemCount() {
        return quizzes.size();
    }

    public void setQuizzes(List<Quiz> quizzes) {
        this.quizzes = quizzes;
        notifyDataSetChanged();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        // Shutdown executor when adapter is no longer needed
        executor.shutdown();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        final TextView letterTextView;
        final TextView titleTextView;
        final TextView detailsTextView;
        final TextView progressLabelTextView;
        final ProgressBar progressBar;
        final View colorIndicator;

        QuizViewHolder(View itemView) {
            super(itemView);
            letterTextView = itemView.findViewById(R.id.text_letter);
            titleTextView = itemView.findViewById(R.id.text_quiz_title);
            detailsTextView = itemView.findViewById(R.id.text_quiz_details);
            progressLabelTextView = itemView.findViewById(R.id.text_progress_label);
            progressBar = itemView.findViewById(R.id.progress_bar);
            colorIndicator = itemView.findViewById(R.id.view_color_indicator);
        }
    }
}