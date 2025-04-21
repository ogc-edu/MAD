package com.example.quizfragments.data.db.repositories;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.quizfragments.data.db.AppDatabase;
import com.example.quizfragments.data.db.DatabaseClient;
import com.example.quizfragments.data.db.dao.QuizDao;
import com.example.quizfragments.data.db.entities.Quiz;

import java.util.List;

//public class QuizRepository {
//    private QuizDao quizDao;
//    private LiveData<List<Quiz>> allQuizzes;
//
//    public QuizRepository(Application application) {
//        quizDao = DatabaseClient.getInstance(application).getAppDatabase().quizDao();
//        allQuizzes = quizDao.getAllQuizzes();
//    }
//
//    public LiveData<List<Quiz>> getAllQuizzes() {
//        return allQuizzes;
//    }
//
//    public LiveData<List<Quiz>> searchQuizzes(String searchQuery) {
//        return quizDao.searchQuizzes(searchQuery);
//    }
//}

public class QuizRepository {
    private QuizDao quizDao;
    private LiveData<List<Quiz>> allQuizzes;

    public QuizRepository(Application application) {
        DatabaseClient db = DatabaseClient.getInstance(application);
        quizDao = db.getAppDatabase().quizDao();
        allQuizzes = quizDao.getAllQuizzes(); // Return LiveData from Dao
    }

    public LiveData<List<Quiz>> getAllQuizzes() {
        return allQuizzes;
    }

    public LiveData<List<Quiz>> searchQuizzes(String searchQuery) {
        return quizDao.searchQuizzes(searchQuery);
    }
}
