package com.example.quizfragments.ui.viewmodels;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class QuizViewModelFactory implements ViewModelProvider.Factory {
    private Application application;

    public QuizViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(QuizViewModel.class)) {
            return (T) new QuizViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
