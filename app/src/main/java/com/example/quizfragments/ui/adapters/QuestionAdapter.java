// QuestionAdapter.java
package com.example.quizfragments.ui.adapters;
import com.example.quizfragments.ui.fragments.quiz.QuestionWithStatus;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quizfragments.R;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private List<QuestionWithStatus> questions;
    private Context context;
    private OnQuestionClickListener listener;

    public interface OnQuestionClickListener {
        void onQuestionClick(QuestionWithStatus question, int position);
    }

    public QuestionAdapter(Context context, List<QuestionWithStatus> questions, OnQuestionClickListener listener) {
        this.context = context;
        this.questions = questions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        QuestionWithStatus question = questions.get(position);
        holder.bind(question, position);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public void updateQuestions(List<QuestionWithStatus> newQuestions) {
        this.questions = newQuestions;
        notifyDataSetChanged();
    }

    class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionNumber;
        TextView tvQuestionText;
        TextView tvAnswerStatus;

        QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionNumber = itemView.findViewById(R.id.tvQuestionNumber);
            tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
            tvAnswerStatus = itemView.findViewById(R.id.tvAnswerStatus);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onQuestionClick(questions.get(position), position);
                }
            });
        }

        void bind(QuestionWithStatus question, int position) {
            // Set question number
            tvQuestionNumber.setText(String.valueOf(position + 1));

            // Set question text
            tvQuestionText.setText(question.getQuestionText());

            // Set circle background color and status text based on attempted status
            int circleColor;
            int textColor;
            String statusText;

            switch (question.getAttempted()) {
                case 1: // Correct
                    circleColor = R.color.colorCorrect;
                    textColor = R.color.colorCorrect;
                    statusText = "Correct: " + question.getCorrectAnswer();
                    break;
                case 2: // Incorrect
                    circleColor = R.color.colorIncorrect;
                    textColor = R.color.colorIncorrect;
                    statusText = "Incorrect: Answered \"" + question.getUserAnswer() + "\"";
                    break;
                default: // Not attempted
                    circleColor = R.color.colorNotAttempted;
                    textColor = R.color.colorNotAttempted;
                    statusText = "Not attempted yet";
                    break;
            }

            tvQuestionNumber.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, circleColor)));
            tvAnswerStatus.setTextColor(ContextCompat.getColor(context, textColor));
            tvAnswerStatus.setText(statusText);
        }
    }
}