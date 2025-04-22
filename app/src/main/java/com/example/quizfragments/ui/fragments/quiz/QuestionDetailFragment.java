package com.example.quizfragments.ui.fragments.quiz;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Question;
import com.example.quizfragments.data.db.entities.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuestionDetailFragment extends Fragment {

    private static final String ARG_QUIZ_ID = "quiz_id";
    private static final String ARG_QUESTION_ID = "question_id";

    private int quizId;
    private int questionId;
    private TextView tvQuestionNumber;
    private TextView tvQuestionText;
    private TextView tvProgress;
    private ProgressBar progressBar;
    private RadioGroup radioGroupOptions;
    private List<RadioButton> optionButtons;
    private Question currentQuestion;
    private List<Option> currentOptions;
    private int totalQuestions;
    private int currentPosition;
    private TextView feedback;

    public static QuestionDetailFragment newInstance(int quizId, int questionId) {
        QuestionDetailFragment fragment = new QuestionDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_QUIZ_ID, quizId);
        args.putInt(ARG_QUESTION_ID, questionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            quizId = getArguments().getInt(ARG_QUIZ_ID);
            questionId = getArguments().getInt(ARG_QUESTION_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.question_detail_fragment, container, false);

        tvQuestionNumber = view.findViewById(R.id.tv_question_number);
        tvQuestionText = view.findViewById(R.id.tv_question_text);
        tvProgress = view.findViewById(R.id.tv_progress);
        progressBar = view.findViewById(R.id.progress_bar);
        radioGroupOptions = view.findViewById(R.id.radio_group_options);

        optionButtons = new ArrayList<>();
        optionButtons.add(view.findViewById(R.id.option1));
        optionButtons.add(view.findViewById(R.id.option2));
        optionButtons.add(view.findViewById(R.id.option3));
        optionButtons.add(view.findViewById(R.id.option4));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        feedback = view.findViewById(R.id.feedback);

        Log.d("QuestionDetailFragment", "Setting up radio group listener");

        radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> {
            // More explicit logging
            Log.d("QuestionDetailFragment", "Radio button clicked! ID: " + checkedId);

            if (checkedId != -1) {
                for (int i = 0; i < optionButtons.size(); i++) {
                    if (optionButtons.get(i).getId() == checkedId) {
                        Log.d("QuestionDetailFragment", "Option " + (i+1) + " selected");
                        updateUserAnswer(currentOptions.get(i).optionId);
                        break;
                    }
                }
            }
        });

        // Load data after everything is set up
        loadQuestionData();
    }

    private void loadQuestionData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Load question and options from database
            AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();
            currentQuestion = db.questionDao().getQuestionById(questionId);
            currentOptions = db.optionDao().getOptionsByQuestion(questionId);

            // Update UI on main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::updateUI);
            }
        });
        executor.shutdown();
    }

    private void updateUI() {
        if (currentQuestion == null || currentOptions == null || currentOptions.isEmpty()) {

        }
        tvQuestionNumber.setText("Question " + currentQuestion.questionNumber);
        tvQuestionText.setText(currentQuestion.questionText);
        tvProgress.setText("Progress: " + currentPosition + "/" + totalQuestions);
        progressBar.setMax(totalQuestions);
        progressBar.setProgress(currentPosition);



        // Set options text
        for (int i = 0; i < Math.min(optionButtons.size(), currentOptions.size()); i++) {
            optionButtons.get(i).setText(currentOptions.get(i).optionText);
            if(currentQuestion.attempted != 0){     //if user selected option before
                if(currentQuestion.userAnswer == currentOptions.get(i).optionId){
                    radioGroupOptions.check(optionButtons.get(i).getId());
                }
            }
        }
        // Check the selected option if any
        if (currentQuestion.userAnswer == -1) {
            Log.d("user ans", "is " + currentQuestion.userAnswer);
            for (int i = 0; i < currentOptions.size(); i++) {
                if (currentOptions.get(i).optionId == currentQuestion.userAnswer) {
                    optionButtons.get(i).setChecked(true);
                    break;
                }
            }
        }
    }

    private void updateUserAnswer(int optionId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();
            // Update question in database with user's selected answer
            currentQuestion.userAnswer = optionId;

            // Check if answer is correct
            boolean isCorrect = false;
            for (Option option : currentOptions) {
                if (option.optionId == optionId && option.isCorrect) {
                    isCorrect = true;
                    break;
                }
            }
            if (isCorrect) {
                requireActivity().runOnUiThread(() -> {
                    feedback.setText("Correct answer!");  // Update UI element here
                    feedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark));  // Set text color to green
                });
            }else{
                requireActivity().runOnUiThread(() -> {
                    feedback.setText("Wrong answer, please try again!");  // Update UI element here
                    feedback.setTextColor(getResources().getColor(android.R.color.holo_red_dark));  // Set text color to red
                });
            }


            currentQuestion.attempted = isCorrect ? 1 : 2;      //1 if correct, 2 if incorrect, 0 if reset
            db.questionDao().updateAttempted(currentQuestion.questionId, currentQuestion.attempted);
            db.questionDao().updateUserAnswer(currentQuestion.questionId, optionId);
        });
        executor.shutdown();
    }
}