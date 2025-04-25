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

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY position ASC")
    List<Flashcard> getFlashcardsByDeckId(int deckId);

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId")
    int getFlashcardCountByDeckId(int deckId);

    @Query("SELECT * FROM flashcards WHERE id = :id")
    Flashcard getFlashcardById(int id);

    @Query("DELETE FROM flashcards WHERE deckId = :deckId")
    void deleteFlashcardsByDeckId(int deckId);
}