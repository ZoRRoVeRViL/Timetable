package com.example.timetable.fragments_bottom_navigation;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;
import com.example.timetable.fragments_staff.RecyclerViewAdapterUnionStaff;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Fragment_staff extends Fragment
{
    View view;

    // Переменные для работы с RecyclerView и адаптером
    RecyclerView recyclerview_staff_union;
    RecyclerViewAdapterUnionStaff recyclerViewAdapterUnionStaff;

    //загрузка анимация
    private SpinKitView progressBar;

    Button reload_fragment_staff;

    //если нет инета
    LinearLayout no_internet_fragment_staff;

    //адресс сайта для парсинга
    String url = "https://www.s-vfu.ru/universitet/rukovodstvo-i-struktura/instituty/";

    // Таймер для проверки интернета
    private Timer internetCheckTimer;

    private static final String CENTER_NEFU = "Центры СВФУ";
    private static final String LABORATORY_NEFU = "Лаборатории СВФУ";
    private static final String URL_NEFU = "https://www.s-vfu.ru//universitet/nauka/nauchnye-instituty-i-tsentry/staff";

    //меняются
    private static final String ERROR_TIMEOUT_CONNECT = "Произошла ошибка: Тайм-аут сетевого соединения";
    private static final String ERROR = "Произошла ошибка: ";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // Заполнение макета фрагмента
        view = inflater.inflate(R.layout.fragment_staff, container, false);

        //если нет инета
        no_internet_fragment_staff = view.findViewById(R.id.no_internet_fragment_staff);

        // Создание адаптера для списка сотрудников
        recyclerview_staff_union = view.findViewById(R.id.recyclerview_staff_union);
        recyclerview_staff_union.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAdapterUnionStaff = new RecyclerViewAdapterUnionStaff();
        recyclerview_staff_union.setAdapter(recyclerViewAdapterUnionStaff);

        //загрузка анимация
        progressBar = view.findViewById(R.id.progress_bar_fragment_staff);

        //перезагрузить
        reload_fragment_staff = view.findViewById(R.id.reload_fragment_staff);

        // Проверяем доступность интернета
        if (!isInternetAvailable())
        {
            no_internet_fragment_staff.setVisibility(View.VISIBLE);

            // Запускаем таймер проверки интернета
            startInternetCheckTimer();
        }
        else
        {
            progressBar.setVisibility(View.VISIBLE);
            // Выполнение асинхронной задачи для загрузки данных сотрудников
            new LoadWebsiteUnionStaff().execute();
        }

        //нажатие на кнопку перезагрузки
        reload_fragment_staff.setOnClickListener(v ->
        {
            reload_fragment_staff.setVisibility(View.GONE);

            progressBar.setVisibility(View.VISIBLE);

            new LoadWebsiteUnionStaff().execute();
        });

        return view;
    }

    // Внутренний класс для выполнения асинхронной задачи по загрузке данных
    private class LoadWebsiteUnionStaff extends AsyncTask<Void, Void,  List<String>>
    {
        @Override
        protected void onPreExecute()
        {
            // Выполняется перед началом выполнения фоновой задачи (UI-подготовка)
            super.onPreExecute();
        }

        @Override
        protected List<String> doInBackground(Void... voids)
        {
            // Создание списка для хранения данных сотрудников
            List<String> union_staffList = new ArrayList<>();

            try
            {
                Document document = Jsoup.connect(url).timeout(20000).get();

                if (document != null)
                {
                    // Перебор индексов от 1 до 10 для парсинга данных
                    for (int i = 1; i <= 10; i++)
                    {
                        String headingId = "accordion-heading-" + String.format("%02d", i);
                        String bodyId = "accordion-body-" + String.format("%02d", i);

                        Element headingElement = document.getElementById(headingId);
                        Element bodyElement = document.getElementById(bodyId);

                        //парсинг списка
                        if (headingElement != null && bodyElement != null)
                        {
                            String currentHeader = headingElement.text();
                            union_staffList.add(currentHeader);
                            union_staffList.add("https");

                            //парсинг подсписка
                            Elements tableRows = bodyElement.select("tbody tr");
                            for (Element row : tableRows)
                            {
                                Elements columns = row.select("td");

                                List<String> rowData = new ArrayList<>();

                                for (Element column : columns)
                                {
                                    String columnText = column.text();
                                    //добавить в список органы
                                    rowData.add(columnText);

                                    // Извлечение ссылки из атрибута href
                                    String href = column.select("a[href]").attr("href");
                                    rowData.add("https://www.s-vfu.ru/" + href + "/staff");
                                    //удалить лишнюю ссылку
                                    rowData.removeIf(link -> link.contains(URL_NEFU));
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
                    getActivity().runOnUiThread(() -> reload_fragment_staff.setVisibility(View.VISIBLE));
                    View rootView = getActivity().findViewById(android.R.id.content);
                    Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
                });
            }
        }

        @Override
        protected void onPostExecute(List<String> union_staffItemList)
        {
            if (union_staffItemList != null && !union_staffItemList.isEmpty())
            {
                // Создание и установка адаптера после получения данных
                recyclerViewAdapterUnionStaff.RecyclerViewAdapterUnionStaff(union_staffItemList);
            }
            else
            {
                // Если произошла ошибка показатье кнопку reload_fragment_staff
                reload_fragment_staff.setVisibility(View.VISIBLE);
            }

            // Скрытие ProgressBar после выполнения задачи
            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled()
        {
            getActivity().runOnUiThread(() -> reload_fragment_staff.setVisibility(View.VISIBLE));
        }
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
                    getActivity().runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
                    getActivity().runOnUiThread(() -> no_internet_fragment_staff.setVisibility(View.GONE));

                    // Отменяем таймер
                    stopInternetCheckTimer();

                    // Запускаем LoadWebsiteUnion
                    new LoadWebsiteUnionStaff().execute();
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