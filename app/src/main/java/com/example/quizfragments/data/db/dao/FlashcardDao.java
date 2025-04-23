package com.example.quizfragments.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quizfragments.data.db.entities.Flashcard;

import java.util.List;

@Dao
public interface FlashcardDao {

    @Insert
    void insert(Flashcard flashcard);

    @Update
    void update(Flashcard flashcard);

    @Delete
    void delete(Flashcard flashcard);

    @Query("SELECT * FROM flashcards ORDER BY position ASC")
    List<Flashcard> getAll();

    @Query("SELECT * FROM flashcards WHERE id = :id")
    Flashcard getFlashcardById(int id);

    @Query("SELECT * FROM flashcards WHERE folderId = :folderId ORDER BY position ASC")
    List<Flashcard> getFlashcardsByFolder(int folderId);

    @Query("SELECT COUNT(*) FROM flashcards WHERE folderId = :folderId")
    int getFlashcardsCountByFolder(int folderId);

    @Query("DELETE FROM flashcards WHERE folderId = :folderId")
    void deleteFlashcardsByFolder(int folderId);
}