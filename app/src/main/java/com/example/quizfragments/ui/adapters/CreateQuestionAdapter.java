package com.example.quizfragments.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.ui.fragments.quiz.CreateQuizFragment.QuestionWithOptions;

import java.util.List;

public class CreateQuestionAdapter extends RecyclerView.Adapter<CreateQuestionAdapter.QuestionViewHolder> {

    private final Context context;
    private final List<QuestionWithOptions> questions;
    private final QuestionActionListener listener;

    public interface QuestionActionListener {
        void onEditQuestion(int position);
        void onDeleteQuestion(int position);
    }

    public CreateQuestionAdapter(Context context, List<QuestionWithOptions> questions, QuestionActionListener listener) {
        this.context = context;
        this.questions = questions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_create_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        QuestionWithOptions question = questions.get(position);

        holder.textQuestionNumber.setText("Question " + question.getQuestionNumber());
        holder.textQuestion.setText(question.getQuestionText());
        holder.textCorrectAnswer.setText("Correct answer: " + question.getCorrectAnswerText());

        // Set click listeners
        holder.btnEditQuestion.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditQuestion(position);
            }
        });

        holder.btnDeleteQuestion.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteQuestion(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        final TextView textQuestionNumber;
        final TextView textQuestion;
        final TextView textCorrectAnswer;
        final ImageButton btnEditQuestion;
        final ImageButton btnDeleteQuestion;

        QuestionViewHolder(View itemView) {
            super(itemView);
            textQuestionNumber = itemView.findViewById(R.id.text_question_number);
            textQuestion = itemView.findViewById(R.id.text_question);
            textCorrectAnswer = itemView.findViewById(R.id.text_correct_answer);
            btnEditQuestion = itemView.findViewById(R.id.btn_edit_question);
            btnDeleteQuestion = itemView.findViewById(R.id.btn_delete_question);
        }
    }
}