package com.example.quizfragments.ui.fragments.notes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

        // Setup RecyclerView
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        noteAdapter = new NoteAdapter(this);
        notesRecyclerView.setAdapter(noteAdapter);

        // Load folder and notes
        loadFolder();
        loadNotes();

        // Setup FAB click listener
        fabAddNote.setOnClickListener(v -> showCreateNoteDialog());
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

    private void loadNotes() {
        new Thread(() -> {
            try {
                List<Note> notes = db.noteDao().getNotesByFolderId(folderId);
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            noteAdapter.setNotes(notes);
                            if (notes.isEmpty()) {
                                notesRecyclerView.setVisibility(View.GONE);
                                emptyNotesView.setVisibility(View.VISIBLE);
                            } else {
                                notesRecyclerView.setVisibility(View.VISIBLE);
                                emptyNotesView.setVisibility(View.GONE);
                            }
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
}
