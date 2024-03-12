package com.example.tasktimer.database.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.tasktimer.database.AppRoomDatabase;
import com.example.tasktimer.database.daos.TaskDao;
import com.example.tasktimer.model.Task;

import java.util.Calendar;
import java.util.Date;
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

    public LiveData<List<Task>> getTasksByDate(Date date){
        Calendar c = Calendar.getInstance();

        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);

        Date startDate = c.getTime();

        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);

        Date endDate = c.getTime();

        return taskDao.getTasksBetweenDate(startDate, endDate);
    }

    public LiveData<Task> getNextTask(Date date){
        return taskDao.getNextTask(date);
    }

    public LiveData<Task> getTaskAfter(Task task){
        return getNextTask(task.getEnd());
    }

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
