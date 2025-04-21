package com.example.quizfragments.data.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "options",
        foreignKeys = @ForeignKey(
                entity = Question.class,
                parentColumns = "questionId",
                childColumns = "questionId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("questionId")}
)
public class Option {

    @PrimaryKey(autoGenerate = true)
    public int optionId;

    public int questionId;
    public String optionText;
    public boolean isCorrect;
}
