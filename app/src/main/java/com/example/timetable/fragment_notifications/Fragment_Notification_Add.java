package com.example.timetable.fragment_notifications;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import androidx.fragment.app.Fragment;

import com.example.timetable.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;

public class Fragment_Notification_Add extends Fragment
{
    View view;
    EditText edit_name_notifications_add, edit_time_notifications_add, edit_description_notifications_add, edit_url_notifications_add;
    LinearLayout linear_layout_edit_notifications_add, no_internet_notifications_add;
    ImageView button_save_notifications_add, button_back_notifications_add;
    TextView text_name_notifications_add;

    DatabaseReference databaseReference, databaseReference_notifications_add;
    SharedPreferences sharedPreferences_load;

    private Timer internetCheckTimer;
    private SpinKitView progressbar;

    String Union, Name, role, select_notifications, verification_notification;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // Надувает view из layout-файла
        view = inflater.inflate(R.layout.fragment_notification_add, container, false);

        // Инициализирует UI-элементы
        initViews();

        // Загружаем роль пользователя из SharedPreferences
        role = sharedPreferences("select_role", "Role");

        // Определяет подразделение и имя пользователя в зависимости от роли
        if (role.equals("Студент"))
        {
            Union = sharedPreferences("select_group", "GroupUnion");
            Name = sharedPreferences("select_group", "GroupName");
        }
        else if (role.equals("Преподаватель"))
        {
            Union = sharedPreferences("select_teacher_name", "Union");
            Name = sharedPreferences("select_teacher_name", "Name");
        }

        // Преобразует полное название подразделения в аббревиатуру
        switch (Union)
        {
            case "Физико-технический институт":
                Union = "ФТИ";
                break;
            case "Финансово-экономический институт":
                Union = "ФЭИ";
                break;
            case "Горный институт":
                Union = "ГИ";
                break;
            case "Инженерно-технический институт":
                Union = "ИТИ";
                break;
            case "Институт зарубежной филологии и регионоведения":
                Union = "ИЗФиР";
                break;
            case "Институт естественных наук":
                Union = "ИЕН";
                break;
            case "Институт математики и информатики":
                Union = "ИМИ";
                break;
            case "Институт психологии":
                Union = "ИП";
                break;
            case "Институт физической культуры и спорта":
                Union = "ИФКиС";
                break;
            case "Институт языков и культуры народов Северо-Востока РФ":
                Union = "ИЯКН СВ РФ";
                break;
            case "Медицинский институт":
                Union = "МИ";
                break;
            case "Педагогический институт":
                Union = "ПИ";
                break;
            case "Автодорожный факультет":
                Union = "АДФ";
                break;
            case "Геологоразведочный факультет":
                Union = "ГРФ";
                break;
            case "Исторический факультет":
                Union = "ИФ";
                break;
            case "Филологический факультет":
                Union = "ФЛФ";
                break;
            case "Юридический факультет":
                Union = "ЮФ";
                break;
            case "Юридический колледж":
                Union = "ЮК";
                break;
            case "Колледж инфраструктурных технологий СВФУ":
                Union = "КИТ";
                break;
            case "Чукотский филиал СВФУ г.Анадырь":
                Union = "Чукотский филиал";
                break;
            case "Технический институт (филиал) СВФУ в г. Нерюнгри":
                Union = "Филиал г.Нерюнгри";
                break;
            case "Политехнический институт (филиал) СВФУ в г. Мирном":
                Union = "Филиал г.Мирный";
                break;
        }

        // Получает аргумент из Bundle
        Bundle args = getArguments();
        if (args != null && args.containsKey("select_notifications"))
        {
            select_notifications = args.getString("select_notifications");
        }

        // Устанавливает заголовок для фрагмента
        text_name_notifications_add.setText(select_notifications);
        // Инициализирует базу данных Firebase
        databaseReference = FirebaseDatabase.getInstance("https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Подразделения");

        // Определяет ссылку на нужный раздел базы данных
        switch (select_notifications)
        {
            case "Для группы":
                databaseReference_notifications_add = databaseReference.child(Union).child(Name).child("Объявления");
                verification_notification = "1";
                break;
            case "Для подразделения":
                databaseReference_notifications_add = databaseReference.child(Union).child("Объявления");
                verification_notification = "0";
                break;
            case "Для всех":
                databaseReference_notifications_add = databaseReference.child("Объявления");
                verification_notification = "0";
                break;
        }

        // Проверяем наличие интернета
        if (!isInternetAvailable())
        {
            no_internet_notifications_add.setVisibility(View.VISIBLE);
            startInternetCheckTimer();
        }

        // Нажатия на кнопку сохранения
        button_save_notifications_add.setOnClickListener(v ->
        {
            if (isInternetAvailable())
            {
                progressbar.setVisibility(View.VISIBLE);
                showSave(view.getContext());
            }
            else
            {
                showSnackbar("Ошибка. Отсутствует подключение к сети");
            }
        });

        // Нажатие на кнопку "назад"
        button_back_notifications_add.setOnClickListener(v -> getActivity().onBackPressed());

