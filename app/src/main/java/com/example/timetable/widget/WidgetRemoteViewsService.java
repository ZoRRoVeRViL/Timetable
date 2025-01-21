package com.example.timetable.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.timetable.MainActivity;
import com.example.timetable.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WidgetRemoteViewsService extends RemoteViewsService
{
    private static final String TIMETABLE_DATA_PREF = "timetable_data";
    private Map<String, List<List<String>>> allTimetableData = null;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new WidgetRemoteViewsFactory(getApplicationContext());
    }

    private static class LessonDataWidget
    {
        String time;
        String subject;
        String type;
        String teacher;
        String auditory;

        public LessonDataWidget(String time, String subject, String type, String teacher, String auditory)
        {
            this.time = time;
            this.subject = subject;
            this.type = type;
            this.teacher = teacher;
            this.auditory = auditory;
        }
    }

    private class WidgetRemoteViewsFactory implements RemoteViewsFactory
    {
        private final Context context;
        private  List<LessonDataWidget> lessonDataList;

        public WidgetRemoteViewsFactory(Context context)
        {
            this.context = context;
            loadSavedTimetableData(context);
        }

        @Override
        public void onCreate() {}

        @Override
        public void onDataSetChanged()
        {
            loadSavedTimetableData(context);
        }

        @Override
        public void onDestroy() {}

        @Override
        public int getCount()
        {
            if(allTimetableData != null && !allTimetableData.isEmpty())
            {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat DATE_FORMAT_FOR_FRAGMENT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                String formattedDate = DATE_FORMAT_FOR_FRAGMENT.format(calendar.getTime());
                List<List<String>> timetableForDay = allTimetableData.get(formattedDate);

                if (timetableForDay != null && !timetableForDay.isEmpty() && !timetableForDay.get(0).get(0).equals("Empty"))
                {
                    List<String> flatList = flatten(timetableForDay);
                    lessonDataList = new ArrayList<>();

                    for (int i = 0; i < flatList.size(); i += 7)
                    {
                        String time = flatList.get(i + 2);
                        String subject = flatList.get(i + 3);
                        String type = flatList.get(i + 4);
                        String teacher = flatList.get(i + 5);
                        String auditory = flatList.get(i + 6);

                        lessonDataList.add(new LessonDataWidget(time, subject,type,teacher,auditory));

                    }

                    lessonDataList.sort((lesson1, lesson2) ->
                    {
                        int startTime1 = timeToMinutes(lesson1.time.split(" - ")[0].trim());
                        int startTime2 = timeToMinutes(lesson2.time.split(" - ")[0].trim());

                        return Integer.compare(startTime1, startTime2);
                    });

                    return lessonDataList.size();
                }
            }
            return 0;
        }

        @Override
        public RemoteViews getViewAt(int position)
        {
            if (lessonDataList == null || lessonDataList.isEmpty() || lessonDataList.size() <= position)
            {
                return null;
            }

            LessonDataWidget lesson = lessonDataList.get(position);

            RemoteViews lessonView = new RemoteViews(context.getPackageName(), R.layout.widget_item_timetable);
            lessonView.setTextViewText(R.id.num_lesson_item_widget_1, getLessonNumber(lesson.time));
            lessonView.setTextViewText(R.id.time_lesson_item_widget_1, lesson.time.replace("-", "–"));
            lessonView.setTextViewText(R.id.name_lesson_item_widget_1, lesson.subject);
            lessonView.setTextViewText(R.id.form_lesson_item_widget_1, lesson.type);
            lessonView.setTextViewText(R.id.teacher_lesson_item_widget_1, lesson.teacher);
            lessonView.setTextViewText(R.id.auditorium_lesson_item_widget_1, lesson.auditory);

            // Создание Intent и PendingIntent тут
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent =  PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            lessonView.setOnClickPendingIntent(R.id.LinearLayout_item_time_table_item_widget_1, pendingIntent);
            return lessonView;
        }

        @Override
        public RemoteViews getLoadingView()
        {
            return null;
        }

        @Override
        public int getViewTypeCount()
        {
            return 1;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public boolean hasStableIds()
        {
            return true;
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

        private int timeToMinutes(String time)
        {
            try
            {
                SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date date = TIME_FORMAT.parse(time);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                return (hours * 60) + minutes;
            }
            catch (Exception e)
            {
                return -1;
            }

        }

        private List<String> flatten(List<List<String>> nestedList)
        {
            List<String> flatList = new ArrayList<>();

            if (nestedList != null)
            {
                for (List<String> innerList : nestedList)
                {
                    flatList.addAll(innerList);
                }
            }

            return flatList;
        }

        private String getLessonNumber(String time)
        {
            if (time.contains("8:00")) return "1";
            if (time.contains("9:50")) return "2";
            if (time.contains("11:40")) return "3";
            if (time.contains("14:00")) return "4";
            if (time.contains("15:50")) return "5";
            if (time.contains("17:40")) return "6";
            if (time.contains("19:")) return "7";
            if (time.contains("20:")) return "8";

            return "";
        }
    }
}