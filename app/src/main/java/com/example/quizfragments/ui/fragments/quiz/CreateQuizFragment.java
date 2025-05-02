package com.example.quizfragments.ui.fragments.quiz;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Option;
import com.example.quizfragments.data.db.entities.Question;
import com.example.quizfragments.data.db.entities.Quiz;
import com.example.quizfragments.ui.adapters.CreateQuestionAdapter; // Fixed import
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateQuizFragment extends Fragment {

    private EditText editQuizTitle;
    private EditText editQuizDescription;
    private RecyclerView recyclerQuestions;
    private Button btnAddQuestion;
    private Button btnSaveQuiz;
    private ImageButton btnBack;

    private CreateQuestionAdapter questionAdapter;
    private List<QuestionWithOptions> questionsList = new ArrayList<>();
    private ExecutorService executor;
    private AppDatabase db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize database
        db = DatabaseClient.getInstance(requireContext()).getAppDatabase();
        executor = Executors.newSingleThreadExecutor();

        // Initialize views
        editQuizTitle = view.findViewById(R.id.edit_quiz_title);
        editQuizDescription = view.findViewById(R.id.edit_quiz_description);
        recyclerQuestions = view.findViewById(R.id.recycler_questions);
        btnAddQuestion = view.findViewById(R.id.btn_add_question);
        btnSaveQuiz = view.findViewById(R.id.btn_save_quiz);
        btnBack = view.findViewById(R.id.btn_back);

        // Set up RecyclerView
        questionAdapter = new CreateQuestionAdapter(requireContext(), questionsList,
                new CreateQuestionAdapter.QuestionActionListener() {
                    @Override
                    public void onEditQuestion(int position) {
                        showAddQuestionDialog(position);
                    }

                    @Override
                    public void onDeleteQuestion(int position) {
                        questionsList.remove(position);
                        questionAdapter.notifyItemRemoved(position);
                        // Update question numbers for all items
                        for (int i = 0; i < questionsList.size(); i++) {
                            questionsList.get(i).setQuestionNumber(i + 1);
                        }
                        questionAdapter.notifyDataSetChanged();
                        updateRecyclerViewHeight();
                    }
                });

        recyclerQuestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerQuestions.setAdapter(questionAdapter);
        recyclerQuestions.post(() -> updateRecyclerViewHeight());

        // Set click listeners
        btnAddQuestion.setOnClickListener(v -> showAddQuestionDialog(-1));

        btnSaveQuiz.setOnClickListener(v -> saveQuiz());

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void showAddQuestionDialog(int editPosition) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_question, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        TextInputEditText editQuestionText = dialogView.findViewById(R.id.edit_question_text);
        TextInputEditText editOption1 = dialogView.findViewById(R.id.edit_option_1);
        TextInputEditText editOption2 = dialogView.findViewById(R.id.edit_option_2);
        TextInputEditText editOption3 = dialogView.findViewById(R.id.edit_option_3);
        TextInputEditText editOption4 = dialogView.findViewById(R.id.edit_option_4);

        RadioButton radioOption1 = dialogView.findViewById(R.id.radio_option_1);
        RadioButton radioOption2 = dialogView.findViewById(R.id.radio_option_2);
        RadioButton radioOption3 = dialogView.findViewById(R.id.radio_option_3);
        RadioButton radioOption4 = dialogView.findViewById(R.id.radio_option_4);

        // Get the RadioGroup but we won't rely solely on it
        RadioGroup radioGroupOptions = dialogView.findViewById(R.id.radio_group_options);

        // IMPORTANT: Set individual click listeners for each radio button
        radioOption1.setOnClickListener(v -> {
            radioOption1.setChecked(true);
            radioOption2.setChecked(false);
            radioOption3.setChecked(false);
            radioOption4.setChecked(false);
        });

        radioOption2.setOnClickListener(v -> {
            radioOption1.setChecked(false);
            radioOption2.setChecked(true);
            radioOption3.setChecked(false);
            radioOption4.setChecked(false);
        });

        radioOption3.setOnClickListener(v -> {
            radioOption1.setChecked(false);
            radioOption2.setChecked(false);
            radioOption3.setChecked(true);
            radioOption4.setChecked(false);
        });

        radioOption4.setOnClickListener(v -> {
            radioOption1.setChecked(false);
            radioOption2.setChecked(false);
            radioOption3.setChecked(false);
            radioOption4.setChecked(true);
        });

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);

        // Set dialog title and button text based on whether we're editing or adding
        if (editPosition != -1) {
            // We're editing an existing question
            QuestionWithOptions question = questionsList.get(editPosition);
            editQuestionText.setText(question.getQuestionText());

            List<OptionItem> options = question.getOptions();
            // Ensure all radio buttons are unchecked first
            radioOption1.setChecked(false);
            radioOption2.setChecked(false);
            radioOption3.setChecked(false);
            radioOption4.setChecked(false);

            if (options.size() >= 1) {
                editOption1.setText(options.get(0).getText());
                if (options.get(0).isCorrect()) radioOption1.setChecked(true);
            }
            if (options.size() >= 2) {
                editOption2.setText(options.get(1).getText());
                if (options.get(1).isCorrect()) radioOption2.setChecked(true);
            }
            if (options.size() >= 3) {
                editOption3.setText(options.get(2).getText());
                if (options.get(2).isCorrect()) radioOption3.setChecked(true);
            }
            if (options.size() >= 4) {
                editOption4.setText(options.get(3).getText());
                if (options.get(3).isCorrect()) radioOption4.setChecked(true);
            }

            btnAdd.setText("Update");
        } else {
            // Default select the first option for new questions
            radioOption1.setChecked(true);
            radioOption2.setChecked(false);
            radioOption3.setChecked(false);
            radioOption4.setChecked(false);
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            if (editQuestionText == null || editOption1 == null || editOption2 == null || editOption3 == null || editOption4 == null) {
                Toast.makeText(getContext(), "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Validate inputs
            String questionText = editQuestionText.getText().toString().trim();
            String option1Text = editOption1.getText().toString().trim();
            String option2Text = editOption2.getText().toString().trim();
            String option3Text = editOption3.getText().toString().trim();
            String option4Text = editOption4.getText().toString().trim();

            if (questionText.isEmpty() || option1Text.isEmpty() || option2Text.isEmpty() ||
                    option3Text.isEmpty() || option4Text.isEmpty()) {
                Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if any radio button is selected
            if (!radioOption1.isChecked() && !radioOption2.isChecked() &&
                    !radioOption3.isChecked() && !radioOption4.isChecked()) {
                Toast.makeText(requireContext(), "Please select the correct answer", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create options list
            List<OptionItem> options = new ArrayList<>();
            options.add(new OptionItem(option1Text, radioOption1.isChecked()));
            options.add(new OptionItem(option2Text, radioOption2.isChecked()));
            options.add(new OptionItem(option3Text, radioOption3.isChecked()));
            options.add(new OptionItem(option4Text, radioOption4.isChecked()));

            // Create QuestionWithOptions object
            QuestionWithOptions questionItem;
            if (editPosition != -1) {
                // Update existing question
                questionItem = questionsList.get(editPosition);
                questionItem.setQuestionText(questionText);
                questionItem.setOptions(options);
                questionAdapter.notifyItemChanged(editPosition);
                updateRecyclerViewHeight();
            } else {
                // Add new question
                questionItem = new QuestionWithOptions(
                        questionsList.size() + 1,
                        questionText,
                        options
                );
                questionsList.add(questionItem);
                questionAdapter.notifyItemInserted(questionsList.size() - 1);
                updateRecyclerViewHeight();
            }

            dialog.dismiss();
        });
    }

    private void saveQuiz() {
        String title = editQuizTitle.getText().toString().trim();
        String description = editQuizDescription.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter title and description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (questionsList.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one question", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save quiz to database
        executor.execute(() -> {
            // Create Quiz entity
            Quiz quiz = new Quiz();
            quiz.title = title;
            quiz.description = description;
            quiz.questionCount = questionsList.size();

            // Insert quiz and get its ID
            long quizId = db.quizDao().insert(quiz);

            // Insert all questions and options
            for (int i = 0; i < questionsList.size(); i++) {
                QuestionWithOptions questionWithOptions = questionsList.get(i);

                // Create Question entity
                Question question = new Question();
                question.quizId = (int) quizId;
                question.questionNumber = i + 1;
                question.questionText = questionWithOptions.getQuestionText();
                question.attempted = 0;
                question.userAnswer = -1;

                // Insert question and get its ID
                long questionId = db.questionDao().insert(question);

                // Insert options for this question
                List<OptionItem> optionItems = questionWithOptions.getOptions();
                for (int j = 0; j < optionItems.size(); j++) {
                    OptionItem optionItem = optionItems.get(j);

                    Option option = new Option();
                    option.questionId = (int) questionId;
                    option.optionText = optionItem.getText();
                    option.isCorrect = optionItem.isCorrect();

                    db.optionDao().insert(option);
                }
            }

            // Return to quiz list on main thread
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Quiz saved successfully", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        });
    }

    private void updateRecyclerViewHeight() {
        recyclerQuestions.post(() -> {
            // Force a layout pass to ensure all children are measured
            recyclerQuestions.requestLayout();

            // Get adapter
            CreateQuestionAdapter adapter = (CreateQuestionAdapter) recyclerQuestions.getAdapter();
            if (adapter == null || adapter.getItemCount() == 0) {
                // Set a minimum height if there are no items
                ViewGroup.LayoutParams params = recyclerQuestions.getLayoutParams();
                params.height = 100; // Minimum height in dp
                recyclerQuestions.setLayoutParams(params);
                return;
            }

            // For a more reliable approach, calculate height based on item count and average item height
            RecyclerView.LayoutManager layoutManager = recyclerQuestions.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                int itemCount = adapter.getItemCount();
                int estimatedItemHeight = 200; // Default estimated height for each item in dp

                // Get actual height of first visible item if possible
                View firstChild = recyclerQuestions.getChildAt(0);
                if (firstChild != null) {
                    estimatedItemHeight = firstChild.getHeight();
                }

                // Calculate total height with some extra space
                int totalHeight = itemCount * estimatedItemHeight + 50; // Add some padding

                // Set new height
                ViewGroup.LayoutParams params = recyclerQuestions.getLayoutParams();
                params.height = totalHeight;
                recyclerQuestions.setLayoutParams(params);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }

    // Helper class to store question data with options
    public static class QuestionWithOptions {
        private int questionNumber;
        private String questionText;
        private List<OptionItem> options;

        public QuestionWithOptions(int questionNumber, String questionText, List<OptionItem> options) {
            this.questionNumber = questionNumber;
            this.questionText = questionText;
            this.options = options;
        }

        public int getQuestionNumber() {
            return questionNumber;
        }

        public void setQuestionNumber(int questionNumber) {
            this.questionNumber = questionNumber;
        }

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public List<OptionItem> getOptions() {
            return options;
        }

        public void setOptions(List<OptionItem> options) {
            this.options = options;
        }

        public String getCorrectAnswerText() {
            for (OptionItem option : options) {
                if (option.isCorrect()) {
                    return option.getText();
                }
            }
            return "";
        }
    }

    // Helper class to store option data
    public static class OptionItem {
        private String text;
        private boolean isCorrect;

        public OptionItem(String text, boolean isCorrect) {
            this.text = text;
            this.isCorrect = isCorrect;
        }

        public String getText() {
            return text;
        }

        public boolean isCorrect() {
            return isCorrect;
        }
    }
}