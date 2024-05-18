package com.example.tasktimer.ui.analytics;

import static com.example.tasktimer.utils.Constants.DATE_FORMAT;
import static com.example.tasktimer.utils.Utils.onSameDay;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.tasktimer.R;
import com.example.tasktimer.database.viewmodels.TaskViewModel;
import com.example.tasktimer.databinding.FragmentAnalyticsBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;

    private BarChart productiveHoursChart;
    private LineChart tasksCompletedChart;
    private TextView completedTasksText;

    private TaskViewModel taskViewModel;

    private Handler mainHandler;

    private ArrayList<BarEntry> productiveHoursData = new ArrayList<>();
    private ArrayList<Entry> completedTasksData = new ArrayList<>();

    private ArrayList<LiveData<Integer>> completedTasksLiveData = new ArrayList<>();
    private ArrayList<String> taskCompletedXLabels = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainHandler = new Handler(getContext().getMainLooper());

        productiveHoursChart = root.findViewById(R.id.barChart);

        tasksCompletedChart = root.findViewById(R.id.lineChart);

        completedTasksText = root.findViewById(R.id.completedTasks);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        bindProductiveHours();
        bindCompletedTasks();
    }

    private void bindCompletedTasks() {
        taskViewModel.getFirstTask().observe(getViewLifecycleOwner(), task -> {
            Date taskStart = new Date();

            if(task != null){
                taskStart = task.getStart();
            }

            bindTasksFromDate(taskStart);

            LineDataSet dataSet = new LineDataSet(completedTasksData, "Completed Tasks");
            LineData lineData = new LineData(dataSet);
            tasksCompletedChart.setData(lineData);

            XAxis xAxis = tasksCompletedChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(2);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(){
                @Override
                public String getFormattedValue(float value) {
                    return taskCompletedXLabels.get((int)value);
                }
            });

            tasksCompletedChart.getAxis(YAxis.AxisDependency.LEFT).setAxisMinimum(0);
            tasksCompletedChart.getAxis(YAxis.AxisDependency.RIGHT).setAxisMinimum(0);

            dataSet.setValueTextSize(12f);
            tasksCompletedChart.getDescription().setEnabled(false);

            tasksCompletedChart.getLegend().setEnabled(false);
        });
    }

    private void bindTasksFromDate(Date startDate) {
        completedTasksLiveData.forEach(liveData -> liveData.removeObservers(getViewLifecycleOwner()));

        completedTasksLiveData.clear();
        completedTasksData.clear();
        taskCompletedXLabels.clear();

        Date now = new Date();
        final int DAY_IN_MS = 24 * 60 * 60 * 1000;

        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        startDate = c.getTime();

        int index = 0;
        while(startDate.getTime() <= now.getTime()){
            Entry entry = new Entry(index, 0);
            completedTasksData.add(entry);
            taskCompletedXLabels.add(dateFormat.format(startDate));
            LiveData<Integer> taskCountLiveData = taskViewModel.getCompletedTaskCount(startDate);
            completedTasksLiveData.add(taskCountLiveData);

            final Date currentDate = new Date(startDate.getTime());
            taskCountLiveData.observe(getViewLifecycleOwner(), count -> {
                entry.setY(count);

                LineDataSet set = (LineDataSet)tasksCompletedChart.getData().getDataSetByIndex(0);
                set.setValues(completedTasksData);
                set.setColors(new int[] {R.color.primary}, getContext());
                set.setCircleColors(new int[] {R.color.black}, getContext());
                tasksCompletedChart.getData().notifyDataChanged();
                tasksCompletedChart.notifyDataSetChanged();
                tasksCompletedChart.invalidate();

                if(onSameDay(now, currentDate)){
                    completedTasksText.setText(count.toString());
                }
            });

            startDate.setTime(startDate.getTime() + DAY_IN_MS);
            index++;
        }
    }

    private void bindProductiveHours(){
        for(int i = 0; i < 24; i++){
            BarEntry entry = new BarEntry(i, 0);
            productiveHoursData.add(entry);
            taskViewModel.getProductiveHour(i).observe(getViewLifecycleOwner(), (count) -> {
                entry.setY(count);

                BarDataSet set = (BarDataSet)productiveHoursChart.getData().getDataSetByIndex(0);
                set.setValues(productiveHoursData);
                productiveHoursChart.getData().notifyDataChanged();
                productiveHoursChart.notifyDataSetChanged();
                productiveHoursChart.invalidate();
            });
        }

        BarDataSet dataSet = new BarDataSet(productiveHoursData, "Productive Hours");
        dataSet.setColors(new int[] {R.color.primary}, getContext());
        BarData barData = new BarData(dataSet);
        productiveHoursChart.setData(barData);

        XAxis xAxis = productiveHoursChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        dataSet.setValueTextSize(12f);
        productiveHoursChart.getDescription().setEnabled(false);

        productiveHoursChart.getLegend().setEnabled(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}