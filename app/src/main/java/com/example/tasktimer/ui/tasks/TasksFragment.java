package com.example.tasktimer.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasktimer.R;
import com.example.tasktimer.adapters.Task_RecyclerViewAdapter;
import com.example.tasktimer.databinding.FragmentTasksBinding;
import com.example.tasktimer.model.Task;

import java.util.ArrayList;
import java.util.Date;

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;

    private ArrayList<Task> tasks = new ArrayList<Task>() {
        {
            add(new Task("Test", new Date(), new Date()));
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView taskList = root.findViewById(R.id.taskList);

        Task_RecyclerViewAdapter adapter = new Task_RecyclerViewAdapter(getContext(), tasks);
        taskList.setAdapter(adapter);
        taskList.setLayoutManager(new LinearLayoutManager(getContext()));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}