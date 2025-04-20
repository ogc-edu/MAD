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

    @Query("SELECT * FROM options WHERE optionId = :id")
    Option getOptionById(int id);

    @Query("DELETE FROM options")
    void deleteAllOptions();
}
