package com.example.tasktimer.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tasktimer.model.Task;

import java.util.Date;
import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM " + Task.TABLE_NAME + " ORDER BY start")
    LiveData<List<Task>> getAll();

    @Query("SELECT * FROM " + Task.TABLE_NAME + " WHERE start BETWEEN :start AND :end ORDER BY start")
    LiveData<List<Task>> getTasksBetweenDate(Date start, Date end);

    @Query("SELECT * FROM " + Task.TABLE_NAME + " WHERE start < :date ORDER BY start")
    LiveData<List<Task>> getTasksBeforeDate(Date date);

    @Query("SELECT * FROM " + Task.TABLE_NAME + " WHERE start > :date ORDER BY start")
    LiveData<List<Task>> getTasksAfterDate(Date date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Task task);

    @Delete
    void delete(Task task);

    @Update
    void update(Task task);
}
