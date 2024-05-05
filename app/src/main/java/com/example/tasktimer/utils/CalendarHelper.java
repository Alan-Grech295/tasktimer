package com.example.tasktimer.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class CalendarHelper {

    public static class CalendarData {
        public String calendarName;
        public String calendarID;

        public CalendarData(String calendarName, String calendarID){
            this.calendarID = calendarID;
            this.calendarName = calendarName;
        }
    }

    public static final int PERMISSION_REQUEST_WRITE_CALENDAR = 1;
    public static final int PERMISSION_REQUEST_READ_CALENDAR = 2;

    public static String addEvent(Activity activity, String calendarId, String title, Date startTime,
                                  Date endTime) {
        if (activity.getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_CALENDAR}, PERMISSION_REQUEST_WRITE_CALENDAR);
            return "";
        }

        ContentValues event = new ContentValues();
        event.put(CalendarContract.Events.CALENDAR_ID, calendarId); // "" for insert
        event.put(CalendarContract.Events.TITLE, title);
//        event.put(CalendarContract.Events.DESCRIPTION, "");
//        event.put(CalendarContract.Events.EVENT_LOCATION, "");
//        event.put(CalendarContract.Events.ALL_DAY, 0);
//        event.put(CalendarContract.Events.STATUS, 1);
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().toString());
//        event.put("transparency", 0);
        event.put(CalendarContract.Events.DTSTART, startTime.getTime());
        event.put(CalendarContract.Events.DTEND, endTime.getTime());

        ContentResolver contentResolver = activity.getApplicationContext().getContentResolver();
        Uri url = contentResolver.insert(CalendarContract.Events.CONTENT_URI, event);
        String ret = url.toString();
        return ret;
    }

    public static int deleteEvent(Activity activity, String eventURI) {
        if (activity.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_CALENDAR}, PERMISSION_REQUEST_WRITE_CALENDAR);
            return 0;
        }

        if (activity.checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CALENDAR}, PERMISSION_REQUEST_READ_CALENDAR);
            return 0;
        }

        ContentResolver contentResolver = activity.getApplicationContext().getContentResolver();
        return contentResolver.delete(Uri.parse(eventURI), null, null);
    }

    public static CalendarData[] getCalendars(Context c) {

        String[] projection = {"_id", "calendar_displayName"};
        Uri calendars;
        calendars = Uri.parse("content://com.android.calendar/calendars");

        ContentResolver contentResolver = c.getContentResolver();
        Cursor managedCursor = contentResolver.query(calendars, projection, null, null, null);

        CalendarData[] calendarDatas = new CalendarData[0];

        if (managedCursor.moveToFirst()){
            calendarDatas = new CalendarData[managedCursor.getCount()];
            String calName;
            String calID;
            int cont= 0;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);
            do {
                calName = managedCursor.getString(nameCol);
                calID = managedCursor.getString(idCol);
                calendarDatas[cont] = new CalendarData(calName, calID);
                cont++;
            } while(managedCursor.moveToNext());
            managedCursor.close();
        }

        return calendarDatas;
    }
}
