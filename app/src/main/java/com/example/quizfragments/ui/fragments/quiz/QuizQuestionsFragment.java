package com.example.quizfragments.ui.fragments.quiz;

import android.os.Bundle;
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
import com.example.quizfragments.ui.adapters.QuestionAdapter;
import com.example.quizfragments.ui.fragments.quiz.QuestionWithStatus;

import java.util.ArrayList;
import java.util.List;

public class QuizQuestionsFragment extends Fragment implements QuestionAdapter.OnQuestionClickListener {

    private static final String ARG_QUIZ_ID = "quiz_id";
    private static final String ARG_QUIZ_TITLE = "quiz_title";

    private TextView tvQuizTitle;
    private TextView tvQuestionCount;
    private TextView tvLastAttempt;
    private TextView tvProgress;
    private ProgressBar progressBar;
    private RecyclerView rvQuestions;
    private QuestionAdapter adapter;
    private Button btnStartQuiz;
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
        if (getArguments() != null) {
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
        tvQuizTitle = view.findViewById(R.id.tvQuizTitle);
        tvQuestionCount = view.findViewById(R.id.tvQuestionCount);
        tvLastAttempt = view.findViewById(R.id.tvLastAttempt);
        tvProgress = view.findViewById(R.id.tvProgress);
        progressBar = view.findViewById(R.id.progressBar);
        rvQuestions = view.findViewById(R.id.rvQuestions);
        btnStartQuiz = view.findViewById(R.id.btnStartQuiz);
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
        // In a real app, this would be loaded from database
        // For demonstration, we'll create sample data similar to the image

        questions.clear();

        // Create sample questions to match the screenshot
        questions.add(new QuestionWithStatus(1, "What is the basic unit of life?", 1, "Cell", "Cell"));
        questions.add(new QuestionWithStatus(2, "What is the powerhouse of the cell?", 2, "Mitochondria", "Nucleus"));
        questions.add(new QuestionWithStatus(3, "Which molecule carries genetic information?", 1, "DNA", "DNA"));
        questions.add(new QuestionWithStatus(4, "What process do plants use to make food?", 1, "Photosynthesis", "Photosynthesis"));
        questions.add(new QuestionWithStatus(5, "What is the primary function of mitochondria?", 1, "Energy production", "Energy production"));
        questions.add(new QuestionWithStatus(6, "What is cellular respiration?", 0, "Process of converting glucose to energy", ""));
        questions.add(new QuestionWithStatus(7, "What are the building blocks of proteins?", 0, "Amino acids", ""));
        questions.add(new QuestionWithStatus(8, "What is the process of cell division called?", 0, "Mitosis", ""));
        questions.add(new QuestionWithStatus(9, "What is homeostasis?", 0, "Maintaining stable internal conditions", ""));
        questions.add(new QuestionWithStatus(10, "What is an organelle?", 0, "Specialized structure within a cell", ""));

        // Update adapter
        adapter.updateQuestions(questions);

        // Update progress
        updateProgress();
    }

    private void updateProgress() {
        // Count attempted questions
        int attempted = 0;
        for (QuestionWithStatus question : questions) {
            if (question.getAttempted() != 0) {
                attempted++;
            }
        }

        // Update progress text and bar
        int total = questions.size();
        tvProgress.setText("Progress: " + attempted + "/" + total);
        progressBar.setMax(total);
        progressBar.setProgress(attempted);

        // Update question count
        tvQuestionCount.setText(total + " Questions");
    }

    private void startQuiz() {
        Toast.makeText(requireContext(), "Starting quiz...", Toast.LENGTH_SHORT).show();
        // In a real app, this would navigate to the first unattempted question
    }

    @Override
    public void onQuestionClick(QuestionWithStatus question, int position) {
        Toast.makeText(requireContext(), "Question " + (position + 1) + " clicked", Toast.LENGTH_SHORT).show();
        // In a real app, this would navigate to the question detail/answer page
    }
}