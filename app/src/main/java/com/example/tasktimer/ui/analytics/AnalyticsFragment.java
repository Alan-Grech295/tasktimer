package com.example.tasktimer.ui.analytics;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tasktimer.R;
import com.example.tasktimer.database.viewmodels.TaskViewModel;
import com.example.tasktimer.databinding.FragmentAnalyticsBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;

    private BarChart productiveHoursChart;

    private TaskViewModel taskViewModel;

    private Handler mainHandler;

    private ArrayList<BarEntry> productiveHoursData = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainHandler = new Handler(getContext().getMainLooper());

        productiveHoursChart = root.findViewById(R.id.barChart);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        bindProductiveHours();
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