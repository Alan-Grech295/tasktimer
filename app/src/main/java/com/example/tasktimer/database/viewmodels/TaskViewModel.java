package com.example.tasktimer.database.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.tasktimer.database.AppRoomDatabase;
import com.example.tasktimer.database.daos.TaskDao;
import com.example.tasktimer.model.Task;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private TaskDao taskDao;

    private final LiveData<List<Task>> taskLiveData;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        AppRoomDatabase database = AppRoomDatabase.getDatabase(application);
        taskDao = database.taskDao();
        taskLiveData = taskDao.getAll();
    }

    public LiveData<List<Task>> getAllTasks() { return taskLiveData; }

    public void insert(Task task){
        AppRoomDatabase.databaseWriteExecutor.execute(() -> {
            taskDao.insert(task);
        });
    }

    public void update(Task task){
        AppRoomDatabase.databaseWriteExecutor.execute(() -> {
            taskDao.update(task);
        });
    }

    public void delete(Task task){
        AppRoomDatabase.databaseWriteExecutor.execute(() -> {
            taskDao.delete(task);
        });
    }
}
