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

    @Delete
    void delete(Question question);

    @Query("UPDATE questions SET attempted = :attempted WHERE questionId = :questionId")
    void updateAttempted(int questionId, int attempted);

    @Query("UPDATE questions SET userAnswer=:userAns WHERE questionId =:questionId")
    void updateUserAnswer(int questionId, int userAns);

    @Query("SELECT * FROM questions WHERE quizId = :quizId")        //select all questions when rendering question list page
    List<Question> getQuestionByQuizId(int quizId);

    @Query("SELECT * FROM questions")
    List<Question> getAllQuestions();

    @Query("SELECT * FROM questions WHERE questionId = :id")
    Question getQuestionById(int id);

    @Query("SELECT attempted FROM questions WHERE questionId = :id")
    int isAttempted(int id);

    @Query("SELECT * FROM questions WHERE quizId = :quizId AND questionNumber > :currentNumber ORDER BY questionNumber ASC LIMIT 1")
    Question getNextQuestion(int quizId, int currentNumber);

    @Query("DELETE FROM questions")
    void deleteAllQuestions();
}
