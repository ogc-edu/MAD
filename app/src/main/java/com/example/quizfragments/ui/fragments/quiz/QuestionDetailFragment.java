package com.example.quizfragments.ui.fragments.quiz;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.dao.OptionDao;
import com.example.quizfragments.data.db.dao.QuestionDao;
import com.example.quizfragments.data.db.dao.QuizDao;
import com.example.quizfragments.data.db.entities.Question;
import com.example.quizfragments.data.db.entities.Option;
import com.example.quizfragments.data.db.entities.Quiz;

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
    private Quiz currentQuiz;
    private Question currentQuestion;
    private List<Option> currentOptions;
    Button nextBtn;
    TextView quizTitle;
    private TextView feedback;
    private ImageButton backBtn;

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
        quizTitle = view.findViewById(R.id.quiz_title);
        tvQuestionNumber = view.findViewById(R.id.tv_question_number);
        tvQuestionText = view.findViewById(R.id.tv_question_text);
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
        nextBtn = view.findViewById(R.id.btn_next);
        nextBtn.setOnClickListener(v -> {

            // Run the database query in a background thread
            new Thread(() -> {
                // Get the database instance
                AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();
                QuestionDao questionDao = db.questionDao();
                Question question = questionDao.getQuestionById(questionId);
                Log.d("current number", "is " + questionId);
                Log.d("quiz number", "is " + quizId);
                Question nextQuestion = questionDao.getNextQuestion(quizId, question.questionNumber); // Query for the next question
                Log.d("next number", "is " + nextQuestion);
                // Check if a next question was found
                if (nextQuestion != null) {
                    // Run UI update on the main thread
                    getActivity().runOnUiThread(() -> {
                        // Create a new fragment with the next question's data
                        QuestionDetailFragment fragment = QuestionDetailFragment.newInstance(quizId, nextQuestion.questionId);

                        // Replace the current fragment with the new one
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .commit();

                        // Optionally, update UI if necessary
                        updateUI();
                    });
                } else {
                    // Handle the case where no next question is found, like showing a message or handling the end of quiz
                    getActivity().runOnUiThread(() -> {
                        // Show a message or handle the scenario
                        Toast.makeText(requireContext(), "No more questions available", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

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
            currentQuiz = db.quizDao().getQuizById(quizId);
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
        quizTitle.setText(currentQuiz.title);
        tvQuestionNumber.setText("Question " + currentQuestion.questionNumber);
        tvQuestionText.setText(currentQuestion.questionText);

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