package com.example.quizfragments.ui.fragments.notes;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Folder;
import com.example.quizfragments.data.db.entities.Note;
import com.example.quizfragments.ui.adapters.NoteAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.quizfragments.data.api.AI;

import java.util.List;

public class FolderDetailFragment extends Fragment implements NoteAdapter.OnNoteClickListener {

    private static final String ARG_FOLDER_ID = "folder_id";

    private int folderId;
    private Folder currentFolder;
    private RecyclerView notesRecyclerView;
    private TextView emptyNotesView;
    private TextView folderTitleHeader;
    private NoteAdapter noteAdapter;
    private AppDatabase db;
    private EditText searchBox;
    private List<Note> allNotes = new ArrayList<>();
    private boolean isFabRotated = false;
    private FloatingActionButton fabAddNote;

    public static FolderDetailFragment newInstance(int folderId) {
        FolderDetailFragment fragment = new FolderDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FOLDER_ID, folderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            folderId = getArguments().getInt(ARG_FOLDER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_folder_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize database
        db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

        // Initialize views
        notesRecyclerView = view.findViewById(R.id.notes_recycler_view);
        emptyNotesView = view.findViewById(R.id.empty_notes_view);
        folderTitleHeader = view.findViewById(R.id.folder_title_header);
        fabAddNote = view.findViewById(R.id.fab_add_note);
        ImageButton sortButton = view.findViewById(R.id.sort_button);
        ImageButton searchButton = view.findViewById(R.id.search_button);
        searchBox = view.findViewById(R.id.search_box);

        // Setup RecyclerView
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        noteAdapter = new NoteAdapter(this);
        notesRecyclerView.setAdapter(noteAdapter);

        // Setup swipe to delete
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't want drag & drop
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final Note note = noteAdapter.getNoteAt(position);

                // Show confirmation dialog
                new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete this note? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Delete note from database
                        deleteNote(note, position);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Cancel deletion and restore the swiped item
                        noteAdapter.notifyItemChanged(position);
                    })
                    .setOnCancelListener(dialog -> {
                        // Also restore on dialog cancel
                        noteAdapter.notifyItemChanged(position);
                    })
                    .show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(notesRecyclerView);

        // Load folder and notes
        loadFolder();
        loadNotesSorted("lastModified");

        // Setup FAB click listener
        fabAddNote.setOnClickListener(v -> {
            rotateFab();
            showNoteCreationOptions();
        });

        //Set up sorting button
        sortButton.setOnClickListener(this::showSortMenu);

        // Toggle search box visibility when search button is clicked
        searchButton.setOnClickListener(v -> {
            if (searchBox.getVisibility() == View.GONE) {
                searchBox.setVisibility(View.VISIBLE);
                searchBox.requestFocus();
            } else {
                searchBox.setVisibility(View.GONE);
                searchBox.setText(""); // Clear search when hiding
                noteAdapter.filter(""); // Reset filter
            }
        });

