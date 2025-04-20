package com.example.quizfragments.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quizfragments.data.db.entities.Folder;

import java.util.List;

@Dao
public interface FolderDao {
    
    @Insert
    void insert(Folder folder);
    
    @Update
    void update(Folder folder);
    
    @Delete
    void delete(Folder folder);
    
    @Query("SELECT * FROM folders ORDER BY title ASC")
    List<Folder> getAll();
    
    @Query("SELECT * FROM folders WHERE id = :id")
    Folder getFolderById(int id);
}
