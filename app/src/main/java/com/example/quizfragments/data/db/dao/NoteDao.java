package com.example.quizfragments.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quizfragments.data.db.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    long insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes ORDER BY title ASC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY title ASC")
    List<Note> getNotesByFolderId(int folderId);

    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY title COLLATE NOCASE ASC")
    List<Note> getNotesSortedByTitle(int folderId);

    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY createdAt DESC")
    List<Note> getNotesSortedByDateCreated(int folderId);

    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY updatedAt DESC")
    List<Note> getNotesSortedByLastModified(int folderId);

    @Query("SELECT * FROM notes WHERE id = :id")
    Note getNoteById(int id);

    @Query("DELETE FROM notes WHERE folderId = :folderId")
    void deleteAllByFolderId(int folderId);
}
