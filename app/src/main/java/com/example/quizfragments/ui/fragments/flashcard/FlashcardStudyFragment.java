package com.example.quizfragments.ui.fragments.flashcard;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Flashcard;
import com.example.quizfragments.data.db.entities.FlashcardDeck;
import com.example.quizfragments.ui.adapters.FlashcardPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FlashcardStudyFragment extends Fragment {

    private static final String ARG_DECK_ID = "deck_id";

    private int deckId;
    private FlashcardDeck currentDeck;
    private List<Flashcard> flashcards = new ArrayList<>();

    private TextView tvDeckTitle;
    private TextView tvCardCount;
    private ViewPager2 viewPagerFlashcards;

    private AnimatorSet showFrontAnimator;
    private AnimatorSet showBackAnimator;
    private FlashcardPagerAdapter adapter;

    private final Executor executor = Executors.newSingleThreadExecutor();

    public static FlashcardStudyFragment newInstance(int deckId) {
        FlashcardStudyFragment fragment = new FlashcardStudyFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DECK_ID, deckId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deckId = getArguments().getInt(ARG_DECK_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcard_study, container, false);

        // Initialize views
        tvDeckTitle = view.findViewById(R.id.tv_deck_title);
        tvCardCount = view.findViewById(R.id.tv_card_count);
        viewPagerFlashcards = view.findViewById(R.id.viewpager_flashcards);

        // Set up animations
        setupAnimations();

        // Load data
        loadFlashcards();

        return view;
    }

    private void setupAnimations() {
        showFrontAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(requireContext(), R.animator.card_flip_front);
        showBackAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(requireContext(), R.animator.card_flip_back);
    }

    private void loadFlashcards() {
        executor.execute(() -> {
            AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

            // Get deck
            currentDeck = db.flashcardDeckDao().getDeckById(deckId);

            // Get flashcards
            flashcards = db.flashcardDao().getFlashcardsByDeckId(deckId);

            requireActivity().runOnUiThread(() -> {
                if (currentDeck != null) {
                    tvDeckTitle.setText(currentDeck.getTitle());
                }

                if (flashcards.isEmpty()) {
                    // Handle empty deck
                    showEmptyState();
                } else {
                    // Setup ViewPager with cards
                    setupViewPager();
                }
            });
        });
    }

    private void setupViewPager() {
        adapter = new FlashcardPagerAdapter(flashcards, showFrontAnimator, showBackAnimator);
        viewPagerFlashcards.setAdapter(adapter);
        viewPagerFlashcards.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateCardCounter(position);
            }
        });

        updateCardCounter(0);
    }

    private void updateCardCounter(int position) {
        tvCardCount.setText(String.format("%d/%d", position + 1, flashcards.size()));
    }

    private void showEmptyState() {
        viewPagerFlashcards.setVisibility(View.GONE);
        tvCardCount.setText("No flashcards in this deck");
    }
}