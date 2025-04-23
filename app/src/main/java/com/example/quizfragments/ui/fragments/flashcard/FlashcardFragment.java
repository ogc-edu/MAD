package com.example.quizfragments.ui.fragments.flashcard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.ui.adapters.FlashcardDeckAdapter;
import com.example.quizfragments.data.db.entities.Folder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class FlashcardFragment extends Fragment {

    private RecyclerView recyclerFlashcardDecks;
    private TextView tvEmptyState;
    private FloatingActionButton fabMain;
    private FloatingActionButton fabCreateManual;
    private FloatingActionButton fabCreateAi;
    private TextView tvCreateManualLabel;
    private TextView tvCreateAiLabel;

    private View fabOverlay;

    private boolean isFabExpanded = false;
    private List<Folder> deckList = new ArrayList<>();
    private FlashcardDeckAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.flashcard_dashboard, container, false);

        // Initialize views
        recyclerFlashcardDecks = view.findViewById(R.id.recycler_flashcard_decks);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        fabMain = view.findViewById(R.id.fab_main);
        fabCreateManual = view.findViewById(R.id.fab_create_manual);
        fabCreateAi = view.findViewById(R.id.fab_create_ai);
        tvCreateManualLabel = view.findViewById(R.id.tv_create_manual_label);
        tvCreateAiLabel = view.findViewById(R.id.tv_create_ai_label);
        fabOverlay = view.findViewById(R.id.fab_overlay);

        setupRecyclerView();
        setupFabListeners();

        loadFlashcardDecks();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new FlashcardDeckAdapter(deckList, deck -> {
            // Handle deck click - navigate to flashcard tutorial/study
            // Navigation.findNavController(requireView()).navigate(
            //     FlashcardFragmentDirections.actionFlashcardFragmentToFlashcardTutorialFragment(deck.getId())
            // );
            showToast("Opening " + deck.getTitle() + " deck");
        });

        recyclerFlashcardDecks.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerFlashcardDecks.setAdapter(adapter);
    }

    private void setupFabListeners() {
        fabMain.setOnClickListener(v -> {
            if (isFabExpanded) {
                collapseFab();
            } else {
                expandFab();
            }
        });

        fabCreateManual.setOnClickListener(v -> {
            collapseFab();
            // Navigate to create flashcard manually
            // Navigation.findNavController(v).navigate(R.id.action_flashcardFragment_to_createFlashcardFragment);
            showToast("Opening Create Flashcards Manually");
        });

        fabCreateAi.setOnClickListener(v -> {
            collapseFab();
            // Navigate to AI flashcard generator
            // Navigation.findNavController(v).navigate(R.id.action_flashcardFragment_to_aiGeneratorFragment);
            showToast("Opening AI Flashcard Generator");
        });

        fabOverlay.setOnClickListener(v -> collapseFab());
    }

    private void expandFab() {
        isFabExpanded = true;

        // Show overlay with animation
        fabOverlay.setVisibility(View.VISIBLE);
        fabOverlay.setAlpha(0f);
        fabOverlay.animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Rotate main FAB
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(fabMain, "rotation", 0f, 45f);
        rotateAnimator.setDuration(300);
        rotateAnimator.setInterpolator(new DecelerateInterpolator());
        rotateAnimator.start();

        // Show child FABs and labels with animation
        fabCreateManual.setVisibility(View.VISIBLE);
        fabCreateAi.setVisibility(View.VISIBLE);
        tvCreateManualLabel.setVisibility(View.VISIBLE);
        tvCreateAiLabel.setVisibility(View.VISIBLE);

        // Animate child FABs
        fabCreateManual.setAlpha(0f);
        fabCreateManual.setScaleX(0.5f);
        fabCreateManual.setScaleY(0.5f);
        fabCreateManual.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        fabCreateAi.setAlpha(0f);
        fabCreateAi.setScaleX(0.5f);
        fabCreateAi.setScaleY(0.5f);
        fabCreateAi.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Animate labels
        tvCreateManualLabel.setAlpha(0f);
        tvCreateManualLabel.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        tvCreateAiLabel.setAlpha(0f);
        tvCreateAiLabel.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        fabMain.bringToFront();
        fabCreateManual.bringToFront();
        fabCreateAi.bringToFront();
        tvCreateManualLabel.bringToFront();
        tvCreateAiLabel.bringToFront();
    }

    private void collapseFab() {
        isFabExpanded = false;

        fabOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isFabExpanded) {
                            fabOverlay.setVisibility(View.GONE);
                        }
                        fabOverlay.setAlpha(1f);
                    }
                })
                .start();

        // Rotate main FAB back
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(fabMain, "rotation", 45f, 0f);
        rotateAnimator.setDuration(300);
        rotateAnimator.setInterpolator(new DecelerateInterpolator());
        rotateAnimator.start();

        // Hide child FABs and labels with animation
        fabCreateManual.animate()
                .alpha(0f)
                .scaleX(0.5f)
                .scaleY(0.5f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isFabExpanded) {
                            fabCreateManual.setVisibility(View.INVISIBLE);
                        }
                        fabCreateManual.setAlpha(1f);
                    }
                })
                .start();

        fabCreateAi.animate()
                .alpha(0f)
                .scaleX(0.5f)
                .scaleY(0.5f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isFabExpanded) {
                            fabCreateAi.setVisibility(View.INVISIBLE);
                        }
                        fabCreateAi.setAlpha(1f);
                    }
                })
                .start();

        // Animate labels
        tvCreateManualLabel.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isFabExpanded) {
                            tvCreateManualLabel.setVisibility(View.INVISIBLE);
                        }
                    }
                })
                .start();

        tvCreateAiLabel.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isFabExpanded) {
                            tvCreateAiLabel.setVisibility(View.INVISIBLE);
                        }
                    }
                })
                .start();
    }

    private void loadFlashcardDecks() {
        deckList.clear();

        if (deckList.isEmpty()) {
            // Add some sample decks for visualization purposes
            for (int i = 1; i <= 5; i++) {
                Folder deck = new Folder("Sample Deck " + i, "Desc");
                deck.setId(i);
                deckList.add(deck);
            }
        }

        if (deckList.isEmpty()) {
            recyclerFlashcardDecks.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerFlashcardDecks.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
    }
}