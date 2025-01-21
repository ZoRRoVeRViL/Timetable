package com.example.timetable.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.timetable.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WidgetTimetable extends AppWidgetProvider
{
    private static final String TIMETABLE_DATA_PREF = "timetable_data";
    private static final String SELECT_GROUP_PREF = "select_group";
    private static final DateFormat DAY_FORMAT = new SimpleDateFormat("d MMMM", Locale.getDefault());
    private static final DateFormat EVEN_FORMAT = new SimpleDateFormat("w", Locale.getDefault());
    private static final String WORK_TAG = "widget_update_work";

    private String GroupName;
    private Map<String, List<List<String>>> allTimetableData = null;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        for (int appWidgetId : appWidgetIds)
        {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        scheduleWidgetUpdate(context);
    }

    @Override
    public void onEnabled(Context context)
    {
        // Вызывается при первом добавлении виджета
        super.onEnabled(context);
        scheduleWidgetUpdate(context);
    }

    @Override
    public void onDisabled(Context context)
    {
        // Вызывается при удалении последнего виджета
        super.onDisabled(context);
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG);
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_timetable);
        Intent emptyIntent = new Intent();
        android.app.PendingIntent emptyPendingIntent = android.app.PendingIntent.getActivity(context, 0,
                emptyIntent, android.app.PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_timetable_main_layout, emptyPendingIntent);

        loadSavedPreferences(context);
        loadSavedTimetableData(context);
        setupWidget(context, views);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void setupWidget(Context context, RemoteViews views)
    {
        if(allTimetableData == null || GroupName == null || GroupName.isEmpty())
        {
            views.setTextViewText(R.id.day_week_widget_1, "Нет данных");
            views.setTextViewText(R.id.even_uneven_widget_1, "Нет данных");

            return;
        }

        Date currentDate = new Date();
        String dayMonth = DAY_FORMAT.format(currentDate);
        String evenText = EVEN_FORMAT.format(currentDate);
        int even = Integer.parseInt(evenText);
        String weekTypeText = (even % 2 == 0) ? "чётная**" : "нечётная*";
        views.setTextViewText(R.id.day_week_widget_1, dayMonth);
        views.setTextViewText(R.id.even_uneven_widget_1, weekTypeText);

        Intent serviceIntent = new Intent(context, WidgetRemoteViewsService.class);
        views.setRemoteAdapter(R.id.widget_timetable_list_view, serviceIntent);
    }

    private void loadSavedTimetableData(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TIMETABLE_DATA_PREF, Context.MODE_PRIVATE);
        String jsonTimetable = sharedPreferences.getString("timetable-list", null);

        if (jsonTimetable != null)
        {
            try
            {
                TypeToken<Map<String, List<List<String>>>> token = new TypeToken<Map<String, List<List<String>>>>() {};
                Map<String, List<List<String>>> savedData = new Gson().fromJson(jsonTimetable, token.getType());

                if (savedData != null)
                {
                    allTimetableData = savedData;
                }
            }
            catch (Exception ignored) {}
        }
    }

    private void loadSavedPreferences(Context context)
    {
        SharedPreferences sharedPreferences_load = context.getSharedPreferences(SELECT_GROUP_PREF, Context.MODE_PRIVATE);
        GroupName = sharedPreferences_load.getString("GroupName", null);
    }

    // Метод для планирования периодического обновления
    private void scheduleWidgetUpdate(Context context)
    {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(WidgetUpdateWorker.class, 10, TimeUnit.SECONDS)
                .setConstraints(constraints).addTag(WORK_TAG).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.REPLACE, workRequest);
    }

    // Метод для обновления виджета (вызов вручную)
    public static void updateWidgetManually(Context context)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, WidgetTimetable.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        if(appWidgetIds != null && appWidgetIds.length > 0)
        {
            for (int appWidgetId : appWidgetIds)
            {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_timetable);

                // создаем инстанцию этого класса, чтобы вызвать его метод
                WidgetTimetable widgetTimetable = new WidgetTimetable();
                widgetTimetable.loadSavedPreferences(context);
                widgetTimetable.loadSavedTimetableData(context);
                widgetTimetable.setupWidget(context, views);

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }
}