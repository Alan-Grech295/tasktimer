package com.example.tasktimer.adapters;

import static com.example.tasktimer.utils.Constants.DATE_FORMAT;
import static com.example.tasktimer.utils.Constants.TIME_FORMAT;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasktimer.R;
import com.example.tasktimer.database.viewmodels.TaskViewModel;
import com.example.tasktimer.model.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Task_RecyclerViewAdapter extends RecyclerView.Adapter<Task_RecyclerViewAdapter.ViewHolder> {

    private List<Task> tasks;
    private Context context;

    private TaskViewModel taskViewModel;

    public Task_RecyclerViewAdapter(Context context, TaskViewModel taskViewModel){
        this.context = context;
        this.tasks = new ArrayList<>();
        this.taskViewModel = taskViewModel;
    }

    @NonNull
    @Override
    public Task_RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.task_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Task_RecyclerViewAdapter.ViewHolder holder, int position) {
        Task curTask = tasks.get(position);

        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);

        holder.taskTitle.setText(curTask.getTaskName());
        if(curTask.isCompleted())
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

        holder.dateText.setText(dateFormat.format(curTask.getStart()));
        String time = timeFormat.format(curTask.getStart()) + " - " + timeFormat.format(curTask.getEnd());
        holder.timeText.setText(time);
        holder.completedCheckbox.setChecked(curTask.isCompleted());
        holder.completedCheckbox.setOnClickListener(view -> {
            curTask.setCompleted(holder.completedCheckbox.isChecked());
            taskViewModel.update(curTask);
        });
        holder.deleteButton.setOnClickListener(v -> {
            taskViewModel.delete(curTask);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks){
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView taskTitle;
        TextView dateText;
        TextView timeText;
        CheckBox completedCheckbox;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            taskTitle = itemView.findViewById(R.id.taskTitle);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            completedCheckbox = itemView.findViewById(R.id.checkBox);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
