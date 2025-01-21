package com.example.timetable.fragments_timetable;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Adapter_Time_table_tab extends RecyclerView.Adapter<Adapter_Time_table_tab.MyViewHolder>
{
    private final Context context;
    private final List<LessonData> lessonDataList = new ArrayList<>();
    private int fragmentContainerId;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final Handler handler;
    private final Runnable updateRunnable;
    private static final long UPDATE_INTERVAL = 10000;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private int counter;

    private static final String PREFS_NAME = "settings_prefs";
    private static final String SHOW_CURRENT_KEY = "show_current_lesson";
    private final SharedPreferences settingsPrefs;

    private static final String PREFS_NAME_VISIBILITY = "visibility_prefs";
    private static final String SHOW_TIME_KEY = "show_time";
    private static final String SHOW_SUBJECT_KEY = "show_subject";
    private static final String SHOW_TEACHER_KEY = "show_teacher";
    private static final String SHOW_TYPE_KEY = "show_type";
    private static final String SHOW_AUDITORY_KEY = "show_auditory";
    private final SharedPreferences settingsPrefs_visibility;

    public Adapter_Time_table_tab(Context context)
    {
        this.context = context;
        settingsPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        settingsPrefs_visibility = context.getSharedPreferences(PREFS_NAME_VISIBILITY, Context.MODE_PRIVATE);

        counter = 1;

        handler = new Handler();
        updateRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                updateVisibleItems();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
    }

    public void AdapterStudentTimeTable(List<String> timetableList, int fragmentContainerId)
    {
        this.fragmentContainerId = fragmentContainerId;
        lessonDataList.clear();

        if (timetableList != null && !timetableList.isEmpty())
        {
            List<LessonData> tempLessonDataList = new ArrayList<>();

            for (int i = 0; i < timetableList.size(); i += 7)
            {
                String dayName = timetableList.get(i);
                String dayDate = timetableList.get(i + 1);
                String time = timetableList.get(i + 2);
                String subject = timetableList.get(i + 3);
                String type = timetableList.get(i + 4);
                String teacher = timetableList.get(i + 5);
                String auditory = timetableList.get(i + 6);

                String cleanedSubject = subject.replaceFirst("^(Лек|Лаб|Пр.)\\s*", "").trim();
                tempLessonDataList.add(new LessonData(dayName, dayDate, time, cleanedSubject, type, teacher, auditory));
            }

            Collections.sort(tempLessonDataList, new Comparator<LessonData>()
            {
                @Override
                public int compare(LessonData lesson1, LessonData lesson2)
                {
                    try
                    {

                        int startTime1 = timeToMinutes(lesson1.time.split(" - ")[0].trim());
                        int startTime2 = timeToMinutes(lesson2.time.split(" - ")[0].trim());
                        return Integer.compare(startTime1, startTime2);
                    }
                    catch (ParseException e)
                    {
                        return 0;
                    }
                }
            });

            lessonDataList.addAll(tempLessonDataList);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_table_student, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        if (lessonDataList != null && !lessonDataList.isEmpty())
        {
            LessonData lesson = lessonDataList.get(position);
            holder.bind(lesson);
            holder.CardView.setOnClickListener(view -> openTasksFragment(lesson));
            setCurrentLesson(holder.CardView, lesson.time, lesson.dayDate, holder.time_lesson);
        }
    }

    @Override
    public int getItemCount()
    {
        return lessonDataList != null ? lessonDataList.size() : 0;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        this.layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        handler.post(updateRunnable);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onDetachedFromRecyclerView(recyclerView);
        handler.removeCallbacks(updateRunnable);
    }

    private void updateVisibleItems()
    {
        if(recyclerView == null || layoutManager == null)
        {
            return;
        }

        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();

        for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++)
        {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);

            if (viewHolder instanceof MyViewHolder)
            {
                MyViewHolder holder = (MyViewHolder) viewHolder;
                LessonData lesson = lessonDataList.get(i);
                setCurrentLesson(holder.CardView, lesson.time, lesson.dayDate, holder.time_lesson);
            }
        }
    }

    private void setCurrentLesson(CardView cardView, String lessonTime, String lessonDate, TextView timeTextView)
    {
        boolean showCurrentLesson = settingsPrefs.getBoolean(SHOW_CURRENT_KEY, true);

        if (!showCurrentLesson)
        {
            return;
        }

        try
        {
            String[] timeParts = lessonTime.split(" - ");
            if (timeParts.length != 2)
            {
                return;
            }

            int startTimeMinutes = timeToMinutes(timeParts[0].trim());
            int endTimeMinutes = timeToMinutes(timeParts[1].trim());

            Calendar currentTime = Calendar.getInstance();
            String currentDate = DATE_FORMAT.format(currentTime.getTime());
            int nowMinutes = timeToMinutes(TIME_FORMAT.format(currentTime.getTime()));

            if(lessonDate != null && lessonDate.equals(currentDate))
            {
                if (nowMinutes >= startTimeMinutes && nowMinutes <= endTimeMinutes)
                {
                    counter += 1;

                    cardView.setBackgroundResource(R.drawable.background_current_lesson);
                    String currentTimeString = lessonTime.replace("-", "–") + " | сейчас идёт";
                    SpannableString spannableString = new SpannableString(currentTimeString);
                    int startIndex = currentTimeString.indexOf("|");

                    if (startIndex != -1)
                    {
                        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.green, null)),
                                startIndex, currentTimeString.length(), 0);
                    }
                    timeTextView.setText(spannableString);
                }
                else if(nowMinutes < startTimeMinutes && counter == 1)
                {
                    counter += 1;

                    cardView.setBackgroundResource(R.drawable.background_next_lesson);

                    String currentTimeString = lessonTime.replace("-", "–") + " | следующая";
                    SpannableString spannableString = new SpannableString(currentTimeString);
                    int startIndex = currentTimeString.indexOf("|");

                    if (startIndex != -1)
                    {
                        spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.orange, null)),
                                startIndex, currentTimeString.length(), 0);
                    }
                    timeTextView.setText(spannableString);
                }
                /*else
                {
                    String currentTimeString = lessonTime.replace("-", "–");
                    timeTextView.setText(currentTimeString);
                    cardView.setBackgroundResource(R.color.white_gray);
                }*/
            }
        }
        catch (ParseException ignored) {}
    }

    private int timeToMinutes(String time) throws ParseException
    {
        Date date = TIME_FORMAT.parse(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        return (hours * 60) + minutes;
    }

    private void openTasksFragment(LessonData lesson)
    {
        if (context == null)
        {
            return;
        }

        Tasks_fragment tasks_fragment = new Tasks_fragment();
        Bundle args = new Bundle();
        args.putString("предмет", lesson.cleanedSubjectName);
        args.putString("преподователь", lesson.teacher);
        tasks_fragment.setArguments(args);

        FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
        transaction.replace(fragmentContainerId, tasks_fragment);
        transaction.addToBackStack(null);
        transaction.commit();
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


    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView num_lesson, time_lesson, form_lesson, name_lesson, teacher_lesson, auditorium_lesson;
        private final CardView CardView;
        private final ImageView teacher_Icon, auditorium_Icon;

        public MyViewHolder(View itemView)
        {
            super(itemView);

            CardView = itemView.findViewById(R.id.CardView_item_time_table_item_timetable_student);
            num_lesson = itemView.findViewById(R.id.num_lesson_item_timetable_student);
            time_lesson = itemView.findViewById(R.id.time_lesson_item_timetable_student);
            form_lesson = itemView.findViewById(R.id.form_lesson_item_timetable_student);
            name_lesson = itemView.findViewById(R.id.name_lesson_item_timetable_student);
            teacher_lesson = itemView.findViewById(R.id.teacher_lesson_item_timetable_student);
            auditorium_lesson = itemView.findViewById(R.id.auditorium_lesson_item_timetable_student);
            teacher_Icon = itemView.findViewById(R.id.src_teacher_lesson_item_timetable_student);
            auditorium_Icon = itemView.findViewById(R.id.src_auditorium_lesson_item_timetable_student);
        }

        public void bind(LessonData lesson)
        {
            boolean showTime = settingsPrefs_visibility.getBoolean(SHOW_TIME_KEY, true);
            boolean showSubject = settingsPrefs_visibility.getBoolean(SHOW_SUBJECT_KEY, true);
            boolean showTeacher = settingsPrefs_visibility.getBoolean(SHOW_TEACHER_KEY, true);
            boolean showType = settingsPrefs_visibility.getBoolean(SHOW_TYPE_KEY, true);
            boolean showAuditory = settingsPrefs_visibility.getBoolean(SHOW_AUDITORY_KEY, true);

            setVisibility(showTime, num_lesson, time_lesson);
            setVisibility(showType, form_lesson);
            setVisibility(showSubject, name_lesson);
            setVisibility(showTeacher, teacher_lesson, teacher_Icon);
            setVisibility(showAuditory, auditorium_lesson, auditorium_Icon);

            num_lesson.setText(getLessonNumber(lesson.time));
            time_lesson.setText(lesson.time.replace("-", "–"));
            form_lesson.setText(lesson.type);
            name_lesson.setText(lesson.cleanedSubjectName);
            teacher_lesson.setText(lesson.teacher);
            auditorium_lesson.setText(lesson.auditory);
        }
    }

    private void setVisibility(boolean show, View... views)
    {
        for (View view : views)
        {
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private static class LessonData
    {
        String dayName;
        String dayDate;
        String time;
        String cleanedSubjectName;
        String type;
        String teacher;
        String auditory;

        public LessonData(String dayName, String dayDate, String time, String cleanedSubjectName, String type, String teacher, String auditory)
        {
            this.dayName = dayName;
            this.dayDate = dayDate;
            this.time = time;
            this.cleanedSubjectName = cleanedSubjectName;
            this.type = type;
            this.teacher = teacher;
            this.auditory = auditory;
        }
    }
}