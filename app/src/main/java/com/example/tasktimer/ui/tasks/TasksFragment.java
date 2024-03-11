package com.example.tasktimer.ui.tasks;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasktimer.MainActivity;
import com.example.tasktimer.R;
import com.example.tasktimer.adapters.Task_RecyclerViewAdapter;
import com.example.tasktimer.database.viewmodels.TaskViewModel;
import com.example.tasktimer.databinding.FragmentTasksBinding;
import com.example.tasktimer.model.Task;
import com.example.tasktimer.ui.AddTaskPopup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;

    RecyclerView pastTasksList;
    RecyclerView curTasksList;
    RecyclerView futureTasksList;

    ImageButton dropdown1;
    ImageButton dropdown2;
    ImageButton dropdown3;

    private enum ListType { PAST, CURRENT, FUTURE }
    private int[] heights = new int[3];

    final int ANIM_DURATION = 300;

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

        dropdown1.setOnClickListener(v -> {
            showTaskList(ListType.PAST, height);
        });

        dropdown2.setOnClickListener(v -> {
            showTaskList(ListType.CURRENT, height);
        });

        dropdown3.setOnClickListener(v -> {
            showTaskList(ListType.FUTURE, height);
        });

        FloatingActionButton addTaskFAB = root.findViewById(R.id.addTaskButton);
        addTaskFAB.setOnClickListener(v -> {
            DialogFragment popup = new AddTaskPopup();
            popup.show(getParentFragmentManager(), "Add Task");
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
}