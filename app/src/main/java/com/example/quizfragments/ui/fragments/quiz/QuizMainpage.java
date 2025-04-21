package com.example.quizfragments.ui.fragments.quiz;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.ui.adapters.QuizAdapter;
import com.example.quizfragments.ui.viewmodels.QuizViewModel;
import com.example.quizfragments.ui.viewmodels.QuizViewModelFactory;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class QuizMainpage extends Fragment {
    private RecyclerView recyclerView;
    private QuizAdapter quizAdapter;
    private QuizViewModel quizViewModel;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Created", "here created");
        View view = inflater.inflate(R.layout.quiz_mainpage, container, false);

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewQuizzes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // Initialize adapter and set to RecyclerView
        quizAdapter = new QuizAdapter();
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(quizAdapter);

        // Setup SearchView
        searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuizzes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuizzes(newText);
                return true;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("Created", "finish creation");

        // Initialize ViewModel
        quizViewModel = new ViewModelProvider(this, new QuizViewModelFactory(requireActivity().getApplication()))
                .get(QuizViewModel.class);
        Log.d("Quizzes", "Start creating quizzes"); // Check the data size
        quizViewModel.getAllQuizzes().observe(getViewLifecycleOwner(), quizzes -> {
            if (quizzes != null) {
                Log.d("Quizzes", "Number of quizzes: " + quizzes.size());
                quizAdapter.setQuizzes(quizzes);
            } else {
                Log.d("Quizzes", "No quizzes available.");
            }
        });

        // Access the activity to set selected navigation item
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.flashcard_dash);
        }
    }

    private void searchQuizzes(String query) {
        if (query.isEmpty()) {
            quizViewModel.getAllQuizzes().observe(getViewLifecycleOwner(), quizzes -> {
                quizAdapter.setQuizzes(quizzes);
            });
        } else {
            quizViewModel.searchQuizzes("%" + query + "%").observe(getViewLifecycleOwner(), quizzes -> {
                quizAdapter.setQuizzes(quizzes);
            });
        }
    }
}