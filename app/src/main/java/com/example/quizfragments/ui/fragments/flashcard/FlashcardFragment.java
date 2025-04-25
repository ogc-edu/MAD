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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.ui.adapters.FlashcardDeckAdapter;
import com.example.quizfragments.data.db.entities.FlashcardDeck;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
    private List<FlashcardDeck> deckList = new ArrayList<>();
    private FlashcardDeckAdapter adapter;
    private final Executor executor = Executors.newSingleThreadExecutor();

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load decks from database every time fragment resumes
        loadFlashcardDecks();
    }

    private void setupRecyclerView() {
        adapter = new FlashcardDeckAdapter(deckList, deck -> {
            // Handle deck click - navigate to flashcard tutorial/study
            navigateToFlashcardStudy(deck.getId());
        });

        recyclerFlashcardDecks.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerFlashcardDecks.setAdapter(adapter);
    }

    private void navigateToFlashcardStudy(int deckId) {
        // Create instance of FlashcardStudyFragment with the deck ID
        FlashcardStudyFragment studyFragment = FlashcardStudyFragment.newInstance(deckId);

        // Navigate to study fragment with deck ID
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, studyFragment);
        transaction.addToBackStack(null);
        transaction.commit();
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
            navigateToCreateFlashcardFragment();
        });

        fabCreateAi.setOnClickListener(v -> {
            collapseFab();
            navigateToAiGeneratorFragment();
        });

        fabOverlay.setOnClickListener(v -> collapseFab());
    }

    private void navigateToCreateFlashcardFragment() {
        // Navigate to CreateFlashcardFragment
        Fragment createFlashcardFragment = new CreateFlashcardFragment();

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, createFlashcardFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void navigateToAiGeneratorFragment() {
        showToast("AI Flashcard Generator - Coming soon!");
        // Uncomment when you have created the AiGeneratorFragment
        /*
        Fragment aiGeneratorFragment = new AiGeneratorFragment();

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, aiGeneratorFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        */
    }

    private void expandFab() {
        // [FAB expansion animation code remains the same]
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
        // [FAB collapse animation code remains the same]
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
        // Show loading indicator if needed
        // loadingIndicator.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            // Get database instance
            AppDatabase db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

            // Get all flashcard decks from database
            List<FlashcardDeck> decks = db.flashcardDeckDao().getAllDecks();

            // Update UI on main thread
            requireActivity().runOnUiThread(() -> {
                deckList.clear();

                // Add all decks to the list
                if (decks != null && !decks.isEmpty()) {
                    deckList.addAll(decks);
                } else {
                    // If no decks in database, show sample data for demonstration
                    boolean showSampleData = false; // Set to false in production

                    if (showSampleData) {
                        for (int i = 1; i <= 3; i++) {
                            FlashcardDeck deck = new FlashcardDeck("Sample Deck " + i, "Sample description for deck " + i);
                            deck.setId(i);
                            deckList.add(deck);
                        }
                    }
                }

                // Update visibility based on decks available
                if (deckList.isEmpty()) {
                    recyclerFlashcardDecks.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerFlashcardDecks.setVisibility(View.VISIBLE);
                    tvEmptyState.setVisibility(View.GONE);
                }

                // Notify adapter of data change
                adapter.notifyDataSetChanged();

                // Hide loading indicator if implemented
                // loadingIndicator.setVisibility(View.GONE);
            });
        });
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
    }
}