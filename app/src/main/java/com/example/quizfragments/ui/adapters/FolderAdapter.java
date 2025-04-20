package com.example.quizfragments.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizfragments.R;
import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.entities.Category;
import com.example.quizfragments.data.db.entities.Folder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private List<Folder> folders = new ArrayList<>();
    private OnFolderClickListener listener;
    private Map<Integer, Category> categoryMap = new HashMap<>();
    private AppDatabase db;

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
                notifyDataSetChanged();
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
        holder.titleTextView.setText(currentFolder.getTitle());
        holder.descriptionTextView.setText(currentFolder.getDescription());

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
                holder.categoryIndicator.setBackgroundColor(Color.LTGRAY);
            }
        } else {
            holder.categoryTextView.setVisibility(View.GONE);
            holder.categoryIndicator.setBackgroundColor(Color.LTGRAY);
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

    public void setDatabase(AppDatabase db) {
        this.db = db;
        loadCategories();
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView descriptionTextView;
        private TextView categoryTextView;
        private View categoryIndicator;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.folder_title);
            descriptionTextView = itemView.findViewById(R.id.folder_description);
            categoryTextView = itemView.findViewById(R.id.folder_category);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onFolderClick(folders.get(position));
                }
            });
        }
    }
}
