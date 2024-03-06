package com.example.tasktimer.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = Task.TABLE_NAME)
public class Task {
    public static final String TABLE_NAME = "tasks";
    @PrimaryKey(autoGenerate = true)
    private int uid;

    private String taskName;
    private boolean completed;
    private Date start;
    private Date end;

    public Task(String taskName, Date start, Date end) {
        this.taskName = taskName;
        this.completed = false;
        this.start = start;
        this.end = end;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
}
