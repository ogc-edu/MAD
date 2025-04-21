package com.example.quizfragments.ui.fragments.notes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Note;

public class NoteDetailFragment extends Fragment {

    private static final String ARG_FOLDER_ID = "folder_id";
    private static final String ARG_NOTE_ID = "note_id";

    private int folderId;
    private int noteId;
    private boolean isEditMode;
    private Note currentNote;

    private EditText titleEditText;
    private EditText contentEditText;
    private Button saveButton;
    private AppDatabase db;

    public static NoteDetailFragment newInstance(int folderId, int noteId) {
        NoteDetailFragment fragment = new NoteDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FOLDER_ID, folderId);
        args.putInt(ARG_NOTE_ID, noteId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            folderId = getArguments().getInt(ARG_FOLDER_ID);
            noteId = getArguments().getInt(ARG_NOTE_ID);
            isEditMode = noteId > 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize database
        db = DatabaseClient.getInstance(requireContext()).getAppDatabase();

        // Initialize views
        titleEditText = view.findViewById(R.id.note_title_edit);
        contentEditText = view.findViewById(R.id.note_content_edit);
        saveButton = view.findViewById(R.id.btn_save_note);

        // If in edit mode, load the note
        if (isEditMode) {
            loadNote();
        }

        // Setup save button click listener
        saveButton.setOnClickListener(v -> saveNote());
    }

    private void loadNote() {
        new Thread(() -> {
            try {
                currentNote = db.noteDao().getNoteById(noteId);
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (isAdded() && currentNote != null) {
                            titleEditText.setText(currentNote.getTitle());
                            contentEditText.setText(currentNote.getContent());
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error loading note: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        }).start();
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Title cannot be empty");
            return;
        }

        new Thread(() -> {
            try {
                if (isEditMode) {
                    // Update existing note
                    currentNote.setTitle(title);
                    currentNote.setContent(content);
                    currentNote.setUpdatedAt(System.currentTimeMillis());
                    db.noteDao().update(currentNote);
                } else {
                    // Create new note
                    Note newNote = new Note(folderId, title, content);
                    db.noteDao().insert(newNote);
                }

                if (isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Note saved", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
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
}
