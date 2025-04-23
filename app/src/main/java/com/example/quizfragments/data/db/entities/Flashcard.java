package com.example.quizfragments.data.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "flashcards",
        foreignKeys = {
                @ForeignKey(
                        entity = Folder.class,
                        parentColumns = "id",
                        childColumns = "folderId",
                        onDelete = ForeignKey.CASCADE
                )
        })
public class Flashcard {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String question;
    private String answer;
    private int folderId;
    private long createdAt;
    private long updatedAt;
    private int position;

    public Flashcard(String question, String answer, int folderId) {
        this.question = question;
        this.answer = answer;
        this.folderId = folderId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.position = 0;
    }

    // Getters and Setters
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
        this.updatedAt = System.currentTimeMillis();
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}