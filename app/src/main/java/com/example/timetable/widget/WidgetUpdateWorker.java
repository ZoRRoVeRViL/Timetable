package com.example.timetable.widget;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WidgetUpdateWorker extends Worker
{
    public WidgetUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        WidgetTimetable.updateWidgetManually(getApplicationContext()); // Используем контекст который пришел в doWork()

        return Result.success();
    }
}