package com.example.quizfragments.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.quizfragments.data.db.entities.Category;

import java.util.List;

public class CategorySpinnerAdapter extends ArrayAdapter<Category> {
    
    private final LayoutInflater inflater;
    private final List<Category> categories;
    
    public CategorySpinnerAdapter(@NonNull Context context, List<Category> categories) {
        super(context, android.R.layout.simple_spinner_item, categories);
        this.inflater = LayoutInflater.from(context);
        this.categories = categories;
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
    
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView text = view.findViewById(android.R.id.text1);
        
        if (position == 0) {
            // First item is the hint
            text.setText("Select a category");
        } else {
            Category category = getItem(position);
            if (category != null) {
                text.setText(category.getName());
            }
        }
        
        return view;
    }
    
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView text = view.findViewById(android.R.id.text1);
        
        if (position == 0) {
            // First item is the hint
            text.setText("Select a category");
        } else {
            Category category = getItem(position);
            if (category != null) {
                text.setText(category.getName());
            }
        }
        
        return view;
    }
    
    @Override
    public int getCount() {
        return categories.size();
    }
    
    @Nullable
    @Override
    public Category getItem(int position) {
        return categories.get(position);
    }
}
