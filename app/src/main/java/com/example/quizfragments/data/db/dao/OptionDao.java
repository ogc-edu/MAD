package com.example.quizfragments.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.quizfragments.data.db.entities.Option;

import java.util.List;

public interface OptionDao {
    @Insert
    void insert(Option option);

    @Update
    void update(Option option);

    @Delete
    void delete(Option option);

    @Query("SELECT * FROM options")
    List<Option> getAllOptions();

    @Query("SELECT * FROM options WHERE optionId= :id")
    Option getQuizById(int id);
}
