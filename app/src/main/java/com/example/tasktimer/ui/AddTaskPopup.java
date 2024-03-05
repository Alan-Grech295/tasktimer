package com.example.tasktimer.ui;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.tasktimer.R;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddTaskPopup#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddTaskPopup extends DialogFragment {

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
        EditText taskTitleText = view.findViewById(R.id.taskNameEditText);
        EditText dateText = view.findViewById(R.id.dateEditText);
        EditText startTimeText = view.findViewById(R.id.startTimeEditText);
        EditText endTimeText = view.findViewById(R.id.endTimeEditText);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

        dateText.setOnClickListener(v -> {
            Date curDate = new Date();
            try {
                Calendar c = Calendar.getInstance();
                c.setTime(Objects.requireNonNull(formatter.parse(dateText.getText().toString())));
                c.add(Calendar.DAY_OF_MONTH, 1);
                curDate = c.getTime();
            } catch (ParseException e) {
            }

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
                Date date = new Date(o);
                dateText.setText(formatter.format(date));
            });
        });

        View.OnClickListener timePickerListener = v -> {
            EditText edit = (EditText)v;
            Date curTime = new Date();

            try {
                curTime = timeFormatter.parse(edit.getText().toString());
            } catch (ParseException e) {
            }

            Calendar c = Calendar.getInstance();
            c.setTime(curTime);

            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(c.get(Calendar.HOUR_OF_DAY))
                    .setMinute(c.get(Calendar.MINUTE))
                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                    .setTitleText("Select Appointment time")
                    .build();

            picker.show(getParentFragmentManager(), null);

            picker.addOnPositiveButtonClickListener(o -> {
                int minute = picker.getMinute();
                int hour = picker.getHour();

                String minText = String.format("%2s", minute).replace(' ', '0');
                String hourText = String.format("%2s", hour).replace(' ', '0');

                edit.setText(hourText + ":" + minText);
            });
        };

        startTimeText.setOnClickListener(timePickerListener);
        endTimeText.setOnClickListener(timePickerListener);


        // Inflate the layout for this fragment
        return view;
    }

//    private void
}