package com.example.tasktimer.model;

import java.time.OffsetTime;
import java.util.Date;

public class Task {
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

    public Date getStartTime() {
        return start;
    }

    public void setStartTime(Date start) {
        this.start = start;
    }

    public Date getEndTime() {
        return end;
    }

    public void setEndTime(Date end) {
        this.end = end;
    }
}
