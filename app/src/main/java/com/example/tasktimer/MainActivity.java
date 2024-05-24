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
import com.example.tasktimer.ui.ChooseCalendarPopup;
import com.example.tasktimer.utils.CalendarHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.tasktimer.databinding.ActivityMainBinding;

import java.util.Arrays;
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

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Called when the export to calendar menu item is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_export){
            CalendarHelper.CalendarData[] calendarDatas = CalendarHelper.getCalendars(this.getApplicationContext());

            DialogFragment popup = new ChooseCalendarPopup(Arrays.stream(calendarDatas).map(c -> c.calendarName).toArray(String[]::new), calendar -> {
                CalendarHelper.CalendarData calendarData = null;

                // Finds the calendar data with the calendar name
                for(CalendarHelper.CalendarData cal : calendarDatas){
                    if(cal.calendarName.equals(calendar)){
                        calendarData = cal;
                        break;
                    }
                }

                if(calendarData == null) return;
                exportToCalendar(calendarData);
            });
            popup.show(getSupportFragmentManager(), "Choose a calendar");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportToCalendar(CalendarHelper.CalendarData calendar){
        TaskViewModel taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        LiveData<List<Task>> tasksLiveData = taskViewModel.getAllTasks();
        Observer<List<Task>> observer = tasks -> {
            try {
                for(Task task : tasks){
                    // If a calendar event already exists, delete it
                    if(task.getEventURI() != null){
                        mainHandler.post(() -> {
                            CalendarHelper.deleteEvent(this, task.getEventURI());
                        });
                    }

                    mainHandler.post(() -> {
                        task.setEventURI(CalendarHelper.addEvent(this, calendar.calendarID, task.getTaskName(), task.getStart(), task.getEnd()));
                        taskViewModel.update(task);
                    });
                }

                Toast.makeText(this, String.format("Successfully exported %s tasks to %s", tasks.size(), calendar.calendarName), Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(this, "Error when exporting tasks to " + calendar.calendarName, Toast.LENGTH_SHORT).show();
            }

            tasksLiveData.removeObservers(this);
        };
        tasksLiveData.observe(this, observer);
    }
}