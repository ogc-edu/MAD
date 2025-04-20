package com.example.quizfragments.data.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

@Entity(
        tableName = "options",
        foreignKeys = @ForeignKey(
                entity = Question.class,
                parentColumns = "questionId",
                childColumns = "questionId",
                onDelete = ForeignKey.CASCADE
        )
)
public class Option {

    @PrimaryKey(autoGenerate = true)
    public int optionId;

    public int questionId;
    public String optionText;
    public boolean isCorrect;
}
