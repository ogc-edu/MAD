package com.example.quizfragments.data.db;

import com.example.quizfragments.data.db.dao.*;
import com.example.quizfragments.data.db.entities.*;
import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(
        entities = {Quiz.class, Question.class, Option.class, Folder.class, Note.class, Category.class, Flashcard.class, FlashcardDeck.class},
        version = 9,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase{
    public abstract QuizDao quizDao();
    public abstract OptionDao optionDao();
    public abstract QuestionDao questionDao();
    public abstract FolderDao folderDao();
    public abstract NoteDao noteDao();
    public abstract CategoryDao categoryDao();
    public abstract FlashcardDao flashcardDao();
    public abstract FlashcardDeckDao flashcardDeckDao();

}
