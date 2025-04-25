package com.example.quizfragments.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.entities.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private final List<Note> allNotes = new ArrayList<>();
    private final List<Note> notes = new ArrayList<>();
    private final OnNoteClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
    private final String[] colors = {"#FF6B6B", "#4ECDC4", "#FFD166", "#6B5B95", "#88D8B0"};

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public NoteAdapter(OnNoteClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = notes.get(position);
        String title = currentNote.getTitle();
        holder.titleTextView.setText(title);
        holder.contentPreviewTextView.setText(currentNote.getContent());

        // Set the avatar with the first letter of the title
        if (title != null && !title.isEmpty()) {
            holder.avatarTextView.setText(String.valueOf(title.charAt(0)).toUpperCase());
        } else {
            holder.avatarTextView.setText("N"); // Default for "Note"
        }

        // Set category color based on position
        int colorIndex = position % colors.length;

        // Set avatar background drawable based on position
        int avatarColorIndex = position % 5;
        int[] avatarBackgrounds = {
            R.drawable.avatar_circle_blue,
            R.drawable.avatar_circle_red,
            R.drawable.avatar_circle_green,
            R.drawable.avatar_circle_yellow,
            R.drawable.avatar_circle_purple
        };
        holder.avatarTextView.setBackgroundResource(avatarBackgrounds[avatarColorIndex]);

        // Set the last edited date
        long lastEditedTime = currentNote.getUpdatedAt();
        String formattedDate = formatLastEditedDate(lastEditedTime);
        holder.lastEditedTextView.setText(
                holder.itemView.getContext().getString(R.string.last_edited_format, formattedDate));

        // Set a color for the category indicator
        holder.categoryIndicator.setBackgroundColor(Color.parseColor(colors[colorIndex]));

        // Set click listener for the arrow button
        holder.arrowButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(currentNote);
            }
        });
    }

    private String formatLastEditedDate(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        // If edited today
        if (diff < TimeUnit.DAYS.toMillis(1)) {
            return "Today";
        }
        // If edited yesterday
        else if (diff < TimeUnit.DAYS.toMillis(2)) {
            return "Yesterday";
        }
        // Otherwise show the date
        else {
            return dateFormat.format(new Date(timestamp));
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setNotes(List<Note> notes) {
        this.notes.clear();            // Clear the existing list
        this.notes.addAll(notes);      // Add all new notes
        this.allNotes.clear();         // Clear the backup list
        this.allNotes.addAll(notes);   // Add all new notes to backup
        notifyDataSetChanged();        // Notify adapter of changes
    }

    public void deleteNote(int position) {
        if (position >= 0 && position < notes.size()) {
            notes.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Note getNoteAt(int position) {
        if (position >= 0 && position < notes.size()) {
            return notes.get(position);
        }
        return null;
    }

    public void filter(String text) {
        notes.clear();
        if (text.isEmpty()) {
            notes.addAll(allNotes);
        } else {
            text = text.toLowerCase();
            for (Note note : allNotes) {
                // Handle potential null values
                String title = note.getTitle() != null ? note.getTitle().toLowerCase() : "";
                String content = note.getContent() != null ? note.getContent().toLowerCase() : "";

                if (title.contains(text) || content.contains(text)) {
                    notes.add(note);
                }
            }
        }
        notifyDataSetChanged();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView contentPreviewTextView;
        private final TextView lastEditedTextView;
        private final TextView avatarTextView;
        private final View categoryIndicator;
        private final ImageButton arrowButton;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.note_title);
            contentPreviewTextView = itemView.findViewById(R.id.note_content_preview);
            lastEditedTextView = itemView.findViewById(R.id.note_last_edited);
            avatarTextView = itemView.findViewById(R.id.note_avatar);
            categoryIndicator = itemView.findViewById(R.id.note_category_indicator);
            arrowButton = itemView.findViewById(R.id.note_arrow_button);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onNoteClick(notes.get(position));
                }
            });
        }
    }
}
