package com.example.timetable.fragments_staff;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Staff extends Fragment
{
    View view;

    // Переменные для работы с RecyclerView и адаптером
    RecyclerView recyclerview_staff_list;
    RecyclerViewAdapterStaff recyclerViewAdapterStaff;

    LinearLayout no_internet_staff, LinearLayout_search_staff_list;

    EditText edit_text_search_staff;
    ImageView button_search_staff;

    TextView no_personal_staff;

    private SpinKitView progressBar;

    Button reload_staff;

    Toolbar toolbar_staff;

    //передача названия институтадля парсинга
    static public String url = "";
    static public String union = "";

    // Таймер для проверки интернета
    private Timer internetCheckTimer;

    private static final String URL_NEFU = "https://www.s-vfu.ru/";
    private static final String EMPTY = "Empty_URL";
    private static final String ERROR_TIMEOUT_CONNECT = "Произошла ошибка: Тайм-аут сетевого соединения";
    private static final String ERROR = "Произошла ошибка: ";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.staff, container, false);

        // Настройка отображения списка сотрудников
        recyclerview_staff_list = view.findViewById(R.id.recyclerview_staff_list);
        recyclerview_staff_list.setLayoutManager(new LinearLayoutManager(getContext()));

        //если нет инета
        no_internet_staff = view.findViewById(R.id.no_internet_staff);

        //нет такого сотрудника
        no_personal_staff = view.findViewById(R.id.no_personal_staff);

        //фильтр
        LinearLayout_search_staff_list = view.findViewById(R.id.LinearLayout_search_staff_list);

        //найти сотрудника
        edit_text_search_staff = view.findViewById(R.id.edit_text_search_staff);
        button_search_staff = view.findViewById(R.id.button_search_staff);

        //плитки в ряд
        recyclerview_staff_list.setLayoutManager(new GridLayoutManager(getContext(), 2));
        // Создание адаптера для списка сотрудников
        recyclerViewAdapterStaff = new RecyclerViewAdapterStaff(new ArrayList<>());
        recyclerview_staff_list.setAdapter(recyclerViewAdapterStaff);

        progressBar = view.findViewById(R.id.progress_bar_staff);

        //перезагрузить
        reload_staff = view.findViewById(R.id.reload_staff);

        // Устанавливаем название института в Toolbar
        toolbar_staff = view.findViewById(R.id.toolbar_staff);
        toolbar_staff.setTitle(union);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_staff);
        // Добавление кнопки "Назад"
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Обработчик нажатия на кнопку "Назад"
        toolbar_staff.setNavigationOnClickListener(v ->
        {
            // Вызвать метод возврата на предыдущий фрагмент
            getActivity().onBackPressed();
        });

        // Проверяем доступность интернета
        if (!isInternetAvailable())
        {
            no_internet_staff.setVisibility(View.VISIBLE);

            // Запускаем таймер проверки интернета
            startInternetCheckTimer();
        }
        else
        {
            progressBar.setVisibility(View.VISIBLE);

            // Выполнение асинхронной задачи для загрузки данных сотрудников
            new LoadWebsiteStaff("").execute();
        }

        //кнопка поиска сотрудника
        button_search_staff.setOnClickListener(view ->
        {
            progressBar.setVisibility(View.VISIBLE);

            staffSearch();
        });

        //кнопка поиска сотрудника enter
        edit_text_search_staff.setOnEditorActionListener((v, actionId, event) ->
        {
            progressBar.setVisibility(View.VISIBLE);
            no_personal_staff.setVisibility(View.GONE);

            staffSearch();

            return false;
        });

        //нажатие на кнопку перезагрузки
        reload_staff.setOnClickListener(v ->
        {
            reload_staff.setVisibility(View.GONE);
            no_personal_staff.setVisibility(View.GONE);

            progressBar.setVisibility(View.VISIBLE);

            // Выполнение асинхронной задачи для загрузки данных сотрудников
            new LoadWebsiteStaff("").execute();
        });

        return view;
    }

    // Внутренний класс для выполнения асинхронной задачи по загрузке данных
    private class LoadWebsiteStaff extends AsyncTask<Void, Class_staff, Void>
    {
        private final String searchText;

        @Override
        protected void onPreExecute()
        {
            // Выполняется перед началом выполнения фоновой задачи (UI-подготовка)
            super.onPreExecute();
        }

        public LoadWebsiteStaff(String searchText)
        {
            this.searchText = searchText.toLowerCase();
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            // Создание списка для хранения данных сотрудников
            String photoUrl, name, position, url_lk;
            boolean no_staff = true;

            try
            {
                Document document = Jsoup.connect(url).timeout(20000).get();

                if (document != null)
                {
                    try
                    {
                        Elements staffElements = document.select(".col-lg-3.col-sm-6.g-mb-30");

                        for (Element staffElement : staffElements)
                        {
                            photoUrl = URL_NEFU + staffElement.select(".w-100.g-min-height-200").attr("src");
                            name = staffElement.select(".g-color-black.g-text-underline--none--hover").text();
                            position = staffElement.select(".d-block.u-info-v6-1__item.g-font-style-normal.g-font-size-11.text-uppercase.g-color-primary").text();

                            if (name.equals(""))
                            {
                                String name_empty = staffElement.select(".g-bg-white.g-pa-15").text();
                                String position_empty = staffElement.select(".d-block.u-info-v6-1__item.g-font-style-normal.g-font-size-11.text-uppercase.g-color-primary").text();
                                name = name_empty.replace(position_empty, "");
                            }
                            url_lk = URL_NEFU + staffElement.select(".g-color-black.g-text-underline--none--hover").attr("href");
                            url_lk = url_lk.replace("s-vfu.ru//staff/", "s-vfu.ru/staff/");

                            if (url_lk.equals(URL_NEFU))
                            {
                                url_lk = EMPTY;
                            }

                            if (!searchText.equals(""))
                            {
                                if (name.toLowerCase().contains(searchText) || position.toLowerCase().contains(searchText))
                                {
                                    Class_staff class_staff = new Class_staff(name, position, url_lk, photoUrl);
                                    publishProgress(class_staff);

                                    //если есть хоть 1 сотрудник
                                    no_staff = false;
                                }
                            }
                            else
                            {
                                Class_staff class_staff = new Class_staff(name, position, url_lk, photoUrl);
                                publishProgress(class_staff);
                                no_staff = false;
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
                no_staff = false;
            }
            catch (IOException e)
            {
                // Другие исключения
                showError(ERROR + e.getMessage());
                e.printStackTrace();
                no_staff = false;
            }

            if (no_staff)
            {
                getActivity().runOnUiThread(() -> no_personal_staff.setVisibility(View.VISIBLE));
            }

            return null;
        }

        // Метод для отображения сообщением об ошибке
        private void showError(String message)
        {
            if (getActivity() != null)
            {
                getActivity().runOnUiThread(() ->
                {
                    getActivity().runOnUiThread(() -> reload_staff.setVisibility(View.VISIBLE));

                    View rootView = getActivity().findViewById(android.R.id.content);
                    Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
                });
            }
        }

        @Override
        protected void onProgressUpdate(Class_staff... values)
        {
            Class_staff staff = values[0];

            recyclerViewAdapterStaff.addItem(staff);
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            // Скрытие ProgressBar после выполнения задачи
            progressBar.setVisibility(View.GONE);

            LinearLayout_search_staff_list.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onCancelled()
        {
            getActivity().runOnUiThread(() -> reload_staff.setVisibility(View.VISIBLE));
        }
    }

    //поиск по фильту
    private void staffSearch()
    {
        // Уберите фокус с edit
        edit_text_search_staff.clearFocus();

        // Очистить адаптер перед новой загрузкой данных
        recyclerViewAdapterStaff.clear();

        String searchText = edit_text_search_staff.getText().toString().trim();

        new LoadWebsiteStaff(searchText).execute();

        //скрыть клавиатуру
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
                    getActivity().runOnUiThread(() -> no_internet_staff.setVisibility(View.GONE));

                    // Отменяем таймер
                    stopInternetCheckTimer();

                    // Запускаем LoadWebsiteUnion
                    new LoadWebsiteStaff("").execute();
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