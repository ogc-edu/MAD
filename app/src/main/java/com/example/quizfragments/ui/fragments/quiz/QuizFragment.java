package com.example.quizfragments.ui.fragments.quiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Question;
import com.example.quizfragments.data.db.entities.Quiz;

import java.util.List;

public class QuizFragment extends Fragment {

    private AppDatabase db;
    private TextView showDB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.quiz_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

        EditText textToStore = view.findViewById(R.id.textinput);
        showDB = view.findViewById(R.id.dbInfo);

        Button storeDB = view.findViewById(R.id.storeBtn);
        Button checkQuiz = view.findViewById(R.id.checkQuiz);
        Button checkOption = view.findViewById(R.id.checkOption);
        Button checkQuestion = view.findViewById(R.id.checkQuestion);
        Button clearDB = view.findViewById(R.id.clearDB);

        storeDB.setOnClickListener(v -> storeQuiz(textToStore.getText().toString().trim()));
        checkQuiz.setOnClickListener(v -> displayAllQuiz());
        checkOption.setOnClickListener(v -> displayAllOptions());
        checkQuestion.setOnClickListener(v -> displayAllQuestions());
        clearDB.setOnClickListener(v -> clearDatabase());
    }

    private void storeQuiz(String title) {
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a title!", Toast.LENGTH_SHORT).show();
            return;
        }

        Quiz quiz = new Quiz();
        quiz.title = title;
        quiz.description = "new quiz sample";

        runInBackground(() -> {
            db.quizDao().insert(quiz);
            runOnMain(() -> {
                Toast.makeText(requireContext(), "Quiz stored successfully!", Toast.LENGTH_SHORT).show();
                showDB.setText("Quiz stored: " + quiz.title);
            });
        });
    }

    private void displayAllQuiz(){
        runInBackground(() ->{
            List<Quiz> quiz = db.quizDao().getAllQuizzes();
            StringBuilder builder = new StringBuilder();
            for (Quiz q : quiz) {
                builder.append("ID: ").append(q.quizId)
                        .append(", Text: ").append(q.title)
                        .append("\n");
            }
            updateDisplay(builder.toString());
        });
    }

    private void displayAllQuestions() {
        runInBackground(() -> {
            List<Question> questions = db.questionDao().getAllQuestions();
            StringBuilder builder = new StringBuilder();
            for (Question q : questions) {
                builder.append("ID: ").append(q.questionId)
                        .append(", Text: ").append(q.questionText)
                        .append("\n");
            }
            updateDisplay(builder.toString());
        });
    }

    private void displayAllOptions() {
        runInBackground(() -> {
            List<Option> options = db.optionDao().getAllOptions();
            StringBuilder builder = new StringBuilder();
            for (Option o : options) {
                builder.append("ID: ").append(o.optionId)
                        .append(", Text: ").append(o.optionText)
                        .append("\n");
            }
            updateDisplay(builder.toString());
        });
    }

    private void clearDatabase() {
        runInBackground(() -> {
            db.optionDao().deleteAllOptions();
            db.questionDao().deleteAllQuestions();
            db.quizDao().deleteAllQuizzes();

            runOnMain(() -> Toast.makeText(requireContext(), "Database cleared", Toast.LENGTH_SHORT).show());
        });
    }

    // Helper methods
    private void runInBackground(Runnable task) {
        new Thread(task).start();
    }

    private void runOnMain(Runnable task) {
        requireActivity().runOnUiThread(task);
    }

    private void updateDisplay(String text) {
        runOnMain(() -> {
            showDB.setText(text);
            showDB.setVisibility(View.VISIBLE);
        });
    }
}
