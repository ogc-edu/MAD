package com.example.quizfragments.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.quizfragments.data.db.entities.Quiz;


import java.util.List;

@Dao
public interface QuizDao {

    @Insert
    long insert(Quiz quiz);

    @Update
    void update(Quiz quiz);

    @Delete
    void delete(Quiz quiz);

    @Query("SELECT * FROM quizzes")
    List<Quiz> getAllQuizzes();

    @Query("SELECT * FROM quizzes WHERE quizId = :id")
    Quiz getQuizById(int id);

    @Query("DELETE FROM quizzes")
    void deleteAllQuizzes();

    @Query("SELECT questionCount FROM quizzes WHERE quizId = :quizId")
    int getQuizQuestionCount(int quizId);
    @Query("SELECT COUNT(questionId) FROM questions WHERE quizId = :quizId AND attempted = 1")
    int getNumberOfAttemptedQuestion(int quizId);
}
