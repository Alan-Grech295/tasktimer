package com.example.tasktimer.ui.tasks;

import static com.example.tasktimer.utils.Constants.DATE_FORMAT;
import static com.example.tasktimer.utils.Constants.TIME_FORMAT;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasktimer.R;
import com.example.tasktimer.adapters.Task_RecyclerViewAdapter;
import com.example.tasktimer.database.viewmodels.TaskViewModel;
import com.example.tasktimer.databinding.FragmentTasksBinding;
import com.example.tasktimer.model.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;

    RecyclerView pastTasksList;
    RecyclerView curTasksList;
    RecyclerView futureTasksList;

    ImageView dropdown1;
    ImageView dropdown2;
    ImageView dropdown3;

    private enum ListType { PAST, CURRENT, FUTURE }
    private int[] heights = new int[3];

    final int ANIM_DURATION = 200;

    int height = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        pastTasksList = root.findViewById(R.id.pastTasksList);
        curTasksList = root.findViewById(R.id.curTasksList);
        futureTasksList = root.findViewById(R.id.futureTasksList);

        ViewTreeObserver vto = curTasksList.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                curTasksList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                height = curTasksList.getMeasuredHeight();
                heights[ListType.CURRENT.ordinal()] = height;
            }
        });

//        int height = getTaskListHeight();

        TaskViewModel taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Past tasks
        Task_RecyclerViewAdapter pastAdapter = setupRecyclerView(pastTasksList, taskViewModel);

        taskViewModel.getTasksBeforeDate(new Date()).observe(getViewLifecycleOwner(), pastAdapter::setTasks);

        // Current tasks
        Task_RecyclerViewAdapter curAdapter = setupRecyclerView(curTasksList, taskViewModel);

        taskViewModel.getTasksByDate(new Date()).observe(getViewLifecycleOwner(), curAdapter::setTasks);

        // Future tasks
        Task_RecyclerViewAdapter futureAdapter = setupRecyclerView(futureTasksList, taskViewModel);

        taskViewModel.getTasksAfterDate(new Date()).observe(getViewLifecycleOwner(), futureAdapter::setTasks);

        dropdown1 = root.findViewById(R.id.dropdownIcon1);
        dropdown2 = root.findViewById(R.id.dropdownIcon2);
        dropdown3 = root.findViewById(R.id.dropdownIcon3);

        root.findViewById(R.id.pastTasksLayout).setOnClickListener(v -> {
            showTaskList(ListType.PAST, height);
        });

        root.findViewById(R.id.curTasksLayout).setOnClickListener(v -> {
            showTaskList(ListType.CURRENT, height);
        });

        root.findViewById(R.id.futureTasksLayout).setOnClickListener(v -> {
            showTaskList(ListType.FUTURE, height);
        });

        showTaskList(ListType.CURRENT, height);

        FloatingActionButton addTaskFAB = root.findViewById(R.id.addTaskButton);
        addTaskFAB.setOnClickListener(v -> {
//            DialogFragment popup = new AddTaskPopup();
//            popup.show(getParentFragmentManager(), "Add Task");

            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), com.google.android.material.R.style.Base_V14_ThemeOverlay_MaterialComponents_BottomSheetDialog);
            View bottomSheetView = LayoutInflater.from(getActivity().getApplicationContext())
                    .inflate(R.layout.fragment_add_task_popup, root.findViewById(R.id.popupContainer));

            AtomicReference<Date> taskDate = new AtomicReference<>(null);
            AtomicReference<Pair<Integer, Integer>> taskStartTime = new AtomicReference<>(null);
            AtomicReference<Pair<Integer, Integer>> taskEndTime = new AtomicReference<>(null);

            EditText taskTitleText = bottomSheetView.findViewById(R.id.taskNameEditText);
            EditText dateText = bottomSheetView.findViewById(R.id.dateEditText);
            EditText startTimeText = bottomSheetView.findViewById(R.id.startTimeEditText);
            EditText endTimeText = bottomSheetView.findViewById(R.id.endTimeEditText);

            Button cancelButton = bottomSheetView.findViewById(R.id.cancelButton);
            Button addButton = bottomSheetView.findViewById(R.id.addTaskButton);

            cancelButton.setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
            });

            addButton.setOnClickListener(view -> {
                if(addGoal(taskTitleText, dateText, taskStartTime, taskEndTime, taskDate, startTimeText, endTimeText)){
                    bottomSheetDialog.dismiss();
                }
            });

            SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            SimpleDateFormat timeFormatter = new SimpleDateFormat(TIME_FORMAT);

            dateText.setOnClickListener(view -> {
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

            startTimeText.setOnClickListener(view -> {
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

            endTimeText.setOnClickListener(view -> {
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

            bottomSheetDialog.setContentView(bottomSheetView);

            BottomSheetBehavior<View> behaviour = BottomSheetBehavior.from((View)bottomSheetView.getParent());
            behaviour.setState(BottomSheetBehavior.STATE_EXPANDED);

            bottomSheetDialog.show();
        });

        pastTasksList.setVisibility(View.GONE);
        futureTasksList.setVisibility(View.GONE);

        return root;
    }

    private int getTaskListHeight(){
        pastTasksList.setVisibility(View.GONE);
        curTasksList.setVisibility(View.GONE);
        futureTasksList.setVisibility(View.VISIBLE);

        int height = futureTasksList.getMeasuredHeight();

        pastTasksList.setVisibility(View.VISIBLE);
        curTasksList.setVisibility(View.VISIBLE);

        return height;
    }

    private Task_RecyclerViewAdapter setupRecyclerView(RecyclerView list, TaskViewModel viewModel){
        Task_RecyclerViewAdapter adapter = new Task_RecyclerViewAdapter(getContext(), viewModel);
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        return adapter;
    }

    private void showTaskList(ListType type, int height){
        pastTasksList.setVisibility(View.VISIBLE);
        curTasksList.setVisibility(View.VISIBLE);
        futureTasksList.setVisibility(View.VISIBLE);

        ValueAnimator pastTasksAnim = ValueAnimator.ofInt(heights[ListType.PAST.ordinal()],
                type == ListType.PAST ? height : 0);
        pastTasksAnim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = pastTasksList.getLayoutParams();
            layoutParams.height = val;
            heights[ListType.PAST.ordinal()] = val;
            pastTasksList.setLayoutParams(layoutParams);
        });
        pastTasksAnim.setDuration(ANIM_DURATION);
        pastTasksAnim.start();

        ValueAnimator curTasksAnim = ValueAnimator.ofInt(heights[ListType.CURRENT.ordinal()],
                type == ListType.CURRENT ? height : 0);
        curTasksAnim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = curTasksList.getLayoutParams();
            layoutParams.height = val;
            heights[ListType.CURRENT.ordinal()] = val;
            curTasksList.setLayoutParams(layoutParams);
        });
        curTasksAnim.setDuration(ANIM_DURATION);
        curTasksAnim.start();

        ValueAnimator futureTasksAnim = ValueAnimator.ofInt(heights[ListType.FUTURE.ordinal()],
                type == ListType.FUTURE ? height : 0);
        futureTasksAnim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = futureTasksList.getLayoutParams();
            layoutParams.height = val;
            heights[ListType.FUTURE.ordinal()] = val;
            futureTasksList.setLayoutParams(layoutParams);
        });
        futureTasksAnim.setDuration(ANIM_DURATION);
        futureTasksAnim.start();

        dropdown1.animate().rotation(type == ListType.PAST ? 0 : -90).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(ANIM_DURATION);
        dropdown2.animate().rotation(type == ListType.CURRENT ? 0 : -90).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(ANIM_DURATION);
        dropdown3.animate().rotation(type == ListType.FUTURE ? 0 : -90).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(ANIM_DURATION);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private Pair<Integer, Integer> dateToTime(Date date){
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        return new Pair<>(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    private boolean addGoal(EditText taskTitleText, EditText dateText,
                         AtomicReference<Pair<Integer, Integer>> taskStartTime,
                         AtomicReference<Pair<Integer, Integer>> taskEndTime,
                         AtomicReference<Date> taskDate,
                         EditText startTimeText, EditText endTimeText){
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
            return false;

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

        return true;
    }
}