        // Нажатие на линейный макет, фокусируем и открываем клавиатуру
        linear_layout_edit_notifications_add.setOnClickListener(v ->
        {
            edit_description_notifications_add.requestFocus();
            edit_description_notifications_add.setSelection(edit_description_notifications_add.getText().length());

            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edit_description_notifications_add, InputMethodManager.SHOW_IMPLICIT);
        });

        return view;
    }

    private void initViews()
    {
        edit_name_notifications_add = view.findViewById(R.id.edit_name_notifications_add);
        edit_time_notifications_add = view.findViewById(R.id.edit_time_notifications_add);
        edit_description_notifications_add = view.findViewById(R.id.edit_description_notifications_add);
        edit_url_notifications_add = view.findViewById(R.id.edit_url_notifications_add);
        linear_layout_edit_notifications_add = view.findViewById(R.id.linear_layout_edit_notifications_add);
        text_name_notifications_add = view.findViewById(R.id.text_name_notifications_add);
        button_save_notifications_add = view.findViewById(R.id.button_save_notifications_add);
        button_back_notifications_add = view.findViewById(R.id.button_back_notifications_add);
        progressbar = view.findViewById(R.id.progress_bar_notifications_add);
        no_internet_notifications_add = view.findViewById(R.id.no_internet_notifications_add);
    }

    private String sharedPreferences(String name, String key)
    {
        sharedPreferences_load = requireContext().getSharedPreferences(name, MODE_PRIVATE);

        return sharedPreferences_load.getString(key, null);
    }

    // Отображения диалогового окна подтверждения
    private void showSave(Context context)
    {
        clearFocusAndHideKeyboard();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null);
        builder.setView(dialogView);

        // Инициализирует элементы диалогового окна
        TextView title_alert_dialog = dialogView.findViewById(R.id.title_alert_dialog);
        TextView message_alert_dialog = dialogView.findViewById(R.id.message_alert_dialog);
        Button positive_button_alert_dialog = dialogView.findViewById(R.id.positive_button_alert_dialog);
        Button negative_button_alert_dialog = dialogView.findViewById(R.id.negative_button_alert_dialog);

        title_alert_dialog.setText("Новое объявление");
        message_alert_dialog.setText("Добавить новое объявление?");
        positive_button_alert_dialog.setText("Да");
        negative_button_alert_dialog.setText("Нет");

        AlertDialog dialog = builder.create();
        // Нажатия на кнопку "Да"
        positive_button_alert_dialog.setOnClickListener(v ->
        {
            addNotificationToFirebase();
            dialog.dismiss();
        });
        // Нажатия на кнопку "Нет"
        negative_button_alert_dialog.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Добавления уведомления в Firebase
    private void addNotificationToFirebase()
    {
        String name = edit_name_notifications_add.getText().toString().trim();
        String time = edit_time_notifications_add.getText().toString().trim();
        String url = edit_url_notifications_add.getText().toString().trim();
        String description = edit_description_notifications_add.getText().toString().trim();

        if (!name.isEmpty() && !description.isEmpty())
        {
            String notificationId = generateIDNotifications();

            databaseReference_notifications_add.child(notificationId).child("Заголовок").setValue(name);
            databaseReference_notifications_add.child(notificationId).child("Время").setValue(time);
            databaseReference_notifications_add.child(notificationId).child("Ссылка").setValue(url);
            databaseReference_notifications_add.child(notificationId).child("Описание").setValue(description);
            databaseReference_notifications_add.child(notificationId).child("Проверка").setValue(verification_notification);
            // Очищает поля ввода
            edit_name_notifications_add.setText("");
            edit_time_notifications_add.setText("");
            edit_url_notifications_add.setText("");
            edit_description_notifications_add.setText("");

            // Показывает сообщение об успехе
            showSnackbar("Объявление добавлено");
            progressbar.setVisibility(View.GONE);
        }
        else
        {
            // Показывает сообщение об ошибке
            showSnackbar("Ошибка. Заголовок или описание пустые");
            progressbar.setVisibility(View.GONE);
        }
    }

    // Скрытие клавиатуры и снятия фокуса с полей
    private void clearFocusAndHideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        edit_name_notifications_add.clearFocus();
        edit_time_notifications_add.clearFocus();
        edit_url_notifications_add.clearFocus();
        edit_description_notifications_add.clearFocus();
    }

    // Отображение Snackbar сообщения
    private void showSnackbar(String message)
    {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    // Генерация уникального ID уведомления
    private String generateIDNotifications()
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("SSSssmmHHddMMyy", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    // Отменяет таймер при уничтожении view
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        stopInternetCheckTimer();
    }

    // Запускает таймер проверки интернета
    private void startInternetCheckTimer()
    {
        internetCheckTimer = new Timer();
        internetCheckTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                if (isInternetAvailable())
                {
                    getActivity().runOnUiThread(() -> no_internet_notifications_add.setVisibility(View.GONE));
                    stopInternetCheckTimer();
                }
            }
        }, 0, 2000); // Запускаем каждые 2 секунды
    }

    // Останавливает таймер проверки интернета
    private void stopInternetCheckTimer()
    {
        if (internetCheckTimer != null)
        {
            internetCheckTimer.cancel();
            internetCheckTimer = null;
        }
    }

    // Функция для проверки доступности интернета
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