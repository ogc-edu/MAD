package com.example.quizfragments.ui.fragments.notes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Category;
import com.example.quizfragments.data.db.entities.Folder;
import com.example.quizfragments.ui.adapters.CategorySpinnerAdapter;
import com.example.quizfragments.ui.adapters.FolderAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class NotesFragment extends Fragment implements FolderAdapter.OnFolderClickListener {

    private RecyclerView foldersRecyclerView;
    private TextView emptyView;
    private FolderAdapter folderAdapter;
    private AppDatabase db;
    private List<Category> categories = new ArrayList<>();
    private Integer selectedCategoryId = null;
    private static final String PREFS_NAME = "NotesAppPrefs";
    private static final String PREF_SWIPE_TIP_SHOWN = "swipe_tip_shown";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.note_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize database
        db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

        // Initialize views
        foldersRecyclerView = view.findViewById(R.id.folders_recycler_view);
        emptyView = view.findViewById(R.id.empty_view);
        FloatingActionButton fabAddFolder = view.findViewById(R.id.fab_add_folder);

        // Setup RecyclerView
        foldersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        folderAdapter = new FolderAdapter(this);
        folderAdapter.setDatabase(db);
        foldersRecyclerView.setAdapter(folderAdapter);

        // Setup swipe to delete
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't want drag & drop
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final Folder folder = folderAdapter.getFolderAt(position);

                // Show confirmation dialog
                new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Folder")
                    .setMessage("Are you sure you want to delete this folder? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Delete folder from database
                        deleteFolder(folder, position);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Cancel deletion and restore the swiped item
                        folderAdapter.notifyItemChanged(position);
                    })
                    .setOnCancelListener(dialog -> {
                        // Also restore on dialog cancel
                        folderAdapter.notifyItemChanged(position);
                    })
                    .show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(foldersRecyclerView);

        // Load folders and categories
        loadFolders();
        loadCategories();

        // Setup FAB click listener
        fabAddFolder.setOnClickListener(v -> showCreateFolderDialog());

        // Show swipe tip dialog if it's the first time
        showSwipeTipIfNeeded();
    }

    private void loadCategories() {
        new Thread(() -> {
            try {
                // Get categories from database
                List<Category> dbCategories = db.categoryDao().getAll();

                // Create a new list with a default "Select category" item
                categories.clear();

                // Add a placeholder category as the first item
                Category placeholderCategory = new Category("Select category", "#FFFFFF");
                placeholderCategory.setId(-1); // Use -1 as a special ID for the placeholder
                categories.add(placeholderCategory);

                // Add all categories from the database
                if (dbCategories != null) {
                    categories.addAll(dbCategories);
                }

                // If no categories exist in the database, add some default ones
                if (dbCategories == null || dbCategories.isEmpty()) {
                    createDefaultCategories();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void createDefaultCategories() {
        try {
            // Create some default categories
            Category scienceCategory = new Category("Science", "#4CAF50");
            Category mathCategory = new Category("Math", "#2196F3");
            Category historyCategory = new Category("History", "#FFC107");
            Category literatureCategory = new Category("Literature", "#9C27B0");

            // Insert them into the database
            db.categoryDao().insert(scienceCategory);
            db.categoryDao().insert(mathCategory);
            db.categoryDao().insert(historyCategory);
            db.categoryDao().insert(literatureCategory);

            // Reload categories
            loadCategories();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFolders() {
        new Thread(() -> {
            try {
                List<Folder> folders = db.folderDao().getAll();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            folderAdapter.setFolders(folders);
                            if (folders.isEmpty()) {
                                foldersRecyclerView.setVisibility(View.GONE);
                                emptyView.setVisibility(View.VISIBLE);
                            } else {
                                foldersRecyclerView.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error loading folders: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }

    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_folder, null);
        builder.setView(dialogView);

        TextInputEditText titleInput = dialogView.findViewById(R.id.folder_title_input);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.folder_description_input);
        Spinner categorySpinner = dialogView.findViewById(R.id.category_spinner);

        // Setup category spinner
        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(requireContext(), categories);
        categorySpinner.setAdapter(categoryAdapter);

        // Set spinner selection listener
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category selectedCategory = (Category) parent.getItemAtPosition(position);
                if (selectedCategory != null && selectedCategory.getId() != -1) {
                    selectedCategoryId = selectedCategory.getId();
                } else {
                    selectedCategoryId = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = null;
            }
        });

        builder.setPositiveButton("Create", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create folder with selected category
            Folder newFolder = new Folder(title, description, selectedCategoryId);
            new Thread(() -> {
                try {
                    db.folderDao().insert(newFolder);
                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(this::loadFolders);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Error creating folder: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            }).start();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onFolderClick(Folder folder) {
        // Navigate to folder detail fragment
        FolderDetailFragment folderDetailFragment = FolderDetailFragment.newInstance(folder.getId());
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, folderDetailFragment)
                .addToBackStack(null)
                .commit();
    }

    public void onFolderDelete(Folder folder, int position) {
        // This method is called from the adapter when a folder is deleted
        // We don't need to implement it here since we're handling deletion in the swipe callback
    }

    private void showSwipeTipIfNeeded() {
        // Check if we've shown the tip before
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean tipShown = prefs.getBoolean(PREF_SWIPE_TIP_SHOWN, false);

        if (!tipShown) {
            // Show the tip dialog
            new AlertDialog.Builder(requireContext())
                .setTitle("Tip")
                .setMessage("You can delete folders and notes by swiping left.\n\nDeleting a folder will also delete all notes inside it. Please be careful.")
                .setPositiveButton("Got it", (dialog, which) -> {
                    // Mark the tip as shown
                    prefs.edit().putBoolean(PREF_SWIPE_TIP_SHOWN, true).apply();
                })
                .setNegativeButton("Don't show again", (dialog, which) -> {
                    // Mark the tip as shown
                    prefs.edit().putBoolean(PREF_SWIPE_TIP_SHOWN, true).apply();
                })
                .setCancelable(false)
                .show();
        }
    }

    private void deleteFolder(Folder folder, int position) {
        new Thread(() -> {
            try {
                // First delete all notes in this folder
                db.noteDao().deleteAllByFolderId(folder.getId());

                // Then delete the folder itself
                db.folderDao().delete(folder);

                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        // Remove from adapter
                        folderAdapter.deleteFolder(position);

                        // Show empty view if no folders left
                        if (folderAdapter.getItemCount() == 0) {
                            foldersRecyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }

                        Toast.makeText(requireContext(), "Folder deleted", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        // Restore the item in the adapter
                        folderAdapter.notifyItemChanged(position);
                        Toast.makeText(requireContext(), "Error deleting folder: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }
}
