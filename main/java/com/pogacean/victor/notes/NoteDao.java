package com.pogacean.victor.notes;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert
    void insert(Note note);
    @Update
    void update(Note note);
    @Delete
    void delete(Note note);
    @Query("SELECT * FROM note")
    List<Note> getNotes();
    @Query("SELECT * FROM note WHERE position == :position")
    Note getFromPosition(int position);
    @Query("Update note SET position = position - 1 WHERE position > :position")
    void decreasePositions(int position);
    @Query("Update note SET position = position + 1 WHERE position > :position")
    void increasePositions(int position);
    @Query("Update note SET hidden = 0")
    void showAll();
    @Query("Update note SET hidden = CASE WHEN title LIKE :searchString OR body LIKE :searchString then 0 else 1 END")
    void search(String searchString);
    @Query("SELECT * FROM note WHERE hidden LIKE 1")
    List<Note> getHidden();
    @Query("Update note SET hidden = 1 WHERE position == :position")
    void setHidden(int position);
}
