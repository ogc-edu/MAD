package com.example.quizfragments.data.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "questions",
        foreignKeys = @ForeignKey(
                entity = Quiz.class,
                parentColumns = "quizId",
                childColumns = "quizId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("quizId")}
)
public class Question {

    @PrimaryKey(autoGenerate = true)
    public int questionId;

    public int quizId;
    public String questionText;
}