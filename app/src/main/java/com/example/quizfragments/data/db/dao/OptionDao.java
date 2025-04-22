package com.example.quizfragments.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.quizfragments.data.db.entities.Option;

import java.util.List;
@Dao
public interface OptionDao {
    @Insert
    long insert(Option option);  // Returns the ID of the inserted row

    @Update
    void update(Option option);

    @Delete
    void delete(Option option);

    @Query("SELECT * FROM options")
    List<Option> getAllOptions();

    @Query("SELECT * FROM options WHERE questionId = :questionId")      //get all options of a question
    List<Option> getOptionsByQuestion(int questionId);

    @Query("SELECT * FROM options WHERE optionId = :id")    //get options by id, in question list page
    Option getOptionById(int id);

    @Query("SELECT optionText FROM options WHERE questionId= :ques_id AND isCorrect = 1") //in question list page also
    String getCorrectAnswerForQuestion(int ques_id);

    @Query("DELETE FROM options")
    void deleteAllOptions();
}
