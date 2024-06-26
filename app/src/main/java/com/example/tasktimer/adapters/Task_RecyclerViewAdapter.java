package com.example.tasktimer.adapters;

import static com.example.tasktimer.utils.Constants.DATE_FORMAT;
import static com.example.tasktimer.utils.Constants.TIME_FORMAT;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

// Recycler view adapter for displaying the user's tasks
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

        // Formats the date to the given format
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);

        holder.taskTitle.setText(curTask.getTaskName());
        // Strikethrough title text if the task is completed
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
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        taskViewModel.delete(curTask);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };

            // Displays a confirmation modal
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Are you sure you want to delete the task '" + curTask.getTaskName() + "'?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
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
