package com.example.timetable.fragments_settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

public class Setting_union_teacher extends Fragment
{
    View view;

    //парсинг
    RecyclerView recyclerview_unions;
    RecyclerViewAdapterUnion recyclerViewAdapterUnion;

    //нет интернета
    LinearLayout no_internet_setting_union, small_no_internet_setting_union;

    //загрузка анимация
    private SpinKitView progressBar;

    Toolbar toolbar_setting_union;

    //адрес сайта
    String url = "https://www.s-vfu.ru/universitet/rukovodstvo-i-struktura/instituty/";

    // Таймер для проверки интернета
    private Timer internetCheckTimer;

    List<String> savedUnionList;

    private static final String CENTER_NEFU = "Центры СВФУ";
    private static final String LABORATORY_NEFU = "Лаборатории СВФУ";
    private static final String ERROR_TIMEOUT_CONNECT = "Произошла ошибка: Тайм-аут сетевого соединения";
    private static final String ERROR = "Произошла ошибка: ";
    private static final String URL_NEFU = "https://www.s-vfu.ru//universitet/nauka/nauchnye-instituty-i-tsentry/staff";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.setting_union, container, false);

        //отображение инститтутов
        recyclerview_unions = view.findViewById(R.id.recyclerview_setting_union);
        recyclerview_unions.setLayoutManager(new LinearLayoutManager(getContext()));

        //нет интернета
        no_internet_setting_union = view.findViewById(R.id.no_internet_setting_union);
        small_no_internet_setting_union = view.findViewById(R.id.small_no_internet_setting_union);

        //создаем recyclerViewAdapterUnion
        recyclerViewAdapterUnion = new RecyclerViewAdapterUnion();
        recyclerview_unions.setAdapter(recyclerViewAdapterUnion);

        //загрузка анимация
        progressBar = view.findViewById(R.id.progress_bar_setting_union);

        // Устанавливаем название института в Toolbar
        toolbar_setting_union = view.findViewById(R.id.toolbar_setting_union);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_setting_union);
        // Добавление кнопки "Назад"
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Обработчик нажатия на кнопку "Назад"
        toolbar_setting_union.setNavigationOnClickListener(v -> getActivity().onBackPressed());

        //проверка наличия раннего сохранения
        savedUnionList = loadUnionList();
        if (savedUnionList != null && !savedUnionList.isEmpty())
        {
            recyclerViewAdapterUnion.RecyclerViewAdapterUnion(savedUnionList);

            // Проверяем доступность интернета если есть
            if (isInternetAvailable())
            {
                new LoadWebsiteUnion().execute();
            }
            else
            {
                small_no_internet_setting_union.setVisibility(View.VISIBLE);

                // Запускаем таймер проверки интернета
                startInternetCheckTimer();
            }
        }
        else
        {
            // Проверяем доступность интернета если нет
            if (!isInternetAvailable())
            {
                no_internet_setting_union.setVisibility(View.VISIBLE);

                // Запускаем таймер проверки интернета
                startInternetCheckTimer();
            }
            else
            {
                progressBar.setVisibility(View.VISIBLE);

                new LoadWebsiteUnion().execute();
            }
        }

        return view;
    }

    private class LoadWebsiteUnion extends AsyncTask<Void, Void, List<String>>
    {
        @Override
        protected void onPreExecute()
        {
            //В этом методе код перед началом выполнения фонового процесса
            super.onPreExecute();

            //Иконка нет интернета
            getActivity().runOnUiThread(() -> no_internet_setting_union.setVisibility(View.GONE));
            getActivity().runOnUiThread(() -> small_no_internet_setting_union.setVisibility(View.GONE));
        }

        @Override
        protected List<String> doInBackground(Void... voids)
        {
            List<String> union_staffList = new ArrayList<>();

            System.out.println(url+"     urlurlurlurlurlurlurlurl");
            try
            {
                // Коннектимся и получаем страницу
                Document document = Jsoup.connect(url).get();

                // Перебор индексов от 1 до 10 для парсинга данных
                for (int i = 1; i <= 10; i++)
                {
                    String bodyId = "accordion-body-" + String.format("%02d", i);

                    Element bodyElement = document.getElementById(bodyId);

                    //парсинг списка
                    if (bodyElement != null)
                    {
                        //парсинг подсписка
                        Elements tableRows = bodyElement.select("tbody tr");
                        for (Element row : tableRows)
                        {
                            Elements columns = row.select("td");

                            List<String> rowData = new ArrayList<>();

                            for (Element column : columns)
                            {
                                String columnText = column.text();

                                // Извлечение ссылки из атрибута href
                                String href = column.select("a[href]").attr("href");
                                rowData.add(columnText + "==https://www.s-vfu.ru/" + href + "/staff");
                                //удалить лишнюю ссылку
                                rowData.removeIf(link -> link.contains(URL_NEFU));
                                System.out.println(columnText + "==https://www.s-vfu.ru/" + href + "/staff" + "*************");
                            }

                            // Проверка на "Центры СВФУ" и "Лаборатории СВФУ"
                            if (!rowData.contains(CENTER_NEFU) && !rowData.contains(LABORATORY_NEFU))
                            {
                                union_staffList.addAll(rowData);
                            }
                        }
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

            return union_staffList;
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
        protected void onPostExecute(List<String> unionItemList)
        {
            if (unionItemList != null && !unionItemList.isEmpty())
            {
                // Скрытие ProgressBar после выполнения задачи
                progressBar.setVisibility(View.GONE);

                recyclerViewAdapterUnion.RecyclerViewAdapterUnion(unionItemList);

                //сохраняем список
                saveUnionList(unionItemList);
            }
        }
    }

    //сохранение списка
    private void saveUnionList(List<String> unionList)
    {
        try
        {
            if (isAdded())
            {
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("Setting-union-teacher", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Преобразование списка в строку JSON
                Gson gson = new Gson();
                String jsonList = gson.toJson(unionList);

                editor.putString("union-list", jsonList);
                editor.apply();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //загрузка списка
    private List<String> loadUnionList()
    {
        try
        {
            if (isAdded())
            {
                try
                {
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("Setting-union-teacher", Context.MODE_PRIVATE);
                    String jsonList = sharedPreferences.getString("union-list", null);

                    // Преобразование строки JSON в список
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<String>>()
                    {}.getType();

                    return gson.fromJson(jsonList, type);
                }
                catch (Exception e)
                {
                    e.printStackTrace();

                    return null;
                }
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

                    if (savedUnionList == null && savedUnionList.isEmpty())
                    {
                        getActivity().runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
                    }

                    // Запускаем LoadWebsiteUnion
                    new LoadWebsiteUnion().execute();

                    // Отменяем таймер
                    stopInternetCheckTimer();
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            return networkInfo != null && networkInfo.isConnected();
        }

        return false;
    }
}