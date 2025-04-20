package com.example.quizfragments.data.db.dao;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.quizfragments.data.db.entities.Question;
import java.util.List;

@Dao
public interface QuestionDao {

    @Insert
    long insert(Question question);

    @Update
    void update(Question question);

    @Delete
    void delete(Question question);

    @Query("SELECT * FROM questions")
    List<Question> getAllQuestions();

    @Query("SELECT * FROM questions WHERE questionId = :id")
    Question getQuestionById(int id);
}
