package com.example.quizfragments.ui.fragments.flashcard;

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
import com.example.quizfragments.ui.adapters.FlashcardAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FlashcardStudyFragment extends Fragment {

    private static final String ARG_DECK_ID = "deck_id";
    private int deckId;
    private FlashcardDeck currentDeck;
    private List<Flashcard> flashcards = new ArrayList<>();
    private FlashcardAdapter adapter;
    private TextView tvCardCount;
    private TextView tvDeckTitle;
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

        ViewPager2 viewPager = view.findViewById(R.id.viewpager_flashcards);
        tvCardCount = view.findViewById(R.id.tv_card_count);
        tvDeckTitle = view.findViewById(R.id.tv_deck_title);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> navigateBack());

        adapter = new FlashcardAdapter(requireContext());
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateCardCount(position);
            }
        });

        loadFlashcards();

        return view;
    }

    private void loadFlashcards() {
        executor.execute(() -> {
            AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

            currentDeck = db.flashcardDeckDao().getDeckById(deckId);

            flashcards = db.flashcardDao().getFlashcardsByDeckId(deckId);

            requireActivity().runOnUiThread(() -> {
                if (currentDeck != null) {
                    tvDeckTitle.setText(currentDeck.getTitle());
                }

                if (flashcards.isEmpty()) {
                    showEmptyState();
                } else {
                    adapter.setFlashcards(flashcards);
                    updateCardCount(0);
                }
            });
        });
    }

    private void showEmptyState() {
        // Show empty state UI
        tvCardCount.setText("No flashcards in this deck");
    }

    private void updateCardCount(int position) {
        if (!flashcards.isEmpty()) {
            tvCardCount.setText(String.format("%d/%d", position + 1, flashcards.size()));
        }
    }

    private void navigateBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}