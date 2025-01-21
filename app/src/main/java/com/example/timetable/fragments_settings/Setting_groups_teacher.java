package com.example.timetable.fragments_settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Setting_groups_teacher extends Fragment
{
    View view;

    //парсинг
    RecyclerView recyclerview_groups;
    RecyclerViewAdapterGroupsTeacher recyclerViewAdapterGroupsTeacher;

    //нет интернета
    LinearLayout no_internet_setting_group, small_no_internet_setting_group, LinearLayout_search_setting_group;

    //Поиск
    EditText edit_text_search_setting_group;
    ImageView button_search_setting_group;
    TextView no_personal_setting_group;

    Toolbar toolbar_setting_groups;

    //загрузка анимация
    private SpinKitView progressBar;

    //передача названия институтадля парсинга
    static public String URL = "";
    static public String UNION = "";

    // Таймер для проверки интернета
    private Timer internetCheckTimer;

    List<String> savedGroupList;

    //если парсинг медленный
    Handler timeout = new Handler();

    private static final String ERROR_TIMEOUT_CONNECT = "Произошла ошибка: Тайм-аут сетевого соединения";
    private static final String ERROR = "Произошла ошибка: ";
    private static final String NO_NAME = "Такого преподователя в данном подразделении нет";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.setting_groups, container, false);

        //отображение групп
        recyclerview_groups = view.findViewById(R.id.recyclerview_setting_groups);
        recyclerview_groups.setLayoutManager(new LinearLayoutManager(getContext()));

        //Поиск
        LinearLayout_search_setting_group = view.findViewById(R.id.LinearLayout_search_setting_group);
        edit_text_search_setting_group = view.findViewById(R.id.edit_text_search_setting_group);
        no_personal_setting_group = view.findViewById(R.id.no_personal_setting_group);
        button_search_setting_group = view.findViewById(R.id.button_search_setting_group);

        //создаем recyclerViewAdapterUnion
        recyclerViewAdapterGroupsTeacher = new RecyclerViewAdapterGroupsTeacher();
        recyclerview_groups.setAdapter(recyclerViewAdapterGroupsTeacher);

        //загрузка анимация
        progressBar = view.findViewById(R.id.progress_bar_setting_groups);

        // Устанавливаем название института в Toolbar
        toolbar_setting_groups = view.findViewById(R.id.toolbar_setting_groups);
        toolbar_setting_groups.setTitle(UNION);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_setting_groups);
        // Добавление кнопки "Назад"
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Обработчик нажатия на кнопку "Назад"
        toolbar_setting_groups.setNavigationOnClickListener(v -> getActivity().onBackPressed());

        //проверка наличия раннего сохранения
        savedGroupList = loadGroupList(UNION);
        if (savedGroupList != null && !savedGroupList.isEmpty())
        {
            recyclerViewAdapterGroupsTeacher.RecyclerViewAdapterGroupsTeacher(savedGroupList, UNION);

            // Проверяем доступность интернета если есть
            if (isInternetAvailable())
            {
                new LoadWebsiteGroups().execute();
            }
            else
            {
                small_no_internet_setting_group.setVisibility(View.VISIBLE);
                LinearLayout_search_setting_group.setVisibility(View.VISIBLE);

                // Запускаем таймер проверки интернета
                startInternetCheckTimer();
            }
        }
        else
        {
            // Проверяем доступность интернета
            if (!isInternetAvailable())
            {
                no_internet_setting_group.setVisibility(View.VISIBLE);

                // Запускаем таймер проверки интернета
                startInternetCheckTimer();
            }
            else
            {
                progressBar.setVisibility(View.VISIBLE);
                new LoadWebsiteGroups().execute();
            }
        }

        //кнопка поиска сотрудника
        button_search_setting_group.setOnClickListener(view -> staffSearch());

        //кнопка поиска сотрудника enter
        edit_text_search_setting_group.setOnEditorActionListener((v, actionId, event) ->
        {
            staffSearch();

            return false;
        });

        return view;
    }

    private class LoadWebsiteGroups extends AsyncTask<Void, Void,  List<String>>
    {
        // Создание списка для хранения данных сотрудников
        List<String> name = new ArrayList<>();

        @Override
        protected void onPreExecute()
        {
            //В этом методе код перед началом выполнения фонового процесса
            super.onPreExecute();

            //Иконка нет интернета
            getActivity().runOnUiThread(() -> no_internet_setting_group.setVisibility(View.GONE));
            getActivity().runOnUiThread(() -> small_no_internet_setting_group.setVisibility(View.GONE));

            if (savedGroupList != null && !savedGroupList.isEmpty())
            {
                // Устанавливаем таймаут в 5 секунд
                timeout.postDelayed(() -> getActivity().runOnUiThread(() -> LinearLayout_search_setting_group.setVisibility(View.VISIBLE)), 3000);
            }
        }

        @Override
        protected List<String> doInBackground(Void... voids)
        {
            try
            {
                // Коннектимся и получаем страницу
                Document document = Jsoup.connect(URL).get();

                if (document != null)
                {
                    try
                    {
                        Elements staffElements = document.select(".col-lg-3.col-sm-6.g-mb-30");

                        for (Element staffElement : staffElements)
                        {
                            String str_name = staffElement.select(".g-color-black.g-text-underline--none--hover").text();
                            if (!str_name.isEmpty())
                            {
                                name.add(str_name);
                                String url_str = staffElement.select(".g-color-black.g-text-underline--none--hover").attr("href");
                                url_str = url_str.replace("staff", "");
                                url_str = url_str.replace("/", "");
                                name.add(url_str);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            catch (SocketTimeoutException e)
            {
                // В случае ошибки SocketTimeoutException
                showError(ERROR_TIMEOUT_CONNECT);
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // Другие исключения
                showError(ERROR + e.getMessage());
                e.printStackTrace();
            }

            return name;
        }

        // Метод для отображения сообщением об ошибке
        private void showError(String message)
        {
            if (getActivity() != null)
            {
                getActivity().runOnUiThread(() ->
                {
                    View rootView = getActivity().findViewById(android.R.id.content);
                    Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
                });
            }
        }

        @Override
        protected void onPostExecute(List<String> name)
        {
            // Отменяем таймаут, так как задача завершена
            timeout.removeCallbacksAndMessages(null);

            LinearLayout_search_setting_group.setVisibility(View.VISIBLE);

            if (name != null && !name.isEmpty())
            {
                // Создание и установка адаптера после получения данных
                recyclerViewAdapterGroupsTeacher.RecyclerViewAdapterGroupsTeacher(name, UNION);

                // Сохранение списка в SharedPreferences
                saveGroupList(name, UNION);

                // Скрытие ProgressBar после выполнения задачи
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    //поиск по фильту
    private void staffSearch()
    {
        no_personal_setting_group.setVisibility(View.GONE);

        // Уберите фокус с edit
        edit_text_search_setting_group.clearFocus();

        String searchText = edit_text_search_setting_group.getText().toString().trim().toLowerCase();

        if (!searchText.isEmpty())
        {
            //проверка наличия раннего сохранения
            savedGroupList = loadGroupList(UNION);
            if (savedGroupList != null && !savedGroupList.isEmpty())
            {
                // Фильтруем сохраненный список по введенному тексту
                List<String> filteredList = new ArrayList<>();
                for (int i = 0; i < savedGroupList.size(); i += 1)
                {
                    String currentGroup = savedGroupList.get(i).toLowerCase();

                    if (currentGroup.contains(searchText))
                    {
                        filteredList.add(savedGroupList.get(i));
                        filteredList.add(savedGroupList.get(i + 1));
                    }
                }

                // Проверяем, есть ли отфильтрованные данные
                if (filteredList.isEmpty())
                {
                    recyclerViewAdapterGroupsTeacher.RecyclerViewAdapterGroupsTeacher(filteredList, UNION);

                    // Отображаем сообщение о том, что сотрудник не найден
                    no_personal_setting_group.setVisibility(View.VISIBLE);
                    no_personal_setting_group.setText(NO_NAME);
                }
                else
                {
                    // Отображаем только отфильтрованные данные
                    recyclerViewAdapterGroupsTeacher.RecyclerViewAdapterGroupsTeacher(filteredList, UNION);
                    no_personal_setting_group.setVisibility(View.GONE);
                }
            }
        }
        else
        {
            recyclerViewAdapterGroupsTeacher.RecyclerViewAdapterGroupsTeacher(savedGroupList, UNION);
        }

        //скрыть клавиатуру
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //сохранение списка
    private void saveGroupList(List<String> name, String Union)
    {
        try
        {
            if (isAdded())
            {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("Setting-group-teacher" + Union, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Преобразование списка в строку JSON
                Gson gson = new Gson();
                String jsonList = gson.toJson(name);

                editor.putString("group-list", jsonList);
                editor.apply();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //загрузка списка
    private List<String> loadGroupList(String Union)
    {
        try
        {
            if (isAdded())
            {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("Setting-group-teacher" + Union, Context.MODE_PRIVATE);
                String jsonList = sharedPreferences.getString("group-list", null);

                // Преобразование строки JSON в список
                Gson gson = new Gson();
                Type type = new TypeToken<List<String>>()
                {}.getType();

                return gson.fromJson(jsonList, type);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        // Отменяем таймер при уничтожении представления
        stopInternetCheckTimer();
    }

    // Запуск таймера проверки интернета
    private void startInternetCheckTimer()
    {
        internetCheckTimer = new Timer();
        internetCheckTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                // Проверяем доступность интернета
                if (isInternetAvailable())
                {
                    if (savedGroupList == null && savedGroupList.isEmpty())
                    {
                        getActivity().runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
                    }

                    // Отменяем таймер
                    stopInternetCheckTimer();

                    // Запускаем LoadWebsiteUnion
                    new LoadWebsiteGroups().execute();
                }
            }
        }, 0, 2000); // Запуск каждые 2 секунды
    }

    // Остановка таймера проверки интернета
    private void stopInternetCheckTimer()
    {
        if (internetCheckTimer != null)
        {
            internetCheckTimer.cancel();
            internetCheckTimer = null;
        }
    }

    //проверка наличия интернета
    private boolean isInternetAvailable()
    {
        if (isAdded())
        {
            ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager != null)
            {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                return networkInfo != null && networkInfo.isConnected();
            }

            return false;
        }

        return false;
    }
}