package com.example.timetable.fragments_timetable;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Time_table_tab_layout_fragment extends Fragment
{
    private static final String TAG = "TimeTableFragment";
    private View view;
    private LinearLayout first_window;
    private TextView text_no;
    private SharedPreferences sharedPreferences_load;
    private RecyclerView recyclerview_time_table;
    private Adapter_Time_table_tab Adapter;
    private FrameLayout fragmentContainer;
    private String GroupName;
    public String dayOfWeek;
    public String selectedCalendar;
    private List<String> timetableForDay;

    private static final String GROUP_NOT_SELECT = "Группа не выбрана \nвыберите свою группу в настройках";
    private static final String SELECT_GROUP_PREF = "select_group";
    private static final String ARG_DATE = "date";
    private static final String ARG_TIMETABLE_DATA = "timetable_data";

    public static Time_table_tab_layout_fragment newInstance(String date, List<String> timetableData)
    {
        Time_table_tab_layout_fragment fragment = new Time_table_tab_layout_fragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        args.putStringArrayList(ARG_TIMETABLE_DATA, (ArrayList<String>) timetableData);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.time_table_tab_layout_fragment, container, false);

        initViews();
        setupRecyclerView();

        Bundle args = getArguments();
        if (args != null)
        {
            selectedCalendar = args.getString(ARG_DATE);
            timetableForDay = args.getStringArrayList(ARG_TIMETABLE_DATA);
            Log.e(TAG, "Received data: " + timetableForDay);
            dayOfWeek = getDayOfWeekFromDate(selectedCalendar);
        }

        loadSavedPreferences();

        if (GroupName != null && !GroupName.isEmpty())
        {
            loadSavedTimeTable();
        }
        else
        {
            showGroupNotSelected();
        }

        return view;
    }

    private void initViews()
    {
        fragmentContainer = view.findViewById(R.id.fragment_container_time_table);
        recyclerview_time_table = view.findViewById(R.id.recyclerview_time_table);
        first_window = view.findViewById(R.id.first_window);
        text_no = view.findViewById(R.id.text_no);
    }

    private void setupRecyclerView()
    {
        recyclerview_time_table.setLayoutManager(new LinearLayoutManager(requireContext()));
        Adapter = new Adapter_Time_table_tab(getContext());
        recyclerview_time_table.setAdapter(Adapter);
    }

    private void loadSavedPreferences()
    {
        sharedPreferences_load = requireContext().getSharedPreferences(SELECT_GROUP_PREF, Context.MODE_PRIVATE);
        GroupName = sharedPreferences_load.getString("GroupName", null);
    }

    private void loadSavedTimeTable()
    {
        if (timetableForDay != null && !timetableForDay.isEmpty())
        {
            if (timetableForDay.size() == 1 && timetableForDay.get(0).equals("Empty"))
            {
                showNoScheduleTextView();
            }
            else
            {
                Adapter.AdapterStudentTimeTable(timetableForDay, R.id.fragment_container_time_table);
            }
        }
        else
        {
            showNoScheduleTextView();
        }
    }

    private void showGroupNotSelected()
    {
        first_window.setVisibility(View.VISIBLE);
        text_no.setText(GROUP_NOT_SELECT);
    }

    public void showNoScheduleTextView()
    {
        first_window.setVisibility(View.VISIBLE);
        text_no.setText(JokeTextsStudent.getRandomJoke());
        recyclerview_time_table.setVisibility(View.GONE);
    }

    private String getDayOfWeekFromDate(String dateString)
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            switch (dayOfWeek)
            {
                case Calendar.MONDAY:
                    return "ПОНЕДЕЛЬНИК";
                case Calendar.TUESDAY:
                    return "ВТОРНИК";
                case Calendar.WEDNESDAY:
                    return "СРЕДА";
                case Calendar.THURSDAY:
                    return "ЧЕТВЕРГ";
                case Calendar.FRIDAY:
                    return "ПЯТНИЦА";
                case Calendar.SATURDAY:
                    return "СУББОТА";
                case Calendar.SUNDAY:
                    return "ВОСКРЕСЕНЬЕ";
                default:
                    return "ПОНЕДЕЛЬНИК";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "ПОНЕДЕЛЬНИК";
        }
    }
}