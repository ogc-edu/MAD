package com.example.quizfragments.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.quizfragments.data.db.entities.Quiz;
import com.example.quizfragments.data.db.repositories.QuizRepository;

import java.util.List;

//public class QuizViewModel extends AndroidViewModel {
//    private QuizRepository repository;
//    private LiveData<List<Quiz>> allQuizzes;
//
//    public QuizViewModel(@NonNull Application application) {
//        super(application);
//        repository = new QuizRepository(application);
//        allQuizzes = repository.getAllQuizzes();
//    }
//
//    public LiveData<List<Quiz>> getAllQuizzes() {
//        return allQuizzes;
//    }
//
//    public LiveData<List<Quiz>> searchQuizzes(String searchQuery) {
//        return repository.searchQuizzes(searchQuery);
//    }
//}
public class QuizViewModel extends AndroidViewModel {
    private QuizRepository quizRepository;
    private LiveData<List<Quiz>> allQuizzes;

    public QuizViewModel(@NonNull Application application) {
        super(application);
        quizRepository = new QuizRepository(application);
        allQuizzes = quizRepository.getAllQuizzes(); // LiveData from the repository
    }

    public LiveData<List<Quiz>> getAllQuizzes() {
        return allQuizzes;
    }

    public LiveData<List<Quiz>> searchQuizzes(String searchQuery) {
        return quizRepository.searchQuizzes(searchQuery);
    }
}


