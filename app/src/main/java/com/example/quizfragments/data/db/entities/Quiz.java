package com.example.quizfragments.data.db.entities;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quizzes")
public class Quiz {

    @PrimaryKey(autoGenerate = true)
    public int quizId;

    public String title;
    public String description;

    public String toString() {
        return "Q ID: " + quizId + "\n" + "\nQuestion: " + title;
    }
}