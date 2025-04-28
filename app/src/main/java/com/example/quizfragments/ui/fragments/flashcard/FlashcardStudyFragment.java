package com.example.quizfragments.ui.fragments.flashcard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        ImageButton btnMenu = view.findViewById(R.id.btnMenu);

        btnBack.setOnClickListener(v -> navigateBack());

        // Set up menu button
        btnMenu.setOnClickListener(v -> showPopupMenu(v));

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

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.inflate(R.menu.flashcard_study_menu);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_edit) {
                navigateToEditFlashcards();
                return true;
            } else if (itemId == R.id.action_delete) {
                showDeleteConfirmationDialog();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void navigateToEditFlashcards() {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, EditFlashcardFragment.newInstance(deckId))
                .addToBackStack(null)
                .commit();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Flashcard Deck")
                .setMessage("Are you sure you want to delete this flashcard deck? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> deleteDeck())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteDeck() {
        executor.execute(() -> {
            AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();
            FlashcardDeck deck = db.flashcardDeckDao().getDeckById(deckId);

            if (deck != null) {
                db.flashcardDeckDao().delete(deck);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Deck deleted successfully", Toast.LENGTH_SHORT).show();
                    navigateToFlashcardFragment();
                });
            }
        });
    }

    private void navigateToFlashcardFragment() {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new FlashcardFragment())
                .commit();
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
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new FlashcardFragment())
                .addToBackStack(null)
                .commit();
    }
}