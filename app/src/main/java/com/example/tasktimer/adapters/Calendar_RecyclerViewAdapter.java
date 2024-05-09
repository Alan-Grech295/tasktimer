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

public class Calendar_RecyclerViewAdapter extends RecyclerView.Adapter<Calendar_RecyclerViewAdapter.ViewHolder> {
    private String[] calendarNames;
    private Context context;

    private OnCalendarChosen calendarChosenCallback;

    public Calendar_RecyclerViewAdapter(Context context, String[] calendarNames, OnCalendarChosen calendarChosenCallback){
        this.context = context;
        this.calendarNames = calendarNames;
        this.calendarChosenCallback = calendarChosenCallback;
    }

    @NonNull
    @Override
    public Calendar_RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.calendar_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Calendar_RecyclerViewAdapter.ViewHolder holder, int position) {
        String calendarName = calendarNames[position];

        holder.calendarName.setText(calendarName);
        holder.view.setOnClickListener(v -> {
            calendarChosenCallback.run(calendarName);
        });
    }

    @Override
    public int getItemCount() {
        return calendarNames.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView calendarName;
        View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            calendarName = itemView.findViewById(R.id.calendarName);
        }
    }

    public interface OnCalendarChosen {
        void run(String name);
    }
}
