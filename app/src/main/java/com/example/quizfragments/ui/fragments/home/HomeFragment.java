package com.example.quizfragments.ui.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.quizfragments.R;
import com.example.quizfragments.ui.fragments.notes.NotesFragment;
import com.example.quizfragments.ui.fragments.flashcard.FlashcardFragment;
import com.example.quizfragments.ui.fragments.quiz.getData;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize card views
        CardView newNoteCard = view.findViewById(R.id.new_note_card);
        CardView myFlashcardCard = view.findViewById(R.id.my_flashcard_card);
        CardView takeQuizCard = view.findViewById(R.id.take_quiz_card);

        // Set click listeners for each card
        newNoteCard.setOnClickListener(v -> navigateTo(R.id.note_dash));
        myFlashcardCard.setOnClickListener(v -> navigateTo(R.id.flashcard_dash));
        takeQuizCard.setOnClickListener(v -> navigateTo(R.id.quiz_dash));
    }

    private void navigateTo(int destinationId) {
        // Get the bottom navigation view
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);

        // Set the selected item in the bottom navigation
        bottomNav.setSelectedItemId(destinationId);
    }
}
