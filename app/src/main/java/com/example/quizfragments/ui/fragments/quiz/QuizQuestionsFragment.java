package com.example.quizfragments.ui.fragments.quiz;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.dao.OptionDao;
import com.example.quizfragments.data.db.dao.QuestionDao;
import com.example.quizfragments.data.db.dao.QuizDao;
import com.example.quizfragments.data.db.entities.Option;
import com.example.quizfragments.data.db.entities.Question;
import com.example.quizfragments.ui.adapters.QuestionAdapter;
import com.example.quizfragments.ui.fragments.quiz.QuestionWithStatus;
import com.example.quizfragments.ui.fragments.quiz.QuizQuestionsFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuizQuestionsFragment extends Fragment implements QuestionAdapter.OnQuestionClickListener {

    private static final String ARG_QUIZ_ID = "quiz_id";
    private static final String ARG_QUIZ_TITLE = "quiz_title";
    private TextView label;
    private TextView tvQuestionCount;
    private TextView tvProgress;
    private QuestionAdapter adapter;
    private ImageButton btnBack;
    private int quizId;
    private String quizTitle;
    private List<QuestionWithStatus> questions = new ArrayList<>();

    public static QuizQuestionsFragment newInstance(int quizId, String quizTitle) {
        QuizQuestionsFragment fragment = new QuizQuestionsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_QUIZ_ID, quizId);
        args.putString(ARG_QUIZ_TITLE, quizTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {       //if no arguments was passed
            quizId = getArguments().getInt(ARG_QUIZ_ID, -1);
            quizTitle = getArguments().getString(ARG_QUIZ_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (quizId == -1) {
            Toast.makeText(getContext(), "Error: Quiz not found", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return;
        }

        // Initialize views
        label = view.findViewById(R.id.label);
        TextView tvQuizTitle = view.findViewById(R.id.quizTitle);
        tvQuestionCount = view.findViewById(R.id.tvQuestionCount);
        tvProgress = view.findViewById(R.id.tvProgress);
        RecyclerView rvQuestions = view.findViewById(R.id.rvQuestions);
        Button btnStartQuiz = view.findViewById(R.id.btnStartQuiz);
        btnBack = view.findViewById(R.id.btnBack);

        // Set quiz title
        tvQuizTitle.setText(quizTitle != null ? quizTitle : "Quiz");

        // Set up RecyclerView
        rvQuestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvQuestions.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        // Create adapter
        adapter = new QuestionAdapter(requireContext(), questions, this);
        rvQuestions.setAdapter(adapter);

        // Set up click listeners
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnStartQuiz.setOnClickListener(v -> startQuiz());

        // Load questions
        loadQuestions();
    }

    private void loadQuestions() {
        questions.clear();
        //run database
        new Thread(() -> {
            AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();
            QuestionDao questionDao = db.questionDao();
            OptionDao optionDao = db.optionDao();
            QuizDao quizDao = db.quizDao();

            // Get all questions for the given quiz
            List<Question> rawQuestions = questionDao.getQuestionByQuizId(quizId);
            List<QuestionWithStatus> questionWithStatusList = new ArrayList<>();
            for (Question q : rawQuestions) {
                // Fetch the correct answer option for this question
                String correctAnswer = optionDao.getCorrectAnswerForQuestion(q.questionId); // You need this method

                //get user answer, if not answered before or reset, answer is -1
                Option userAnswerOption;
                if(q.userAnswer != -1){
                    userAnswerOption = optionDao.getOptionById(q.userAnswer);
                }else{
                    userAnswerOption = null;
                }
                String userAnswer = userAnswerOption != null ? userAnswerOption.optionText : "Not answered";

                // Construct your custom object
                questionWithStatusList.add(new QuestionWithStatus(
                        q.questionId,
                        q.questionText,
                        q.attempted,
                        correctAnswer,
                        userAnswer
                ));
            }

            int questionCount = quizDao.getQuizQuestionCount(quizId);
            int numAttempt = quizDao.getNumberOfAttemptedQuestion(quizId);

            requireActivity().runOnUiThread(() -> {
                label.setText(String.valueOf(quizTitle.charAt(0)));
                questions = questionWithStatusList;
                adapter.updateQuestions(questions);
                tvQuestionCount.setText(questionCount + " Questions");
                tvProgress.setText("Progress: " + numAttempt + "/" + questionCount);

                // Add these lines to update the ProgressBar
                ProgressBar progressBar = getView().findViewById(R.id.progressBar);
                progressBar.setMax(questionCount);
                progressBar.setProgress(numAttempt);
            });

        }).start();
    }
    
    private void startQuiz() {
        Toast.makeText(requireContext(), "Starting quiz...", Toast.LENGTH_SHORT).show();
        // In a real app, this would navigate to the first unattempted question
    }

    @Override
    public void onQuestionClick(QuestionWithStatus question, int position) {
        QuestionDetailFragment fragment = QuestionDetailFragment.newInstance(quizId, question.getQuestionId());
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}