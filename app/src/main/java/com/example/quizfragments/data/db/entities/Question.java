package com.example.quizfragments.data.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

@Entity(
        tableName = "questions",
        foreignKeys = @ForeignKey(
                entity = Quiz.class,
                parentColumns = "quizId",
                childColumns = "quizId",
                onDelete = ForeignKey.CASCADE
        )
)
public class Question {

    @PrimaryKey(autoGenerate = true)
    public int questionId;

    public int quizId;
    public String questionText;
}