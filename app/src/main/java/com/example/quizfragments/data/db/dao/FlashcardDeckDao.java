package com.example.quizfragments.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quizfragments.data.db.entities.FlashcardDeck;

import java.util.List;

@Dao
public interface FlashcardDeckDao {
    @Insert
    void insert(FlashcardDeck deck);

    @Update
    void update(FlashcardDeck deck);

    @Delete
    void delete(FlashcardDeck deck);

    @Query("SELECT * FROM flashcard_decks ORDER BY title ASC")
    List<FlashcardDeck> getAllDecks();

    @Query("SELECT * FROM flashcard_decks WHERE id = :id")
    FlashcardDeck getDeckById(int id);
}