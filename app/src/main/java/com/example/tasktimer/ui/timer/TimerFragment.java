package com.example.tasktimer.ui.timer;

import static com.example.tasktimer.utils.Constants.DATE_FORMAT;
import static com.example.tasktimer.utils.Constants.TIME_FORMAT;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.tasktimer.R;
import com.example.tasktimer.database.viewmodels.TaskViewModel;
import com.example.tasktimer.databinding.FragmentTimerBinding;
import com.example.tasktimer.model.Task;
import com.example.tasktimer.utils.FutureUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimerFragment extends Fragment {

    private FragmentTimerBinding binding;
    private TaskViewModel taskViewModel;

    private Task currentTask;
    private Task nextTask;

    private TextView curTaskTitle;
    private TextView curTaskTime;
    private ProgressBar timerProgress;
    private TextView timerText;


    private ConstraintLayout nextTaskLayout;
    private TextView nextTaskTitle;
    private TextView nextDateText;
    private TextView nextTimeText;
    private CheckBox nextCompletedCheckbox;
    private ImageButton nextDeleteButton;

    private ConstraintLayout noTasksLayout;
    private ConstraintLayout mainLayout;

    private Handler mainHandler;

    private Runnable updateTimeRunnable;
    private int pastMinute;

    private LiveData<Task> currentTaskLiveData;
    private LiveData<Task> nextTaskLiveData;

    private FloatingActionButton endTaskButton;
    private FloatingActionButton addTimeButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTimerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainHandler = new Handler(getContext().getMainLooper());

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        CheckBox checkBox = root.findViewById(R.id.checkBox);
        checkBox.setFocusable(false);
        checkBox.setClickable(false);

        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                mainHandler.postDelayed(this, 1000);
            }
        };

        mainLayout = root.findViewById(R.id.mainContentLayout);

        endTaskButton = root.findViewById(R.id.stopButton);
        addTimeButton = root.findViewById(R.id.addTimeButton);

        mainLayout.setVisibility(View.GONE);

        endTaskButton.setOnClickListener(view -> {
            if(currentTask != null){
                currentTask.setCompleted(true);
                currentTask.setEnd(new Date());
                taskViewModel.update(currentTask);
            }
        });

        addTimeButton.setOnClickListener(view -> {
            Calendar c = Calendar.getInstance();
            c.setTime(currentTask.getEnd());
            c.add(Calendar.MINUTE, 5);

            currentTask.setEnd(c.getTime());

            taskViewModel.update(currentTask);
        });

        curTaskTitle = root.findViewById(R.id.curTaskTitle);
        curTaskTime = root.findViewById(R.id.curTaskTime);
        timerText = root.findViewById(R.id.timerText);
        timerProgress = root.findViewById(R.id.timerProgress);

        noTasksLayout = root.findViewById(R.id.noTasksLayout);
        noTasksLayout.setVisibility(View.GONE);

        nextTaskLayout = root.findViewById(R.id.nextTaskLayout);
        nextTaskTitle = root.findViewById(R.id.taskTitle);
        nextDateText = root.findViewById(R.id.dateText);
        nextTimeText = root.findViewById(R.id.timeText);
        nextCompletedCheckbox = root.findViewById(R.id.checkBox);
        nextDeleteButton = root.findViewById(R.id.deleteButton);

        root.findViewById(R.id.deleteButton).setVisibility(View.GONE);

        getCurrentTask();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        getCurrentTask();
    }

    @Override
    public void onPause() {
        mainHandler.removeCallbacks(updateTimeRunnable);
        super.onPause();
    }

    @SuppressLint("CheckResult")
    private void getCurrentTask(){
        try{
            getViewLifecycleOwner();
        }catch(Exception e){
            return;
        }

        if(currentTaskLiveData != null)
            currentTaskLiveData.removeObservers(getViewLifecycleOwner());

        if(nextTaskLiveData != null)
            nextTaskLiveData.removeObservers(getViewLifecycleOwner());

        currentTaskLiveData = taskViewModel.getCurrentTask(new Date());
        nextTaskLiveData = taskViewModel.getNextTask(new Date());

        currentTaskLiveData.observe(getViewLifecycleOwner(), task -> {
            currentTask = task;
            if(task == null){
                mainHandler.post(() -> {
                    mainLayout.setVisibility(View.GONE);
                    noTasksLayout.setVisibility(View.VISIBLE);
                });
            }

            mainHandler.post(this::updateTasks);
        });

        nextTaskLiveData.observe(getViewLifecycleOwner(), task -> {
            nextTask = task;

            mainHandler.post(this::updateTasks);
        });
    }

    private void updateTime(){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int curMinute = c.get(Calendar.MINUTE);
        if(curMinute != pastMinute){
            pastMinute = curMinute;
            getCurrentTask();
        }

        if(currentTask == null && nextTask == null){
            return;
        }

        long timeDiff = 0;
        long timePassed = 0;
        long currentTime = new Date().getTime();

        if(currentTask == null){
            timerProgress.setProgress(100);
            timerText.setText("");

            timeDiff = nextTask.getStart().getTime() - currentTime;
        }else{
            long taskStartTime = currentTask.getStart().getTime();
            long taskEndTime = currentTask.getEnd().getTime();

            if(currentTime >= taskStartTime && currentTime <= taskEndTime){
                timeDiff = taskEndTime - taskStartTime;
                timePassed = currentTime - taskStartTime;
            }else{
                if(nextTask != null){
                    timeDiff = nextTask.getStart().getTime() - taskEndTime;
                    timePassed = currentTime - taskEndTime;
                }
            }
        }

        float progress = (float) (timePassed) / timeDiff;
        timerProgress.setProgress((int) (progress * 100));

        int timeLeftS = (int)((timeDiff - timePassed) / 1000);
        int hoursLeft = Math.floorDiv(timeLeftS, 3600);
        int minsLeft = Math.floorDiv(timeLeftS - (hoursLeft * 3600), 60);
        int secsLeft = timeLeftS % 60;

        if(hoursLeft > 0){
            timerText.setText(String.format("%2s:%2s:%2s", hoursLeft, minsLeft, secsLeft).replace(' ', '0'));
        }else{
            timerText.setText(String.format("%2s:%2s", minsLeft, secsLeft).replace(' ', '0'));
        }
    }

    private void updateTasks(){
        if(currentTask == null && nextTask == null){
            showNoTaskScreen();
            return;
        }

        showMainScreen();

        DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);

        boolean isBreak = false;

        long minsTillNext = 0;

        if(currentTask == null){
            isBreak = true;
        }else {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);

            minsTillNext = (c.getTime().getTime() - currentTask.getEnd().getTime()) / (1000 * 60);

            if (currentTask.getEnd().getTime() >= new Date().getTime()) {
                curTaskTitle.setText(currentTask.getTaskName());
                String time = timeFormat.format(currentTask.getStart()) + " - " + timeFormat.format(currentTask.getEnd());
                curTaskTime.setText(time);
                endTaskButton.setEnabled(true);
            } else {
                currentTask.setCompleted(true);
                taskViewModel.update(currentTask);
                endTaskButton.setEnabled(false);
                if(nextTask != null) {
                    isBreak = true;
                } else {
                    showNoTaskScreen();
                }
            }
        }

        if(isBreak){
            curTaskTitle.setText("Have a break!");
            curTaskTime.setText("");
            endTaskButton.setEnabled(false);
        }

        if(nextTask != null){
            nextTaskLayout.setVisibility(View.VISIBLE);

            DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

            nextTaskTitle.setText(nextTask.getTaskName());

            nextDateText.setText(dateFormat.format(nextTask.getStart()));
            String time = timeFormat.format(nextTask.getStart()) + " - " + timeFormat.format(nextTask.getEnd());
            nextTimeText.setText(time);
            nextCompletedCheckbox.setChecked(nextTask.isCompleted());
            nextDeleteButton.setOnClickListener(v -> {
                taskViewModel.delete(nextTask);
            });

            if(currentTask != null){
                minsTillNext = (nextTask.getStart().getTime() - currentTask.getEnd().getTime()) / (1000 * 60);
            }
        }else{
            nextTaskLayout.setVisibility(View.INVISIBLE);
        }

        addTimeButton.setEnabled(!isBreak && minsTillNext >= 5);

        updateTime();

        mainHandler.removeCallbacks(updateTimeRunnable);
        mainHandler.postDelayed(updateTimeRunnable, 1000);
    }

    private void showNoTaskScreen(){
        mainLayout.setVisibility(View.GONE);
        noTasksLayout.setVisibility(View.VISIBLE);
    }

    private void showMainScreen(){
        mainLayout.setVisibility(View.VISIBLE);
        noTasksLayout.setVisibility(View.GONE);
    }

    @Override 
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}