package com.example.quizfragments.ui.fragments.flashcard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Flashcard;
import com.example.quizfragments.data.db.entities.FlashcardDeck;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreateFlashcardFragment extends Fragment {

    private EditText etDeckName;
    private EditText etDeckDescription;
    private LinearLayout flashcardsContainer;
    private ScrollView scrollView;
    private final List<CardView> cardViews = new ArrayList<>();
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_flashcard, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> navigateBack());

        etDeckName = view.findViewById(R.id.et_deck_name);
        flashcardsContainer = view.findViewById(R.id.flashcards_container);
        Button btnAddCard = view.findViewById(R.id.btn_add_card);
        Button btnSaveDeck = view.findViewById(R.id.btn_save_deck);
        scrollView = view.findViewById(R.id.scroll_view);

        btnAddCard.setOnClickListener(v -> addNewCard());
        btnSaveDeck.setOnClickListener(v -> saveDeck());

        addNewCard();

        return view;
    }

    private void addNewCard() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View cardView = inflater.inflate(R.layout.item_flashcard_edit, flashcardsContainer, false);

        ImageButton btnRemove = cardView.findViewById(R.id.btn_remove_card);
        btnRemove.setOnClickListener(v -> {
            if (cardViews.size() > 1) {
                flashcardsContainer.removeView(cardView);
                cardViews.remove(new CardView(cardView));
            } else {
                Toast.makeText(requireContext(), "At least one card is required", Toast.LENGTH_SHORT).show();
            }
        });

        flashcardsContainer.addView(cardView);
        cardViews.add(new CardView(cardView));

        // Scroll to the bottom to show the newly added card
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void saveDeck() {
        String deckName = etDeckName.getText().toString().trim();
        String deckDescription = etDeckDescription != null ?
                etDeckDescription.getText().toString().trim() : "";

        if (deckName.isEmpty()) {
            etDeckName.setError("Deck name is required");
            return;
        }

        // Validate all cards
        List<FlashcardData> validatedCards = validateCards();
        if (validatedCards == null) {
            // Validation failed
            return;
        }

        // Create and save the deck and its flashcards
        executor.execute(() -> {
            AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

            // Create deck with the new FlashcardDeck constructor
            FlashcardDeck deck = new FlashcardDeck(deckName, deckDescription);
            db.flashcardDeckDao().insert(deck);

            // We need to retrieve the deck to get its ID
            List<FlashcardDeck> decks = db.flashcardDeckDao().getAllDecks();
            FlashcardDeck insertedDeck = null;
            for (FlashcardDeck d : decks) {
                if (d.getTitle().equals(deckName)) {
                    insertedDeck = d;
                    break;
                }
            }

            if (insertedDeck == null) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to save deck", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            int deckId = insertedDeck.getId();

            // Create flashcards
            for (int i = 0; i < validatedCards.size(); i++) {
                FlashcardData card = validatedCards.get(i);
                Flashcard flashcard = new Flashcard(card.question, card.answer, deckId);
                flashcard.setPosition(i);
                db.flashcardDao().insert(flashcard);
            }

            // Update UI on the main thread
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Deck saved successfully", Toast.LENGTH_SHORT).show();
                // Go back to the FlashcardFragment
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            });
        });
    }

    private List<FlashcardData> validateCards() {
        List<FlashcardData> cards = new ArrayList<>();

        for (CardView cardView : cardViews) {
            EditText etQuestion = cardView.view.findViewById(R.id.et_question);
            EditText etAnswer = cardView.view.findViewById(R.id.et_answer);

            String question = etQuestion.getText().toString().trim();
            String answer = etAnswer.getText().toString().trim();

            if (question.isEmpty()) {
                etQuestion.setError("Question is required");
                scrollToView(cardView.view);
                return null;
            }

            if (answer.isEmpty()) {
                etAnswer.setError("Answer is required");
                scrollToView(cardView.view);
                return null;
            }

            cards.add(new FlashcardData(question, answer));
        }

        return cards;
    }

    private void scrollToView(View view) {
        int viewTop = view.getTop();
        scrollView.smoothScrollTo(0, viewTop);
    }

    // Helper class to store flashcard question and answer
    private static class FlashcardData {
        String question;
        String answer;

        FlashcardData(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }

    private static class CardView {
        View view;

        CardView(View view) {
            this.view = view;
        }
    }

    private void navigateBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}