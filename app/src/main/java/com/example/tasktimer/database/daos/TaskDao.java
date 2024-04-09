package com.example.tasktimer.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tasktimer.model.Task;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM " + Task.TABLE_NAME + " ORDER BY start")
    LiveData<List<Task>> getAll();

    @Query("SELECT * FROM " + Task.TABLE_NAME + " WHERE start BETWEEN :start AND :end ORDER BY start")
    LiveData<List<Task>> getTasksBetweenDate(Date start, Date end);

    @Query("SELECT * FROM " + Task.TABLE_NAME + " WHERE NOT completed AND start > :date AND `end` <= :endOfDay ORDER BY start LIMIT 1")
    LiveData<Task> getNextTask(Date date, Date endOfDay);

    @Query("SELECT * FROM " + Task.TABLE_NAME + " WHERE `end` <= :date ORDER BY `end` LIMIT 1")
    ListenableFuture<Task> getPreviousTask(Date date);

    @Query("SELECT COUNT(*) FROM " + Task.TABLE_NAME + " WHERE completed AND :hour BETWEEN (CAST(start / 3600000 AS INT) % 24) AND (CAST(`end` / 3600000 AS INT) % 24)")
    LiveData<Integer> getProductiveHour(int hour);

    @Query("SELECT * FROM " + Task.TABLE_NAME + " WHERE NOT completed AND `start` <= :date AND `end` <= :endOfDay ORDER BY `start` DESC LIMIT 1")
    LiveData<Task> getCurrentOrPrevTask(Date date, Date endOfDay);

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
