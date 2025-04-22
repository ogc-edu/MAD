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
    public Question(Question question) {
        this.questionId = question.questionId;
        this.quizId = question.quizId;
        this.attempted = question.attempted;
        this.questionText = question.questionText;
        this.userAnswer = question.userAnswer;
    }
    public Question() {
        // Needed by Room
    }

    @PrimaryKey(autoGenerate = true)
    public int questionId;

    public int quizId;

    public int attempted;       //0 is not attempted, 1 is attempted and correct, 2 is attempted but false
    public String questionText;
    public int questionNumber;
    public int userAnswer;  //store option id
}