        // Perform filtering when user types
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.filter(s.toString());
                updateEmptyViewVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    //Animates the FAB with a rotation effect
    private void rotateFab() {
        ObjectAnimator rotate = ObjectAnimator.ofFloat(fabAddNote, "rotation",
                isFabRotated ? 0f : 135f);
        rotate.setDuration(200);
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());
        rotate.start();
        isFabRotated = !isFabRotated;
    }


    //Shows a dialog with options for note creation: manually or using AI
    private void showNoteCreationOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Create Note");

        // Inflate a custom view for the dialog
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_note_options, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        // Set up click listeners for the options
        view.findViewById(R.id.btn_create_manual).setOnClickListener(v -> {
            dialog.dismiss();
            createNoteManually();
        });

        view.findViewById(R.id.btn_create_ai).setOnClickListener(v -> {
            dialog.dismiss();
            showAINoteCreationDialog();
        });

        dialog.show();
    }

    //Navigate to note detail fragment for creating a new note manually

    private void createNoteManually() {
        NoteDetailFragment noteDetailFragment = NoteDetailFragment.newInstance(folderId, 0);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, noteDetailFragment)
                .addToBackStack(null)
                .commit();
    }

    //Shows a dialog to prompt the user for an AI-generated note topic
    private void showAINoteCreationDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_ai_note, null);
        EditText topicInput = dialogView.findViewById(R.id.et_note_topic);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Create Note with AI")
                .setView(dialogView)
                .setPositiveButton("Generate", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String topic = topicInput.getText().toString().trim();
                if (topic.isEmpty()) {
                    topicInput.setError("Please enter a topic");
                } else {
                    dialog.dismiss();
                    generateAINote(topic);
                }
            });
        });

        dialog.show();
    }

    // Generates a note using AI based on the user's topic input
    // The AI will generate both the title and content
    private void generateAINote(String topic) {
        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Generating Note")
                .setMessage("Creating your note with AI...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // Create a prompt for the AI
        String prompt = "Create a comprehensive educational note about \"" + topic + "\".\n\n" +
                "Your response should be structured exactly like this:\n\n" +
                "TITLE: [Concise and engaging title - maximum 5 words]\n\n" +
                "Introduction: A short paragraph explaining the topic.\n" +
                "*****************************************\n\n" +
                "Main Sections:\n" +
                "- Create 3 to 7 main sections.\n" +
                "- For each section:\n" +
                "  - Start with ONLY a numbered subtitle like:\n" +
                "    1. Cell Membrane\n" +
                "  (no colon, no bold, no description after the subtitle)\n" +
                "  - After the subtitle, no spacing, list 2 to 4 bullet points (-) with key facts.\n\n" +
                "*****************************************\n\n" +
                "Conclusion: A short summary paragraph.\n\n" +
                "Important rules:\n" +
                "- Begin with 'TITLE:' followed by a concise title (max 5 words) that is not the same as the full topic.\n" +
                "- Do NOT add a colon ':' after the subtitles.\n" +
                "- Do NOT bold the subtitles.\n" +
                "- Do NOT write explanation under the subtitle.\n" +
                "- Bullet points must be simple and factual.\n" +
                "- Use simple, clear language.";

        // Call the AI API
        AI.modelCall(prompt, new AI.ResponseCallback() {
            @Override
            public void onResponse(String result) {
                loadingDialog.dismiss();
                parseAndCreateNote(result);
            }

            @Override
            public void onError(String error) {
                loadingDialog.dismiss();
                Toast.makeText(requireContext(), "Error generating note: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Parse the AI response to extract the title and content
    private void parseAndCreateNote(String aiResponse) {
        String title;
        String content;

        // Extract the title from the AI response
        if (aiResponse.startsWith("TITLE:")) {
            int titleEndIndex = aiResponse.indexOf("\n\n");
            if (titleEndIndex > 0) {
                title = aiResponse.substring("TITLE:".length(), titleEndIndex).trim();
                content = aiResponse.substring(titleEndIndex + 2).trim();
            } else {
                // Fallback if format is not as expected
                title = "Note " + System.currentTimeMillis();
                content = aiResponse;
            }
        } else {
            // Fallback if the AI doesn't follow the format
            title = "Note " + System.currentTimeMillis();
            content = aiResponse;
        }

        // Ensure title isn't too long
        if (title.length() > 50) {
            title = title.substring(0, 47) + "...";
        }

        // Create the note with the extracted title and content
        createNoteFromAIResponse(title, content);
    }

    //Creates a new note from the AI-generated content
    private void createNoteFromAIResponse(String title, String content) {
        // Create and save the note to the database
        new Thread(() -> {
            try {
                // Create new note
                Note newNote = new Note(folderId, title, content);
                long noteId = db.noteDao().insert(newNote);

                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "AI note created", Toast.LENGTH_SHORT).show();

                            // Navigate to the newly created note
                            NoteDetailFragment noteDetailFragment = NoteDetailFragment.newInstance(
                                    folderId, (int) noteId);
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, noteDetailFragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error saving note: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }

    private void updateEmptyViewVisibility() {
        if (noteAdapter.getItemCount() == 0) {
            notesRecyclerView.setVisibility(View.GONE);
            emptyNotesView.setVisibility(View.VISIBLE);
        } else {
            notesRecyclerView.setVisibility(View.VISIBLE);
            emptyNotesView.setVisibility(View.GONE);
        }
    }

    private void showSortMenu(View view) {
        // Create the PopupMenu
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_sort_options, popupMenu.getMenu());

        // Handle menu item click
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.sort_by_title) {
                loadNotesSorted("title");
                return true;
            } else if (itemId == R.id.sort_by_date_created) {
                loadNotesSorted("dateCreated");
                return true;
            } else if (itemId == R.id.sort_by_last_modified) {
                loadNotesSorted("lastModified");
                return true;
            }
            return false;
        });

        // Show the menu
        popupMenu.show();
    }

    private void loadFolder() {
        new Thread(() -> {
            try {
                currentFolder = db.folderDao().getFolderById(folderId);
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (isAdded() && currentFolder != null) {
                            folderTitleHeader.setText(currentFolder.getTitle());
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error loading folder: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }

    private void loadNotesSorted(String sortBy) {
        new Thread(() -> {
            try {
                List<Note> notes;
                switch (sortBy) {
                    case "title":
                        notes = db.noteDao().getNotesSortedByTitle(folderId);
                        break;
                    case "dateCreated":
                        notes = db.noteDao().getNotesSortedByDateCreated(folderId);
                        break;
                    case "lastModified":
                        notes = db.noteDao().getNotesSortedByLastModified(folderId);
                        break;
                    default:
                        notes = db.noteDao().getNotesByFolderId(folderId);
                }

                List<Note> finalNotes = notes;
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        noteAdapter.setNotes(finalNotes);
                        if (finalNotes.isEmpty()) {
                            notesRecyclerView.setVisibility(View.GONE);
                            emptyNotesView.setVisibility(View.VISIBLE);
                        } else {
                            notesRecyclerView.setVisibility(View.VISIBLE);
                            emptyNotesView.setVisibility(View.GONE);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error loading notes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }

    @Override
    public void onNoteClick(Note note) {
        // Navigate to note detail fragment for editing an existing note
        NoteDetailFragment noteDetailFragment = NoteDetailFragment.newInstance(folderId, note.getId());
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, noteDetailFragment)
                .addToBackStack(null)
                .commit();
    }

    public void onNoteDelete(Note note, int position) {
        // This method is called from the adapter when a note is deleted
        // We don't need to implement it here since we're handling deletion in the swipe callback
    }

    private void deleteNote(Note note, int position) {
        new Thread(() -> {
            try {
                // Delete the note from the database
                db.noteDao().delete(note);

                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        // Remove from adapter
                        noteAdapter.deleteNote(position);

                        // Show empty view if no notes left
                        if (noteAdapter.getItemCount() == 0) {
                            notesRecyclerView.setVisibility(View.GONE);
                            emptyNotesView.setVisibility(View.VISIBLE);
                        }

                        Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        // Restore the item in the adapter
                        noteAdapter.notifyItemChanged(position);
                        Toast.makeText(requireContext(), "Error deleting note: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }
}
