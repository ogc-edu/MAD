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
import com.example.quizfragments.data.db.*;
import com.example.quizfragments.data.db.entities.Question;
import com.example.quizfragments.data.db.entities.Quiz;

import java.util.List;

public class QuizFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.quiz_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){

        Button storeDB = view.findViewById(R.id.storeBtn);
        EditText textToStore = view.findViewById(R.id.textinput);
        Button getDB = view.findViewById(R.id.dbBtn);
        TextView showDB = view.findViewById(R.id.dbInfo);
        AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

        storeDB.setOnClickListener(v ->{
            String title = textToStore.getText().toString().trim();
            if(!title.isEmpty()){
                Quiz quiz = new Quiz();
                quiz.title = title;
                quiz.description = "new quiz sample";

                new Thread(() -> {
                    db.quizDao().insert(quiz);
                    // Once insertion is complete, switch to the main thread to update UI
                    getActivity().runOnUiThread(() -> {
                        // Show success message as a Toast
                        Toast.makeText(requireContext(), "Data stored successfully!", Toast.LENGTH_SHORT).show();

                        // Optionally, you can update a TextView to show success in the UI
                        showDB.setText("Quiz stored: " + quiz.title);
                    });
                }).start();
            }else{
                textToStore.setError("Enter a title!");
            }
        });
        getDB.setOnClickListener(v -> {
            new Thread(() -> {
                // Query the database for all questions
                List<Question> questions = db.questionDao().getAllQuestions();

                // Update UI on the main thread
                requireActivity().runOnUiThread(() -> {
                    // Format the list of questions (this is just a simple example)
                    StringBuilder questionsText = new StringBuilder();
                    for (Question question : questions) {
                        questionsText.append("ID: ").append(question.questionId)
                                .append(", Text: ").append(question.questionText)
                                .append("\n");
                    }

                    // Set the formatted text to the UI element
                    showDB.setText(questionsText.toString());
                    showDB.setVisibility(View.VISIBLE);
                });
            }).start();
        });

    }
}
