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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.tasktimer.R;
import com.example.tasktimer.database.viewmodels.TaskViewModel;
import com.example.tasktimer.databinding.FragmentTimerBinding;
import com.example.tasktimer.model.Task;
import com.example.tasktimer.utils.FutureUtils;

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
        super.onPause();
        mainHandler.removeCallbacks(updateTimeRunnable);
    }

    @SuppressLint("CheckResult")
    private void getCurrentTask(){
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

        if(currentTask == null){
            timerProgress.setProgress(100);
            timerText.setText("");
        }else{
            long taskStartTime = currentTask.getStart().getTime();
            long taskEndTime = currentTask.getEnd().getTime();
            long currentTime = new Date().getTime();

            long timeDiff = 0;
            long timePassed = 0;

            if(currentTime >= taskStartTime && currentTime <= taskEndTime){
                timeDiff = taskEndTime - taskStartTime;
                timePassed = currentTime - taskStartTime;
            }else{
                if(nextTask != null){
                    timeDiff = nextTask.getStart().getTime() - taskEndTime;
                    timePassed = currentTime - taskEndTime;
                }
            }

            float progress = (float) (timePassed) / timeDiff;
            timerProgress.setProgress((int) (progress * 100));

            int timeLeftS = (int)((timeDiff - timePassed) / 1000);
            int minsLeft = Math.floorDiv(timeLeftS, 60);
            int secsLeft = timeLeftS % 60;

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

        if(currentTask == null){
            curTaskTitle.setText("Have a break!");
            curTaskTime.setText("");
        }else {
            if (currentTask.getEnd().getTime() >= new Date().getTime()) {
                curTaskTitle.setText(currentTask.getTaskName());
                String time = timeFormat.format(currentTask.getStart()) + " - " + timeFormat.format(currentTask.getEnd());
                curTaskTime.setText(time);
            } else if(nextTask != null) {
                curTaskTitle.setText("Have a break!");
                curTaskTime.setText("");
            } else {
                showNoTaskScreen();
            }
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
        }else{
            nextTaskLayout.setVisibility(View.INVISIBLE);
        }

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