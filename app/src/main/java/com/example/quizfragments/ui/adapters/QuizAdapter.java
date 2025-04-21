package com.example.quizfragments.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.entities.Quiz;

import java.util.ArrayList;
import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {
    private List<Quiz> quizzes = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.quiz_item, parent, false);
        return new QuizViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz currentQuiz = quizzes.get(position);
        holder.textViewTitle.setText(currentQuiz.getTitle());
        // Add null check to avoid NullPointerException
        int questionCount = currentQuiz.getQuestionCount();
        holder.textViewQuestionCount.setText(questionCount + " questions");

        // Set icon based on quiz category or use a default icon
        holder.imageViewIcon.setImageResource(R.drawable.ic_quiz);
    }

    @Override
    public int getItemCount() {
        return quizzes.size();
    }

    public void setQuizzes(List<Quiz> quizzes) {
        this.quizzes = quizzes;
        notifyDataSetChanged();
    }

    public Quiz getQuizAt(int position) {
        return quizzes.get(position);
    }

    class QuizViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewQuestionCount;
        private ImageView imageViewIcon;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewQuestionCount = itemView.findViewById(R.id.text_view_question_count);
            imageViewIcon = itemView.findViewById(R.id.image_view_icon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(quizzes.get(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Quiz quiz);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}