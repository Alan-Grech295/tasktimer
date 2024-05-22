package com.example.tasktimer.ui;

import static com.example.tasktimer.utils.Constants.DATE_FORMAT;
import static com.example.tasktimer.utils.Constants.TIME_FORMAT;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasktimer.R;
import com.example.tasktimer.adapters.Calendar_RecyclerViewAdapter;
import com.example.tasktimer.adapters.Task_RecyclerViewAdapter;
import com.example.tasktimer.database.viewmodels.TaskViewModel;
import com.example.tasktimer.model.Task;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

// Displays the user's calendars
public class ChooseCalendarPopup extends DialogFragment {
    String[] calendars;
    Calendar_RecyclerViewAdapter.OnCalendarChosen calendarChosenCallback;

    public ChooseCalendarPopup() {
        // Required empty public constructor
    }

    public ChooseCalendarPopup(String[] calendars, Calendar_RecyclerViewAdapter.OnCalendarChosen calendarChosenCallback) {
        this.calendars = calendars;
        this.calendarChosenCallback = calendarChosenCallback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_calendar_popup, container, false);
        RecyclerView calendarList = view.findViewById(R.id.calendarListView);
        Calendar_RecyclerViewAdapter adapter = new Calendar_RecyclerViewAdapter(getContext(), calendars, (calendar) -> {
            calendarChosenCallback.run(calendar);
            dismiss();
        });
        calendarList.setAdapter(adapter);
        calendarList.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Sets the height of the popup to be 50% of the screen height
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.5);
        getDialog().getWindow().setAttributes(params);
    }
}