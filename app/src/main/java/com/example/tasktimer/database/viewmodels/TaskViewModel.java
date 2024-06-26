package com.example.tasktimer.database.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.tasktimer.database.AppRoomDatabase;
import com.example.tasktimer.database.daos.TaskDao;
import com.example.tasktimer.model.Task;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

// View model for accessing/modifying tasks
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

    // Gets all the tasks on the day of the given date
    public LiveData<List<Task>> getTasksByDate(Date date){
        Calendar c = Calendar.getInstance();

        // Start date of the day
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);

        Date startDate = c.getTime();

        // End date of the day
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);

        Date endDate = c.getTime();

        return taskDao.getTasksBetweenDate(startDate, endDate);
    }

    // Gets the current task (or previous if there is no task currently)
    // on the current day
    public LiveData<Task> getCurrentTask(Date date){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);

        Date endOfDay = c.getTime();

        return taskDao.getCurrentOrPrevTask(date, endOfDay);
    }

    // First task ever created
    public LiveData<Task> getFirstTask() {
        return taskDao.getFirstTask();
    }

    // Gets the number of completed tasks on a day
    public LiveData<Integer> getCompletedTaskCount(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);

        Date startOfDay = c.getTime();

        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);

        Date endOfDay = c.getTime();

        return taskDao.getNumCompletedTasks(startOfDay, endOfDay);
    }

    public LiveData<Task> getNextTask(Date date){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);

        Date endOfDay = c.getTime();

        return taskDao.getNextTask(date, endOfDay);
    }

    public LiveData<Integer> getProductiveHour(int hour){
        int hourOffset = TimeZone.getDefault().getOffset(new Date().getTime()) / 3600000;
        return taskDao.getProductiveHour(hour - hourOffset);
    }

    public LiveData<List<Task>> getTasksBeforeDate(Date date){
        Calendar c = Calendar.getInstance();

        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);

        return taskDao.getTasksBeforeDate(c.getTime());
    }

    public LiveData<List<Task>> getTasksAfterDate(Date date){
        Calendar c = Calendar.getInstance();

        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);

        return taskDao.getTasksAfterDate(c.getTime());
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
