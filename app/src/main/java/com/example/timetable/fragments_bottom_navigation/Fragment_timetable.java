package com.example.timetable.fragments_bottom_navigation;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.timetable.R;
import com.example.timetable.fragments_timetable.Time_table_tab_layout_fragment;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Fragment_timetable extends Fragment
{
    private static final String SELECT_GROUP_PREF = "select_group";
    private static final SimpleDateFormat DATE_FORMAT_FOR_FRAGMENT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d\nEE", Locale.getDefault());
    private static final DateFormat DAY_FORMAT = new SimpleDateFormat("d", Locale.getDefault());
    private static final DateFormat CURRENT_DATE_FORMAT = new SimpleDateFormat("MMMM", Locale.getDefault());
    private static final DateFormat EVEN_FORMAT = new SimpleDateFormat("w", Locale.getDefault());
    private static final int TOTAL_PAGES = 1;
    private static final String TIMETABLE_DATA_PREF = "timetable_data";
    private static final String LAST_PARSE_TIME_KEY_TIMETABLE = "last_parse_time_timetable";
    private static final String CALENDAR_CLICKED_PREF = "calendar_clicked";
    private static final int TIMEOUT_TABLE_LOAD = 15000;

    private View view;
    private WebView webView_timetable;
    private TabLayout tab_layout_timetable;
    private ViewPager view_pager_timetable;
    private TextView day_week, even_uneven, timetable_toolbar_subtitle;
    private SpinKitView progressBar_timetable;
    private LinearLayout no_internet_fragment_timetable, first_press_fragment_timetable;

    private String GroupName, GroupLink;
    private final Calendar selectedCalendar = Calendar.getInstance();
    private Map<String, List<List<String>>> allTimetableData = new HashMap<>();
    private int pagesLoaded = 0;
    private String currentLoadedUrl = null;
    private final Date currentDate = new Date();

    private static final String PREFS_NAME = "settings_prefs";
    private static final String LOG_KEY = "log_mode";
    private SharedPreferences settingsPrefs;
    private TextView logTextView;
    private long startTime;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_timetable, container, false);

        initViews();
        setupWebViewSettings();
        loadSavedPreferences();
        setupToolbarSubtitle();
        setupTabLayout();
        loadDate();
        setupDayWeekClick();
        setBackgroundActiveTab();
        checkFirstCalendarClick();
        loadSavedTimetableData();
        setupViewPagerWithInitialData();
        showLog();
        String lastParseTime = lastParseTime();
        log(lastParseTime);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        showProgressBar();
        loadWebsiteTimeTable();
    }

    private void initViews()
    {
        webView_timetable = view.findViewById(R.id.webView_timetable);
        tab_layout_timetable = view.findViewById(R.id.tab_layout_timetable);
        view_pager_timetable = view.findViewById(R.id.view_pager_timetable);
        even_uneven = view.findViewById(R.id.even_uneven);
        day_week = view.findViewById(R.id.day_week);
        progressBar_timetable = view.findViewById(R.id.progress_bar_timetable);
        no_internet_fragment_timetable = view.findViewById(R.id.no_internet_fragment_timetable);
        first_press_fragment_timetable = view.findViewById(R.id.first_press_fragment_timetable);
        timetable_toolbar_subtitle = view.findViewById(R.id.timetable_toolbar_subtitle);
        logTextView = view.findViewById(R.id.log_text_view_timetable);
        settingsPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void setupWebViewSettings()
    {
        WebSettings webSettings = webView_timetable.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
    }

    private void loadSavedPreferences()
    {
        SharedPreferences sharedPreferences_load = requireContext().getSharedPreferences(SELECT_GROUP_PREF, Context.MODE_PRIVATE);
        GroupName = sharedPreferences_load.getString("GroupName", null);
        GroupLink = sharedPreferences_load.getString("GroupLink", null);
    }

    public void loadWebsiteTimeTable()
    {
        try
        {
            if (!isInternetAvailable())
            {
                log("Нет подключения к интернету");
                hideProgressBar();
                showNoInternetView();
                return;
            }
            else
            {
                hideNoInternetView();
            }

            if (GroupLink != null && !GroupLink.isEmpty())
            {
                if (isTimetableParseNeeded())
                {
                    String url_load = GroupLink;
                    webView_timetable.loadUrl(url_load);
                    log("Открыт сайт: " + url_load);

                    webView_timetable.setWebViewClient(new WebViewClient()
                    {
                        @Override
                        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
                        {
                            log("Ошибка загрузки страницы: " + error.getDescription());
                        }

                        @Override
                        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse)
                        {
                            log("HTTP ошибка: " + errorResponse.getStatusCode() + " - " + errorResponse.getReasonPhrase());
                        }

                        @Override
                        public void onPageFinished(WebView view, String url)
                        {
                            log("Начало обновления расписания");

                            if (getActivity() == null || webView_timetable == null)
                            {
                                return;
                            }

                            if (currentLoadedUrl == null)
                            {
                                currentLoadedUrl = url;

                                waitForPrintButtonAndClick(view);
                            }
                        }
                    });
                }
                else
                {
                    log("Расписание обновлено менее 12ч назад");
                    hideProgressBar();
                }
            }
            else
            {
                log("Ошибка. Группа не выбрана");
                hideProgressBar();
            }

            pagesLoaded = 0;
        }
        catch (Exception e)
        {
            log("Исключение при загрузке: " + e.getMessage());
            e.printStackTrace();
            hideProgressBar();
        }
    }

    private void waitForPrintButtonAndClick(WebView view)
    {
        log("Ожидание кнопки...");
        final Handler handler = new Handler();
        final Runnable checkButtonRunnable = new Runnable()
        {
            @Override
            public void run() {
                String checkButtonLoaded = "document.querySelector('button[type=\"button\"][class*=\"stud\"] i.fas.fa-print') !== null";

                view.evaluateJavascript(checkButtonLoaded, buttonLoaded ->
                {
                    if (buttonLoaded != null && buttonLoaded.equals("true"))
                    {
                        log("Кнопка найдена!");
                        String clickAllTimetableButton = "document.querySelector('button[type=\"button\"][class*=\"stud\"]').click();";
                        view.evaluateJavascript(clickAllTimetableButton, null);

                        new Handler().postDelayed(() ->
                        {
                            String checkTableLoaded = "document.querySelector('[data-v-470231ae].flex.md12') !== null;";
                            view.evaluateJavascript(checkTableLoaded, tableLoaded ->
                            {
                                if (tableLoaded != null && tableLoaded.equals("true"))
                                {
                                    log("Таблица расписания загружена, начало обновления");
                                    parseTimetable(view);
                                }
                                else
                                {
                                    log("Не удалось загрузить таблицу расписания");
                                    sendEmptyTimetableData();
                                    hideProgressBar();
                                }
                            });
                        }, TIMEOUT_TABLE_LOAD);
                    }
                    else
                    {
                        handler.postDelayed(this, 500);
                    }
                });
            }
        };
        handler.post(checkButtonRunnable);
    }

    private void parseTimetable(WebView view)
    {
        startTime = System.currentTimeMillis();
        String parseAndGroupTable = "var timetableData = [];\n" +
                "var dayDivs = document.querySelectorAll('[data-v-470231ae].flex.md12');\n" +
                "\n" +
                "console.log('Количество блоков дней:', dayDivs.length);\n" +
                "\n" +
                "dayDivs.forEach(function(dayDiv, dayIndex) {\n" +
                "    console.log('Обработка блока дня:', dayIndex);\n" +
                "\n" +
                "    var dayName = dayDiv.querySelector('.v-toolbar__title:first-child')?.textContent.trim() || 'Неизвестно';\n" +
                "    var dayDate = dayDiv.querySelector('.v-toolbar__title:last-child')?.textContent.trim() || 'Неизвестно';\n" +
                "    console.log('  День:', dayName, 'Дата:', dayDate);\n" +
                "\n" +
                "    var lessonContainers = dayDiv.querySelectorAll('div[data-v-470231ae].mt-2');\n" +
                "    console.log('  Количество контейнеров уроков:', lessonContainers.length);\n" +
                "\n" +
                "    lessonContainers.forEach(function(lessonContainer, lessonIndex) {\n" +
                "        console.log('    Обработка контейнера урока:', lessonIndex);\n" +
                "        \n" +
                "       var timeDiv = lessonContainer.querySelector('.layout > span:first-child');\n" +
                "          var time = timeDiv ? timeDiv.textContent.trim() : 'Время не указано';\n" +
                "       console.log('     Время:', time);\n" +
                "       \n" +
                "        var lesson = lessonContainer.querySelector('.layout.mx-0');\n" +
                "        if (lesson) {\n" +
                "             var cardDiv = lesson.closest('.rasp-square');\n" +
                "           if (cardDiv) {\n" +
                "                console.log('      Есть карточка расписания');\n" +
                "                 var subjectSpan = lesson.querySelector('span[style*=\"font-weight: bold;\"]');\n" +
                "               var subjectName = subjectSpan ? subjectSpan.textContent.trim() : 'Название не указано';\n" +
                "               var lessonTypeMatch = subjectName.match(/^(Лек|Лаб|Пр)/);\n" +
                "               var lessonType = lessonTypeMatch ? lessonTypeMatch[0] : 'Неизвестно';\n" +
                "              var cleanedSubjectName = subjectName.replace(/^(Лек|Лаб|Пр)\\\\s*/, '').trim();\n" +
                "               console.log('     Название предмета:', cleanedSubjectName, 'Тип:', lessonType);\n" +
                "\n" +
                "               var teacherIcon = cardDiv.querySelector('span[style*=\"color: grey;\"] i.fas.fa-user-tie');\n" +
                "                var teacherName = teacherIcon ? teacherIcon.parentElement.textContent.trim() : 'Преподаватель не указан';\n" +
                "               console.log('     Преподаватель:', teacherName);\n" +
                "\n" +
                "               var audIcon = cardDiv.querySelector('span[style*=\"color: grey;\"] i.fas.fa-map-marker-alt');\n" +
                "                var audName = audIcon ? audIcon.parentElement.textContent.trim() : 'Аудитория не указана';\n" +
                "               console.log('     Аудитория:', audName);\n" +
                "\n" +
                "               timetableData.push({\n" +
                "                   dayName: dayName,\n" +
                "                   dayDate: dayDate,\n" +
                "                   time: time,\n" +
                "                    subject: cleanedSubjectName,\n" +
                "                    type: lessonType,\n" +
                "                   teacher: teacherName,\n" +
                "                   auditory: audName\n" +
                "               });\n" +
                "                console.log('    Урок добавлен в timetableData');\n" +
                "           }else{\n" +
                "               console.log('      Нет карточки расписания');\n" +
                "           }\n" +
                "        }else{\n" +
                "             console.log('     Блок урока не найден');\n" +
                "        }\n" +
                "    });\n" +
                "});\n" +
                "\n" +
                "console.log('Итоговый результат:', JSON.stringify(timetableData));\n" +
                "\n" +
                "JSON.stringify(timetableData);";

        view.evaluateJavascript(parseAndGroupTable, value ->
        {
            if (value == null || value.equals("null") || value.isEmpty())
            {
                log("Данные расписания не получены");
                hideProgressBar();
                sendEmptyTimetableData();

                return;
            }

            try
            {
                String cleanedJsonString = String.valueOf(value);
                if (cleanedJsonString.startsWith("\"") && cleanedJsonString.endsWith("\"")) {
                    cleanedJsonString = cleanedJsonString.substring(1, cleanedJsonString.length() - 1);
                }

                if (!cleanedJsonString.startsWith("[") || !cleanedJsonString.endsWith("]"))
                {
                    log("Ошибка. JSON не является массивом.");
                    hideProgressBar();
                    sendEmptyTimetableData();

                    return;
                }

                cleanedJsonString = cleanedJsonString.replaceAll("\\\\\"", "\"").replaceAll("\\\\\\\\", "\\\\");
                JSONArray jsonArray = new JSONArray(cleanedJsonString);

                Map<String, List<List<String>>> parsedTimetableData = new HashMap<>();

                if (jsonArray.length() > 0)
                {
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject lessonObject = jsonArray.getJSONObject(i);
                        String dayName = lessonObject.getString("dayName");
                        String dayDate = lessonObject.getString("dayDate");
                        String time = lessonObject.getString("time");
                        String subject = lessonObject.getString("subject");
                        String type = lessonObject.getString("type");
                        String teacher = lessonObject.getString("teacher");
                        String auditory = lessonObject.getString("auditory");

                        List<String> lesson = new ArrayList<>();
                        lesson.add(dayName);
                        lesson.add(dayDate);
                        lesson.add(time);
                        lesson.add(subject);
                        lesson.add(type);
                        lesson.add(teacher);
                        lesson.add(auditory);

                        if (parsedTimetableData.containsKey(dayDate))
                        {
                            parsedTimetableData.get(dayDate).add(lesson);
                        }
                        else
                        {
                            List<List<String>> lessonsForDay = new ArrayList<>();
                            lessonsForDay.add(lesson);
                            parsedTimetableData.put(dayDate, lessonsForDay);
                        }
                    }
                }
                else
                {
                    List<String> emptyList = new ArrayList<>();
                    emptyList.add("Empty");
                    String dayDate = DATE_FORMAT_FOR_FRAGMENT.format(Calendar.getInstance().getTime());
                    List<List<String>> empty = new ArrayList<>();
                    empty.add(emptyList);
                    parsedTimetableData.put(dayDate, empty);
                }

                if (getActivity() == null)
                {
                    hideProgressBar();
                    return;
                }

                pagesLoaded++;

                if (pagesLoaded == TOTAL_PAGES)
                {
                    saveTimetableData(parsedTimetableData);
                    allTimetableData = parsedTimetableData;
                    updateViewPager();

                    long endTime = System.currentTimeMillis();
                    long parseTime = endTime - startTime;
                    log("Обновление завершено успешно, время: "+parseTime + "мс");

                    hideProgressBar();
                }
            }
            catch (JSONException e)
            {
                log("Ошибка JSON при обновлении: " + e.getMessage());
                Log.e("----------","Ошибка JSON при обновлении: " + e.getMessage());
                e.printStackTrace();

                hideProgressBar();
            }
        });
    }

    private void sendEmptyTimetableData()
    {
        if (getActivity() == null)
        {
            hideProgressBar();
            return;
        }

        Map<String, List<List<String>>> emptyData = new HashMap<>();
        List<String> emptyList = new ArrayList<>();
        emptyList.add("Empty");

        for (int i = 0; i < 7; i++)
        {
            Calendar calendar = (Calendar) selectedCalendar.clone();
            int selectedDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int daysOffset = selectedDayOfWeek - Calendar.MONDAY;
            calendar.add(Calendar.DAY_OF_WEEK, -daysOffset);
            calendar.add(Calendar.DAY_OF_WEEK, i);

            if (selectedDayOfWeek == Calendar.SUNDAY)
            {
                calendar.add(Calendar.DAY_OF_WEEK, -7);
            }

            String formattedDate = DATE_FORMAT_FOR_FRAGMENT.format(calendar.getTime());
            List<List<String>> empty = new ArrayList<>();
            empty.add(emptyList);
            emptyData.put(formattedDate, empty);
        }

        saveTimetableData(emptyData);
        allTimetableData = emptyData;
        updateViewPager();
        hideProgressBar();
    }

    private void saveTimetableData(Map<String, List<List<String>>> timetableData)
    {
        if (isAdded() && getContext() != null)
        {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(TIMETABLE_DATA_PREF, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String jsonTimetable = new Gson().toJson(timetableData);
            editor.putString("timetable-list", jsonTimetable);
            editor.putLong(getParseTimeKey(), System.currentTimeMillis());
            editor.apply();
        }
    }

    private void loadSavedTimetableData()
    {
        if (isAdded() && getContext() != null)
        {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(TIMETABLE_DATA_PREF, Context.MODE_PRIVATE);
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
                        updateViewPager();
                    }
                }
                catch (Exception ignored) {}
            }
        }
    }

    private void updateViewPager()
    {
        if (getActivity() != null)
        {
            FragmentStatePagerAdapter adapter = (FragmentStatePagerAdapter) view_pager_timetable.getAdapter();

            if (adapter != null)
            {
                adapter.notifyDataSetChanged();

                for (int i = 0; i < tab_layout_timetable.getTabCount(); i++)
                {
                    TabLayout.Tab tab = tab_layout_timetable.getTabAt(i);

                    if (tab != null)
                    {
                        setTabTitle(tab, i, selectedCalendar);
                    }
                }
                updateBackgroundForCurrentDayTab();
            }
        }
    }

    private void setTabTitle(TabLayout.Tab tab, int position, Calendar selectedDayCalendar)
    {
        Calendar calendar = (Calendar) selectedDayCalendar.clone();
        int selectedDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysOffset = selectedDayOfWeek - Calendar.MONDAY;

        calendar.add(Calendar.DAY_OF_WEEK, -daysOffset);
        calendar.add(Calendar.DAY_OF_WEEK, position);

        if (selectedDayOfWeek == Calendar.SUNDAY)
        {
            calendar.add(Calendar.DAY_OF_WEEK, -7);
        }
        tab.setText(DATE_FORMAT.format(calendar.getTime()));
    }

    private void setupViewPagerWithInitialData()
    {
        if (getActivity() == null)
        {
            return;
        }

        FragmentStatePagerAdapter pagerAdapter = new FragmentStatePagerAdapter(getChildFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position)
            {
                return createDayFragment(position);
            }

            @Override
            public int getCount()
            {
                return 7;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position)
            {
                return null;
            }

            @Override
            public int getItemPosition(@NonNull Object object)
            {
                return POSITION_NONE;
            }
        };

        view_pager_timetable.setAdapter(pagerAdapter);
        tab_layout_timetable.setupWithViewPager(view_pager_timetable);
        view_pager_timetable.setOffscreenPageLimit(tab_layout_timetable.getTabCount());
        int currentDayOfWeek = selectedCalendar.get(Calendar.DAY_OF_WEEK);
        int initialTabIndex = getInitialTabIndex(currentDayOfWeek);
        view_pager_timetable.setCurrentItem(initialTabIndex);

        for (int i = 0; i < tab_layout_timetable.getTabCount(); i++)
        {
            TabLayout.Tab tab = tab_layout_timetable.getTabAt(i);

            if (tab != null)
            {
                setTabTitle(tab, i, selectedCalendar);
            }
        }
        updateBackgroundForCurrentDayTab();
    }

    private int getInitialTabIndex(int currentDayOfWeek)
    {
        if (currentDayOfWeek == Calendar.SUNDAY)
        {
            return Calendar.SATURDAY + 1 - Calendar.MONDAY;
        }

        return currentDayOfWeek - Calendar.MONDAY;
    }

    private Fragment createDayFragment(int position)
    {
        Calendar calendar = (Calendar) selectedCalendar.clone();
        int selectedDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysOffset = selectedDayOfWeek - Calendar.MONDAY;

        calendar.add(Calendar.DAY_OF_WEEK, -daysOffset);
        calendar.add(Calendar.DAY_OF_WEEK, position);

        if (selectedDayOfWeek == Calendar.SUNDAY)
        {
            calendar.add(Calendar.DAY_OF_WEEK, -7);
        }

        String formattedDate = DATE_FORMAT_FOR_FRAGMENT.format(calendar.getTime());
        List<List<String>> timetableForDay = allTimetableData.get(formattedDate);

        if (timetableForDay == null)
        {
            timetableForDay = new ArrayList<>();
        }

        return Time_table_tab_layout_fragment.newInstance(formattedDate, flatten(timetableForDay));
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

    private void setupTabLayout()
    {
        for (int i = 0; i < tab_layout_timetable.getTabCount(); i++)
        {
            TabLayout.Tab tab = tab_layout_timetable.getTabAt(i);

            if (tab != null)
            {
                tab.setText(getWeekTab(i, selectedCalendar));
            }
        }
        updateBackgroundForCurrentDayTab();
    }

    private void updateBackgroundForCurrentDayTab()
    {
        Calendar calendar = Calendar.getInstance();
        String todayFormatted = DATE_FORMAT.format(calendar.getTime());

        for (int i = 0; i < tab_layout_timetable.getTabCount(); i++)
        {
            TabLayout.Tab tab = tab_layout_timetable.getTabAt(i);

            if (tab != null && tab.getText() != null)
            {
                if (tab.getText().toString().equals(todayFormatted))
                {
                    View tabView = tab.view;
                    tabView.setBackgroundResource(R.drawable.tab_layout_background_form_2);
                }
                else
                {
                    if (tab.view != null)
                    {
                        tab.view.setBackgroundResource(android.R.color.transparent);
                    }

                }
            }
        }
    }

    private String getWeekTab(int position, Calendar selectedDayCalendar)
    {
        Calendar calendar = (Calendar) selectedDayCalendar.clone();
        int selectedDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysOffset = selectedDayOfWeek - Calendar.MONDAY;

        calendar.add(Calendar.DAY_OF_WEEK, -daysOffset);
        calendar.add(Calendar.DAY_OF_WEEK, position);

        if (selectedDayOfWeek == Calendar.SUNDAY)
        {
            calendar.add(Calendar.DAY_OF_WEEK, -7);
        }

        return DATE_FORMAT.format(calendar.getTime());
    }

    private void loadDate()
    {
        String dayMonth = DAY_FORMAT.format(currentDate) + " " + CURRENT_DATE_FORMAT.format(currentDate);
        day_week.setText(dayMonth);

        String evenText = EVEN_FORMAT.format(currentDate);
        int even = Integer.parseInt(evenText);
        String weekTypeText = (even % 2 == 0) ? "чётная**" : "нечётная*";
        even_uneven.setText(weekTypeText);
    }

    private void setTabBackground(TabLayout.Tab tab, int backgroundResource)
    {
        View customView = tab.getCustomView();

        if (customView != null)
        {
            customView.setBackgroundResource(backgroundResource);
        }
    }

    private void setupDayWeekClick()
    {
        day_week.setOnClickListener(view -> selectDateCalendar());
    }

    private void setBackgroundActiveTab()
    {
        tab_layout_timetable.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                setTabBackground(tab, R.drawable.tab_layout_background_form_1);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {
                setTabBackground(tab, android.R.color.transparent);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void selectDateCalendar()
    {
        Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(getContext(),
                R.style.DatePickerTheme, (datePicker, selectedYear, selectedMonth, selectedDay) ->
        {
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
            setupViewPagerWithInitialData();
            //clearLog();
            loadWebsiteTimeTable();
            saveCalendarClicked();
            first_press_fragment_timetable.setVisibility(View.GONE);
            String selectedWeekTypeText = (selectedCalendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) ? "чётная**" : "нечётная*";
            even_uneven.setText(selectedWeekTypeText);
            updateBackgroundForCurrentDayTab();
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private String lastParseTime()
    {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(TIMETABLE_DATA_PREF, Context.MODE_PRIVATE);
        long lastParseTime = sharedPreferences.getLong(getParseTimeKey(), 0);

        if (lastParseTime == 0)
        {
            return "Время последнего обновления: нет данных";
        }
        else
        {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            String formattedTime = sdf.format(new Date(lastParseTime));

            return "Время последнего обновления: " + formattedTime;
        }
    }

    private void log(String message)
    {
        if (getActivity() != null)
        {
            getActivity().runOnUiThread(() ->
            {
                boolean logMode = settingsPrefs.getBoolean(LOG_KEY, false);
                if(logMode)
                {
                    if(logTextView.getVisibility() == View.GONE)
                    {
                        logTextView.setVisibility(View.VISIBLE);
                    }

                    String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    String logText = logTextView.getText() + "\n" + currentTime + " - " + message;
                    logTextView.setText(logText);
                }
            });
        }
    }

    private void clearLog()
    {
        if (getActivity() != null)
        {
            getActivity().runOnUiThread(() -> logTextView.setText(""));
        }
    }

    private void showLog()
    {
        if (getActivity() != null)
        {
            getActivity().runOnUiThread(() ->
            {
                boolean logMode = settingsPrefs.getBoolean(LOG_KEY, false);

                if(logMode && logTextView.getVisibility() == View.GONE)
                {
                    logTextView.setVisibility(View.VISIBLE);
                }
                else if(!logMode && logTextView.getVisibility() == View.VISIBLE)
                {
                    logTextView.setVisibility(View.GONE);
                }
            });
        }
    }

    private void showProgressBar()
    {
        if (getActivity() != null)
        {
            getActivity().runOnUiThread(() -> progressBar_timetable.setVisibility(View.VISIBLE));
        }
    }

    private void hideProgressBar()
    {
        if (getActivity() != null)
        {
            getActivity().runOnUiThread(() -> progressBar_timetable.setVisibility(View.GONE));
        }
    }

    private void showNoInternetView()
    {
        if (getActivity() != null)
        {
            getActivity().runOnUiThread(() -> no_internet_fragment_timetable.setVisibility(View.VISIBLE));
        }
    }

    private void hideNoInternetView()
    {
        if (getActivity() != null)
        {
            getActivity().runOnUiThread(() -> no_internet_fragment_timetable.setVisibility(View.GONE));
        }
    }

    private void checkFirstCalendarClick()
    {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(TIMETABLE_DATA_PREF, Context.MODE_PRIVATE);

        if (!sharedPreferences.getBoolean(CALENDAR_CLICKED_PREF, false))
        {
            first_press_fragment_timetable.setVisibility(View.VISIBLE);
        }
    }

    private void setupToolbarSubtitle()
    {
        if (GroupName != null && !GroupName.isEmpty())
        {
            timetable_toolbar_subtitle.setVisibility(View.VISIBLE);
            timetable_toolbar_subtitle.setText(GroupName.replace("\\\\*", "/"));
        }
    }

    private void saveCalendarClicked()
    {
        if (isAdded() && getContext() != null)
        {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(TIMETABLE_DATA_PREF, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(CALENDAR_CLICKED_PREF, true);
            editor.apply();
        }
    }

    private boolean isTimetableParseNeeded()
    {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(TIMETABLE_DATA_PREF, Context.MODE_PRIVATE);
        long lastParseTime = sharedPreferences.getLong(getParseTimeKey(), 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastParseTime) > TimeUnit.HOURS.toMillis(12);
    }

    private String getParseTimeKey()
    {
        return LAST_PARSE_TIME_KEY_TIMETABLE + "_" + GroupName;
    }

    private boolean isInternetAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }

        return false;
    }
}