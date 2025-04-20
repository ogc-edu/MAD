package com.example.quizfragments.data.db;

import com.example.quizfragments.data.db.dao.*;
import com.example.quizfragments.data.db.entities.*;
import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(
        entities = {Quiz.class, Question.class, Option.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase{
    public abstract QuizDao quizDao();
    public abstract OptionDao optionDao();
    public abstract QuestionDao questionDao();
}
