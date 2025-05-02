package com.example.quizfragments.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.quizfragments.ui.fragments.home.HomeFragment;
import com.example.quizfragments.ui.fragments.notes.NotesFragment;
import com.example.quizfragments.R;
import com.example.quizfragments.ui.fragments.flashcard.FlashcardFragment;
import com.example.quizfragments.ui.fragments.quiz.QuizListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.home_dash) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.note_dash) {
                selectedFragment = new NotesFragment();
            } else if (id == R.id.quiz_dash) {
                selectedFragment = new QuizListFragment();
            } else if (id == R.id.flashcard_dash) {
                selectedFragment = new FlashcardFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });


        // Default selection
        bottomNav.setSelectedItemId(R.id.home_dash);
    }
}

