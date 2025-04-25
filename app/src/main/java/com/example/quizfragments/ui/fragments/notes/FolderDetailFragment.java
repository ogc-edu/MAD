package com.example.quizfragments.ui.fragments.notes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.PopupMenu;

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
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sort_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
        } else {
            return super.onOptionsItemSelected(item);
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
        FloatingActionButton fabAddNote = view.findViewById(R.id.fab_add_note);
        ImageButton sortButton = view.findViewById(R.id.sort_button);

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
        fabAddNote.setOnClickListener(v -> showCreateNoteDialog());

        //Set up sorting button
        sortButton.setOnClickListener(v -> showSortMenu(v));
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


    private void showCreateNoteDialog() {
        // Navigate to note detail fragment for creating a new note
        NoteDetailFragment noteDetailFragment = NoteDetailFragment.newInstance(folderId, 0);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, noteDetailFragment)
                .addToBackStack(null)
                .commit();
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
