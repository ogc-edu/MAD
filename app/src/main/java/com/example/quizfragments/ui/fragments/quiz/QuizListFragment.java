package com.example.quizfragments.ui.fragments.quiz;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.example.quizfragments.data.db.entities.Quiz;
import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.ui.adapters.QuizAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.quizfragments.ui.adapters.QuizAdapter;

public class QuizListFragment extends Fragment implements QuizAdapter.OnQuizClickListener {

    private QuizAdapter quizAdapter;
    private List<Quiz> allQuizzes = new ArrayList<>();
    private AppDatabase db;
    private EditText searchEditText;
    private Button btnAll, btnScience, btnHistory;

    // FAB variables
    private FloatingActionButton fabMain, fabCreateQuiz, fabGenerateAI;
    private TextView tvCreateQuizLabel, tvGenerateAILabel;
    private View fabOverlay;
    private boolean isFabMenuOpen = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_list, container, false);

        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_quizzes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        quizAdapter = new QuizAdapter(getContext(), this);
        recyclerView.setAdapter(quizAdapter);

        // Initialize search and filter views
        searchEditText = view.findViewById(R.id.edit_search);

        // Initialize FAB components
        fabMain = view.findViewById(R.id.fab_main);
        fabCreateQuiz = view.findViewById(R.id.menu_new);
        fabGenerateAI = view.findViewById(R.id.menu_ai);
        tvCreateQuizLabel = view.findViewById(R.id.tv_new_quiz_label);
        tvGenerateAILabel = view.findViewById(R.id.tv_ai_gen_label);
        fabOverlay = view.findViewById(R.id.fab_overlay);

        // Set FAB listeners
        setupFabListeners();

        setupListeners();

        // Initialize database
        db = Room.databaseBuilder(requireContext(),
                AppDatabase.class, "database").build();

        // Load quizzes from database
        loadQuizzes();

        return view;
    }

    private void setupFabListeners() {
        // Main FAB click listener
        fabMain.setOnClickListener(view -> {
            if (isFabMenuOpen) {
                closeFabMenu();
            } else {
                openFabMenu();
            }
        });

        fabCreateQuiz.setOnClickListener(view -> {
            closeFabMenu();
            navigateToCreateQuizFragment();
        });

        fabGenerateAI.setOnClickListener(view -> {
            closeFabMenu();
            navigateToQuizGeneratorFragment();
        });

        fabOverlay.setOnClickListener(view -> closeFabMenu());
    }

    private void openFabMenu() {
        isFabMenuOpen = true;
        fabOverlay.setVisibility(View.VISIBLE);
        fabCreateQuiz.setVisibility(View.VISIBLE);
        fabGenerateAI.setVisibility(View.VISIBLE);
        tvCreateQuizLabel.setVisibility(View.VISIBLE);
        tvGenerateAILabel.setVisibility(View.VISIBLE);
        fabMain.animate().rotation(45f).setDuration(300).start();
    }

    private void closeFabMenu() {
        isFabMenuOpen = false;
        fabOverlay.setVisibility(View.GONE);
        fabCreateQuiz.setVisibility(View.INVISIBLE);
        fabGenerateAI.setVisibility(View.INVISIBLE);
        tvCreateQuizLabel.setVisibility(View.INVISIBLE);
        tvGenerateAILabel.setVisibility(View.INVISIBLE);
        fabMain.animate().rotation(0f).setDuration(300).start();
    }

    private void navigateToCreateQuizFragment() {
        // Navigate to CreateQuizFragment
        CreateQuizFragment createQuizFragment = new CreateQuizFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, createQuizFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToQuizGeneratorFragment() {
        // Navigate to QuizGeneratorFragment
        QuizGeneratorFragment quizGeneratorFragment = new QuizGeneratorFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, quizGeneratorFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onQuizClick(Quiz quiz) {
        // Navigate to QuizQuestionsFragment
        QuizQuestionsFragment questionsFragment = QuizQuestionsFragment.newInstance(quiz.quizId, quiz.title);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, questionsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupListeners() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterQuizzes(s.toString());
            }
        });
    }
    private void filterQuizzes(String query) {
        List<Quiz> filteredList = new ArrayList<>();

        for (Quiz quiz : allQuizzes) {
            if (quiz.title.toLowerCase().contains(query.toLowerCase()) ||
                    quiz.description.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(quiz);
            }
        }

        quizAdapter.setQuizzes(filteredList);
    }

    private void loadQuizzes() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Get quizzes from database
            List<Quiz> quizzes = db.quizDao().getAllQuizzes();

            // If database is empty, add some sample quizzes
            if (quizzes.isEmpty()) {
                Log.d("empty", "no quiz found");
                for (Quiz quiz : quizzes) {
                    db.quizDao().insert(quiz);
                }
                quizzes = db.quizDao().getAllQuizzes(); // Reload to get IDs
            }

            allQuizzes = quizzes;

            // Update UI on main thread
            if (getActivity() != null) {
                List<Quiz> finalQuizzes = quizzes;
                getActivity().runOnUiThread(() -> {
                    if(finalQuizzes.size() == 0){

                    }
                    quizAdapter.setQuizzes(finalQuizzes);
                });
            }
        });
        executor.shutdown();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Make sure the FAB menu is closed when returning to this fragment
        if (isFabMenuOpen) {
            closeFabMenu();
        }
    }
}