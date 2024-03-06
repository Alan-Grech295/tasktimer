package com.example.tasktimer.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tasktimer.model.Task;

import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM " + Task.TABLE_NAME)
    LiveData<List<Task>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Task task);

    @Delete
    void delete(Task task);

    @Update
    void update(Task task);
}
