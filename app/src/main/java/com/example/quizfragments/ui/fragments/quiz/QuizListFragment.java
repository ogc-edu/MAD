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
        btnAll = view.findViewById(R.id.btn_all);
        btnScience = view.findViewById(R.id.btn_science);
        btnHistory = view.findViewById(R.id.btn_history);

        setupListeners();

        // Initialize database
        db = Room.databaseBuilder(requireContext(),
                AppDatabase.class, "database").build();

        // Load quizzes from database
        loadQuizzes();

        return view;
    }

    @Override
    public void onQuizClick(Quiz quiz) {
        // Navigate to QuizQuestionsFragment
        QuizQuestionsFragment questionsFragment = QuizQuestionsFragment.newInstance(quiz.quizId, quiz.title);       //call a new fragment instance(implemented inside fragment file)

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, questionsFragment)
                .addToBackStack(null)
                .commit();
    }
    private void setupListeners() {
        // Search functionality
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

        // Category filter buttons
        btnAll.setOnClickListener(v -> {
            setActiveButton(btnAll);
            quizAdapter.setQuizzes(allQuizzes);
        });

        btnScience.setOnClickListener(v -> {
            setActiveButton(btnScience);
            filterByCategory("Science");
        });

        btnHistory.setOnClickListener(v -> {
            setActiveButton(btnHistory);
            filterByCategory("History");
        });
    }

    private void setActiveButton(Button activeButton) {
        // Reset all buttons
        btnAll.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));
        btnAll.setTextColor(getResources().getColor(android.R.color.black));

        btnScience.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));
        btnScience.setTextColor(getResources().getColor(android.R.color.black));

        btnHistory.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));
        btnHistory.setTextColor(getResources().getColor(android.R.color.black));

        // Set active button
        activeButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_light));
        activeButton.setTextColor(getResources().getColor(android.R.color.white));
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

    private void filterByCategory(String category) {
        List<Quiz> filteredList = new ArrayList<>();

        for (Quiz quiz : allQuizzes) {
            if (quiz.description.contains(category)) {
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
//                quizzes = createSampleQuizzes();
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
                getActivity().runOnUiThread(() -> quizAdapter.setQuizzes(finalQuizzes));
            }
        });
        executor.shutdown();
    }
}