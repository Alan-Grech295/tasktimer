package com.example.tasktimer;

import static com.example.tasktimer.utils.CalendarHelper.PERMISSION_REQUEST_READ_CALENDAR;
import static com.example.tasktimer.utils.CalendarHelper.PERMISSION_REQUEST_WRITE_CALENDAR;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.tasktimer.database.viewmodels.TaskViewModel;
import com.example.tasktimer.model.Task;
import com.example.tasktimer.utils.CalendarHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.tasktimer.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getApplicationContext().checkSelfPermission(android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, PERMISSION_REQUEST_WRITE_CALENDAR);
        }

        if (getApplicationContext().checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, PERMISSION_REQUEST_READ_CALENDAR);
        }

        mainHandler = new Handler(getMainLooper());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(navView, navController);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_import){

            return true;
        }else if(item.getItemId() == R.id.action_export){
            CalendarHelper.CalendarData[] calendarDatas = CalendarHelper.getCalendars(this.getApplicationContext());
            int calendarIndex = 8;

            TaskViewModel taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
            LiveData<List<Task>> tasksLiveData = taskViewModel.getAllTasks();
            Observer<List<Task>> observer = tasks -> {
                try {
                    for(Task task : tasks){
                        if(task.getEventURI() != null){
                            try{
                                mainHandler.post(() -> CalendarHelper.deleteEvent(this, task.getEventURI()));
                            }catch (Exception ignored){
                            }
                        }

                        task.setEventURI(CalendarHelper.addEvent(this, calendarDatas[calendarIndex].calendarID, task.getTaskName(), task.getStart(), task.getEnd()));
                    }

                    Toast.makeText(this, "Successfully exported tasks to " + calendarDatas[calendarIndex].calendarName, Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(this, "Error when exporting tasks to " + calendarDatas[calendarIndex].calendarName, Toast.LENGTH_SHORT).show();
                }

                tasksLiveData.removeObservers(this);

                for(Task task : tasks){
                    taskViewModel.update(task);
                }
            };
            tasksLiveData.observe(this, observer);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSION_REQUEST_WRITE_CALENDAR) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, proceed with inserting events
//                insertEvents();
//            } else {
//                // Permission denied
//                // Handle the situation when the user denies the permission
//            }
//        }
    }
}