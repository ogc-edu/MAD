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
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.entities.Category;
import com.example.quizfragments.data.db.entities.Folder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private List<Folder> folders = new ArrayList<>();
    private OnFolderClickListener listener;
    private Map<Integer, Category> categoryMap = new HashMap<>();
    private AppDatabase db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
    private String[] colors = {"#FF6B6B", "#4ECDC4", "#FFD166", "#6B5B95", "#88D8B0"};

    public interface OnFolderClickListener {
        void onFolderClick(Folder folder);
    }

    public FolderAdapter(OnFolderClickListener listener) {
        this.listener = listener;
    }

    private void loadCategories() {
        if (db == null) return;

        new Thread(() -> {
            try {
                List<Category> categories = db.categoryDao().getAll();
                categoryMap.clear();
                for (Category category : categories) {
                    categoryMap.put(category.getId(), category);
                }
                // Use main thread for UI updates
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> {
                    notifyDataSetChanged();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder currentFolder = folders.get(position);
        String title = currentFolder.getTitle();
        holder.titleTextView.setText(title);
        holder.descriptionTextView.setText(currentFolder.getDescription());

        // Set the avatar with the first letter of the title
        if (title != null && !title.isEmpty()) {
            holder.avatarTextView.setText(String.valueOf(title.charAt(0)).toUpperCase());
        } else {
            holder.avatarTextView.setText("F"); // Default for "Folder"
        }

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
        long lastEditedTime = currentFolder.getUpdatedAt();
        String formattedDate = formatLastEditedDate(lastEditedTime);
        holder.lastEditedTextView.setText("Last edited: " + formattedDate);

        // Set category information if available
        Integer categoryId = currentFolder.getCategoryId();
        if (categoryId != null && categoryMap.containsKey(categoryId)) {
            Category category = categoryMap.get(categoryId);
            holder.categoryTextView.setVisibility(View.VISIBLE);
            holder.categoryTextView.setText(category.getName());

            // Set category color indicator
            try {
                String colorHex = category.getColor();
                if (colorHex != null && !colorHex.isEmpty()) {
                    holder.categoryIndicator.setBackgroundColor(Color.parseColor(colorHex));
                }
            } catch (Exception e) {
                // Use default color if parsing fails
                int indicatorColorIndex = position % colors.length;
                holder.categoryIndicator.setBackgroundColor(Color.parseColor(colors[indicatorColorIndex]));
            }
        } else {
            holder.categoryTextView.setVisibility(View.GONE);
            int indicatorColorIndex = position % colors.length;
            holder.categoryIndicator.setBackgroundColor(Color.parseColor(colors[indicatorColorIndex]));
        }

        // Set click listener for the arrow button
        holder.arrowButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFolderClick(currentFolder);
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
        return folders.size();
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
        notifyDataSetChanged();
    }

    public void deleteFolder(int position) {
        if (position >= 0 && position < folders.size()) {
            folders.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Folder getFolderAt(int position) {
        if (position >= 0 && position < folders.size()) {
            return folders.get(position);
        }
        return null;
    }

    public void setDatabase(AppDatabase db) {
        this.db = db;
        loadCategories();
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView descriptionTextView;
        private TextView categoryTextView;
        private TextView lastEditedTextView;
        private TextView avatarTextView;
        private View categoryIndicator;
        private ImageButton arrowButton;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.folder_title);
            descriptionTextView = itemView.findViewById(R.id.folder_description);
            categoryTextView = itemView.findViewById(R.id.folder_category);
            lastEditedTextView = itemView.findViewById(R.id.folder_last_edited);
            avatarTextView = itemView.findViewById(R.id.folder_avatar);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
            arrowButton = itemView.findViewById(R.id.folder_arrow_button);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onFolderClick(folders.get(position));
                }
            });
        }
    }
}
