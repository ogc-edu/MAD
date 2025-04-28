package com.example.quizfragments.ui.fragments.flashcard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Flashcard;
import com.example.quizfragments.data.db.entities.FlashcardDeck;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EditFlashcardFragment extends Fragment {

    private static final String ARG_DECK_ID = "deck_id";
    private int deckId;
    private FlashcardDeck currentDeck;
    private List<Flashcard> flashcards = new ArrayList<>();
    private LinearLayout flashcardsContainer;
    private TextInputEditText etDeckName;
    private TextInputEditText etDeckDescription;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public static EditFlashcardFragment newInstance(int deckId) {
        EditFlashcardFragment fragment = new EditFlashcardFragment();
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
        View view = inflater.inflate(R.layout.fragment_edit_flashcard, container, false);

        flashcardsContainer = view.findViewById(R.id.flashcards_container);
        etDeckName = view.findViewById(R.id.et_deck_name);
        etDeckDescription = view.findViewById(R.id.et_deck_description);
        Button btnAddCard = view.findViewById(R.id.btn_add_card);
        Button btnSaveDeck = view.findViewById(R.id.btn_save_deck);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> navigateBack());
        btnAddCard.setOnClickListener(v -> addNewFlashcardView());
        btnSaveDeck.setOnClickListener(v -> saveDeckAndFlashcards());

        loadDeckAndFlashcards();

        return view;
    }

    private void loadDeckAndFlashcards() {
        executor.execute(() -> {
            AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();
            currentDeck = db.flashcardDeckDao().getDeckById(deckId);
            flashcards = db.flashcardDao().getFlashcardsByDeckId(deckId);

            requireActivity().runOnUiThread(() -> {
                if (currentDeck != null) {
                    etDeckName.setText(currentDeck.getTitle());
                    etDeckDescription.setText(currentDeck.getDescription());
                }

                if (flashcards.isEmpty()) {
                    addNewFlashcardView();
                } else {
                    for (Flashcard flashcard : flashcards) {
                        addFlashcardView(flashcard);
                    }
                }
            });
        });
    }

    private void addFlashcardView(Flashcard flashcard) {
        View cardView = LayoutInflater.from(requireContext()).inflate(R.layout.item_flashcard_edit, flashcardsContainer, false);

        TextInputEditText etQuestion = cardView.findViewById(R.id.et_question);
        TextInputEditText etAnswer = cardView.findViewById(R.id.et_answer);
        ImageButton btnRemoveCard = cardView.findViewById(R.id.btn_remove_card);

        etQuestion.setText(flashcard.getQuestion());
        etAnswer.setText(flashcard.getAnswer());

        cardView.setTag(flashcard.getId());

        btnRemoveCard.setOnClickListener(v -> {
            if (flashcardsContainer.getChildCount() > 1) {
                flashcardsContainer.removeView(cardView);
            } else {
                Toast.makeText(requireContext(), "You must have at least one flashcard", Toast.LENGTH_SHORT).show();
            }
        });

        flashcardsContainer.addView(cardView);
    }

    private void addNewFlashcardView() {
        View cardView = LayoutInflater.from(requireContext()).inflate(R.layout.item_flashcard_edit, flashcardsContainer, false);

        ImageButton btnRemoveCard = cardView.findViewById(R.id.btn_remove_card);

        cardView.setTag(-1);

        btnRemoveCard.setOnClickListener(v -> {
            if (flashcardsContainer.getChildCount() > 1) {
                flashcardsContainer.removeView(cardView);
            } else {
                Toast.makeText(requireContext(), "You must have at least one flashcard", Toast.LENGTH_SHORT).show();
            }
        });

        flashcardsContainer.addView(cardView);
    }

    private void saveDeckAndFlashcards() {
        String deckName = etDeckName.getText().toString().trim();
        String deckDescription = etDeckDescription.getText().toString().trim();

        if (deckName.isEmpty()) {
            etDeckName.setError("Deck name is required");
            return;
        }

        List<Pair<Integer, Flashcard>> flashcardsToSave = new ArrayList<>();
        List<Integer> flashcardIdsToDelete = new ArrayList<>();
        List<Integer> existingIds = new ArrayList<>();

        for (Flashcard flashcard : flashcards) {
            existingIds.add(flashcard.getId());
        }

        for (int i = 0; i < flashcardsContainer.getChildCount(); i++) {
            View cardView = flashcardsContainer.getChildAt(i);

            TextInputEditText etQuestion = cardView.findViewById(R.id.et_question);
            TextInputEditText etAnswer = cardView.findViewById(R.id.et_answer);

            String question = etQuestion.getText().toString().trim();
            String answer = etAnswer.getText().toString().trim();

            if (question.isEmpty()) {
                etQuestion.setError("Question is required");
                return;
            }

            if (answer.isEmpty()) {
                etAnswer.setError("Answer is required");
                return;
            }

            int flashcardId = (int) cardView.getTag();
            Flashcard flashcard = new Flashcard(question, answer, deckId);
            flashcard.setPosition(i);

            if (flashcardId != -1) {
                flashcard.setId(flashcardId);
                existingIds.remove(Integer.valueOf(flashcardId));
            }

            flashcardsToSave.add(new Pair<>(flashcardId, flashcard));
        }

        flashcardIdsToDelete.addAll(existingIds);

        executor.execute(() -> {
            AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

            currentDeck.setTitle(deckName);
            currentDeck.setDescription(deckDescription);
            currentDeck.setUpdatedAt(System.currentTimeMillis());
            db.flashcardDeckDao().update(currentDeck);

            for (Integer idToDelete : flashcardIdsToDelete) {
                Flashcard flashcardToDelete = db.flashcardDao().getFlashcardById(idToDelete);
                if (flashcardToDelete != null) {
                    db.flashcardDao().delete(flashcardToDelete);
                }
            }

            for (Pair<Integer, Flashcard> pair : flashcardsToSave) {
                if (pair.first == -1) {
                    // New flashcard
                    db.flashcardDao().insert(pair.second);
                } else {
                    // Existing flashcard
                    db.flashcardDao().update(pair.second);
                }
            }

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Flashcard deck updated successfully", Toast.LENGTH_SHORT).show();
                navigateToStudyFragment();
            });
        });
    }

    private void navigateBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void navigateToStudyFragment() {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, FlashcardStudyFragment.newInstance(deckId))
                .commit();
    }

    private static class Pair<F, S> {
        final F first;
        final S second;

        Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }
}