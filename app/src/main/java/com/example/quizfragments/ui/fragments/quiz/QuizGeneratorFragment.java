package com.example.quizfragments.ui.fragments.quiz;

import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.quizfragments.R;

public class QuizGeneratorFragment extends Fragment {
    private EditText topicEditText;
    private EditText numQuestionsEditText;
    private TextView difficultyTextView;
    private Button generateButton;
    private ProgressBar progressBar;
    private String selectedDifficulty = "Medium"; // Default difficulty

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_generator, container, false);

        // Initialize views
        topicEditText = view.findViewById(R.id.topic_edit_text);
        numQuestionsEditText = view.findViewById(R.id.num_questions_edit_text);
        difficultyTextView = view.findViewById(R.id.difficulty_text_view);
        generateButton = view.findViewById(R.id.generate_button);
        progressBar = view.findViewById(R.id.progress_bar);

        // Set up the difficulty selection with context menu
        difficultyTextView.setText(selectedDifficulty);
        registerForContextMenu(difficultyTextView);
        difficultyTextView.setOnClickListener(v -> {
            requireActivity().openContextMenu(difficultyTextView);
        });

        // Set up the generate button
        generateButton.setOnClickListener(v -> validateAndGenerateQuiz());

        // Set up the back button
        ImageButton backButton = view.findViewById(R.id.back_btn);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.difficulty_text_view) {
            menu.setHeaderTitle("Select Difficulty");
            menu.add(Menu.NONE, 1, Menu.NONE, "Easy");
            menu.add(Menu.NONE, 2, Menu.NONE, "Medium");
            menu.add(Menu.NONE, 3, Menu.NONE, "Hard");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                selectedDifficulty = "Easy";
                difficultyTextView.setText(selectedDifficulty);
                return true;
            case 2:
                selectedDifficulty = "Medium";
                difficultyTextView.setText(selectedDifficulty);
                return true;
            case 3:
                selectedDifficulty = "Hard";
                difficultyTextView.setText(selectedDifficulty);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void validateAndGenerateQuiz() {
        String topic = topicEditText.getText().toString().trim();
        String numQuestionsStr = numQuestionsEditText.getText().toString().trim();

        // Validate topic
        if (topic.isEmpty()) {
            topicEditText.setError("Please enter a quiz topic");
            return;
        }

        // Validate number of questions
        int numQuestions;
        try {
            numQuestions = Integer.parseInt(numQuestionsStr);
            if (numQuestions < 5 || numQuestions > 20) {
                numQuestionsEditText.setError("Number of questions must be between 5 and 20");
                return;
            }
        } catch (NumberFormatException e) {
            numQuestionsEditText.setError("Please enter a valid number");
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        generateButton.setEnabled(false);

        // Generate quiz (placeholder function for your AI implementation)
        generateQuizWithAI(topic, numQuestions, selectedDifficulty);
    }

    private void generateQuizWithAI(String topic, int numQuestions, String difficulty) {
        // This is a placeholder for your AI implementation
        // You would replace this with your actual AI code

        // For demo, simulate a delay
        new Handler().postDelayed(() -> {
            // Hide progress
            progressBar.setVisibility(View.GONE);
            generateButton.setEnabled(true);

            // Show success message
            Toast.makeText(requireContext(), "Quiz generated for topic: " + topic, Toast.LENGTH_SHORT).show();

            // Here you would navigate to the quiz display fragment
            // navigateToQuizDisplay(generatedQuiz);
        }, 2000);
    }
}