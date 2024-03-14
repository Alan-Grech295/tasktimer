package com.example.tasktimer.ui;

import static com.example.tasktimer.utils.Constants.DATE_FORMAT;
import static com.example.tasktimer.utils.Constants.TIME_FORMAT;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.tasktimer.MainActivity;
import com.example.tasktimer.R;
import com.example.tasktimer.database.viewmodels.TaskViewModel;
import com.example.tasktimer.model.Task;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class AddTaskPopup extends DialogFragment {

    AtomicReference<Date> taskDate = new AtomicReference<>(null);
    AtomicReference<Pair<Integer, Integer>> taskStartTime = new AtomicReference<>(null);
    AtomicReference<Pair<Integer, Integer>> taskEndTime = new AtomicReference<>(null);

    EditText taskTitleText;
    EditText dateText;
    EditText startTimeText;
    EditText endTimeText;

    public AddTaskPopup() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task_popup, container, false);
        taskTitleText = view.findViewById(R.id.taskNameEditText);
        dateText = view.findViewById(R.id.dateEditText);
        startTimeText = view.findViewById(R.id.startTimeEditText);
        endTimeText = view.findViewById(R.id.endTimeEditText);

        Button cancelButton = view.findViewById(R.id.cancelButton);
        Button addButton = view.findViewById(R.id.addTaskButton);

        cancelButton.setOnClickListener(v -> {
            dismiss();
        });

        addButton.setOnClickListener(this::addGoal);

        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        SimpleDateFormat timeFormatter = new SimpleDateFormat(TIME_FORMAT);

        dateText.setOnClickListener(v -> {
            Date curDate = taskDate.get() == null ? new Date() : taskDate.get();

            Calendar c = Calendar.getInstance();
            c.setTime(curDate);
            CalendarConstraints constraints = new CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now())
                    .build();

            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Task Date")
                    .setCalendarConstraints(constraints)
                    .setSelection(c.getTimeInMillis())
                    .build();

            picker.show(getParentFragmentManager(), null);

            picker.addOnPositiveButtonClickListener(o -> {
                taskDate.set(new Date(o));
                dateText.setText(formatter.format(taskDate.get()));
                dateText.setError(null);
            });
        });

        startTimeText.setOnClickListener(v -> {
            Pair<Integer, Integer> curTime = taskStartTime.get() == null ? dateToTime(new Date()) : taskStartTime.get();

            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(curTime.first)
                    .setMinute(curTime.second)
                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                    .setTitleText("Select Task Start time")
                    .build();

            picker.show(getParentFragmentManager(), null);

            picker.addOnPositiveButtonClickListener(o -> {
                int minute = picker.getMinute();
                int hour = picker.getHour();

                taskStartTime.set(new Pair<>(hour, minute));

                String minText = String.format("%2s", minute).replace(' ', '0');
                String hourText = String.format("%2s", hour).replace(' ', '0');

                startTimeText.setText(hourText + ":" + minText);
                startTimeText.setError(null);
            });
        });

        endTimeText.setOnClickListener(v -> {
            Pair<Integer, Integer> curTime = taskEndTime.get() == null ? dateToTime(new Date()) : taskEndTime.get();

            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(curTime.first)
                    .setMinute(curTime.second)
                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                    .setTitleText("Select Task End time")
                    .build();

            picker.show(getParentFragmentManager(), null);

            picker.addOnPositiveButtonClickListener(o -> {
                int minute = picker.getMinute();
                int hour = picker.getHour();

                taskEndTime.set(new Pair<>(hour, minute));

                String minText = String.format("%2s", minute).replace(' ', '0');
                String hourText = String.format("%2s", hour).replace(' ', '0');

                endTimeText.setText(hourText + ":" + minText);
                endTimeText.setError(null);
            });
        });


        // Inflate the layout for this fragment
        return view;
    }

    private Pair<Integer, Integer> dateToTime(Date date){
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        return new Pair<>(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    private void addGoal(View view){
        boolean foundError = false;
        //Should use isBlank
        if (taskTitleText.getText().toString().trim().isEmpty()) {
            taskTitleText.setError("Task name cannot be empty");
            foundError = true;
        }

        if (dateText.getText().toString().trim().isEmpty()) {
            dateText.setError("You must pick a date for the task");
            foundError = true;
        }

        int startTimeMins = taskStartTime.get() == null ? -1 : taskStartTime.get().first * 60 + taskStartTime.get().second;
        int endTimeMins = taskEndTime.get() == null ? 60 * 24 + 1 : taskEndTime.get().first * 60 + taskEndTime.get().second;

        if (startTimeText.getText().toString().trim().isEmpty()) {
            startTimeText.setError("You must pick a starting time for the task");
            foundError = true;
        } else if (startTimeMins >= endTimeMins){
            startTimeText.setError("You must pick a starting time earlier than the end time");
            foundError = true;
        }

        if (endTimeText.getText().toString().trim().isEmpty()) {
            endTimeText.setError("You must pick an end time for the task");
            foundError = true;
        } else if (startTimeMins >= endTimeMins){
            endTimeText.setError("You must pick an end time later than the start time");
            foundError = true;
        }

        if (foundError)
            return;

        Calendar c = Calendar.getInstance();
        c.setTime(taskDate.get());
        c.set(Calendar.HOUR_OF_DAY, taskStartTime.get().first);
        c.set(Calendar.MINUTE, taskStartTime.get().second);

        Date startTime = c.getTime();

        c.set(Calendar.HOUR_OF_DAY, taskEndTime.get().first);
        c.set(Calendar.MINUTE, taskEndTime.get().second);

        Date endTime = c.getTime();

        Task task = new Task(taskTitleText.getText().toString(), startTime, endTime);

        TaskViewModel taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.insert(task);

        dismiss();
    }

//    private void
}