package com.example.quizfragments.ui.fragments.quiz;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.example.quizfragments.data.db.entities.Quiz;
import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.ui.adapters.QuizAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.quizfragments.ui.adapters.QuizAdapter;

public class QuizListFragment extends Fragment implements QuizAdapter.OnQuizClickListener {

    private QuizAdapter quizAdapter;
    private List<Quiz> allQuizzes = new ArrayList<>();
    private AppDatabase db;
    private EditText searchEditText;
    private Button btnAll, btnScience, btnHistory;
    private RecyclerView recyclerView;
    private TextView tvEmptyQuizzes;

    // FAB variables
    private FloatingActionButton fabMain, fabCreateQuiz, fabGenerateAI;
    private TextView tvCreateQuizLabel, tvGenerateAILabel;
    private View fabOverlay;
    private boolean isFabMenuOpen = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_quizzes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        quizAdapter = new QuizAdapter(getContext(), this);
        recyclerView.setAdapter(quizAdapter);

        tvEmptyQuizzes = view.findViewById(R.id.tv_empty_quizzes);
        searchEditText = view.findViewById(R.id.edit_search);
        fabMain = view.findViewById(R.id.fab_main);
        fabCreateQuiz = view.findViewById(R.id.menu_new);
        fabGenerateAI = view.findViewById(R.id.menu_ai);
        tvCreateQuizLabel = view.findViewById(R.id.tv_new_quiz_label);
        tvGenerateAILabel = view.findViewById(R.id.tv_ai_gen_label);
        fabOverlay = view.findViewById(R.id.fab_overlay);

        setupSwipeToDelete();   //to delete quiz

        setupFabListeners();
        setupListeners();
        //setup db first, with instance and load quiz on setting up db
        db = DatabaseClient.getInstance(requireContext()).getAppDatabase();
        loadQuizzes();

        return view;
    }

    private void setupSwipeToDelete() { //deletion of quiz
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final ColorDrawable background = new ColorDrawable(Color.parseColor("#FF5252"));
            private final Drawable deleteIcon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete);
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't want drag & drop
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Quiz quizToDelete = quizAdapter.getQuizAt(position);

                // Show confirmation dialog
                showDeleteConfirmationDialog(position, quizToDelete);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                if (Math.abs(dX) < 0.1) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    return;
                }

                background.setBounds(
                        itemView.getLeft(),
                        itemView.getTop(),
                        itemView.getRight(),
                        itemView.getBottom());
                background.draw(c);
                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + iconMargin;
                int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();

                if (dX > 0) { // swipe to right
                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconRight = itemView.getLeft() + iconMargin + deleteIcon.getIntrinsicWidth();
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                } else { // left
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                }
                deleteIcon.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    private void showDeleteConfirmationDialog(int position, Quiz quiz) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Quiz");
        builder.setMessage("Are you sure you want to delete \"" + quiz.title + "\"?");

        builder.setPositiveButton("Delete", (dialog, id) -> {   //user deletes
            deleteQuiz(position, quiz);
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> {   //user cancel to delete
            quizAdapter.notifyItemChanged(position);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteQuiz(int position, Quiz deletedQuiz) {
        //clear from adapter
        quizAdapter.removeQuiz(position);
        //check if quiz list is empty
        updateEmptyState();
        //show snackbar to undo
        Snackbar.make(recyclerView, "Quiz deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> {
                    //add back the quiz if undo-ed
                    quizAdapter.addQuiz(position, deletedQuiz);
                    updateEmptyState();
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        //If snackbar is closed without undo, delete from database
                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                            deleteQuizFromDatabase(deletedQuiz);
                        }
                    }
                })
                .show();
    }

    private void deleteQuizFromDatabase(Quiz quiz) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            db.quizDao().delete(quiz);
            allQuizzes.remove(quiz); //update list to dynamically update UI
        });
        executor.shutdown();
    }

    private void updateEmptyState() {
        if (quizAdapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyQuizzes.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyQuizzes.setVisibility(View.GONE);
        }
    }

    private void setupFabListeners() {
        // Main FAB click listener
        fabMain.setOnClickListener(view -> {
            if (isFabMenuOpen) {
                closeFabMenu();
            } else {
                openFabMenu();
            }
        });

        fabCreateQuiz.setOnClickListener(view -> {
            closeFabMenu();
            navigateToCreateQuizFragment();
        });

        fabGenerateAI.setOnClickListener(view -> {
            closeFabMenu();
            navigateToQuizGeneratorFragment();
        });

        fabOverlay.setOnClickListener(view -> closeFabMenu());
    }

    private void openFabMenu() {
        isFabMenuOpen = true;
        fabOverlay.setVisibility(View.VISIBLE);
        fabCreateQuiz.setVisibility(View.VISIBLE);
        fabGenerateAI.setVisibility(View.VISIBLE);
        tvCreateQuizLabel.setVisibility(View.VISIBLE);
        tvGenerateAILabel.setVisibility(View.VISIBLE);
        fabMain.animate().rotation(45f).setDuration(300).start();
    }

    private void closeFabMenu() {
        isFabMenuOpen = false;
        fabOverlay.setVisibility(View.GONE);
        fabCreateQuiz.setVisibility(View.INVISIBLE);
        fabGenerateAI.setVisibility(View.INVISIBLE);
        tvCreateQuizLabel.setVisibility(View.INVISIBLE);
        tvGenerateAILabel.setVisibility(View.INVISIBLE);
        fabMain.animate().rotation(0f).setDuration(300).start();
    }

    private void navigateToCreateQuizFragment() {
        // Navigate to CreateQuizFragment
        CreateQuizFragment createQuizFragment = new CreateQuizFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, createQuizFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToQuizGeneratorFragment() {
        // Navigate to QuizGeneratorFragment
        QuizGeneratorFragment quizGeneratorFragment = new QuizGeneratorFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, quizGeneratorFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onQuizClick(Quiz quiz) {
        QuizQuestionsFragment questionsFragment = QuizQuestionsFragment.newInstance(quiz.quizId, quiz.title);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, questionsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupListeners() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterQuizzes(s.toString());
            }
        });
    }
    private void filterQuizzes(String query) {
        List<Quiz> filteredList = new ArrayList<>();

        for (Quiz quiz : allQuizzes) {
            if (quiz.title.toLowerCase().contains(query.toLowerCase()) ||
                    quiz.description.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(quiz);
            }
        }

        quizAdapter.setQuizzes(filteredList);
        updateEmptyState();
    }

    private void loadQuizzes() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Quiz> quizzes = db.quizDao().getAllQuizzes();

            if (quizzes.isEmpty()) {
                Log.d("empty", "no quiz found");
                for (Quiz quiz : quizzes) {
                    db.quizDao().insert(quiz);
                }
                quizzes = db.quizDao().getAllQuizzes();
            }

            allQuizzes = quizzes;
            if (getActivity() != null) {
                List<Quiz> finalQuizzes = quizzes;
                getActivity().runOnUiThread(() -> {
                    quizAdapter.setQuizzes(finalQuizzes);
                    updateEmptyState();
                });
            }
        });
        executor.shutdown();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFabMenuOpen) {
            closeFabMenu();
        }
    }
}