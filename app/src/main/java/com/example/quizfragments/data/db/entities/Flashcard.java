package com.example.quizfragments.data.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "flashcards",
        foreignKeys = @ForeignKey(
                entity = FlashcardDeck.class,
                parentColumns = "id",
                childColumns = "deckId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("deckId")})
public class Flashcard {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String question;
    private String answer;
    private final int deckId;
    private int position;

    public Flashcard(String question, String answer, int deckId) {
        this.question = question;
        this.answer = answer;
        this.deckId = deckId;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getDeckId() {
        return deckId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}