package com.example.quizfragments.data.db;

import com.example.quizfragments.data.db.dao.*;
import com.example.quizfragments.data.db.entities.*;
import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(
        entities = {Quiz.class, Question.class, Option.class, Folder.class, Note.class, Category.class},
        version = 5,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase{
    public abstract QuizDao quizDao();
    public abstract OptionDao optionDao();
    public abstract QuestionDao questionDao();
    public abstract FolderDao folderDao();
    public abstract NoteDao noteDao();
    public abstract CategoryDao categoryDao();
}
