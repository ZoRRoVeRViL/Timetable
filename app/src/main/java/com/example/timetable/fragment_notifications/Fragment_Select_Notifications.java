package com.example.timetable.fragment_notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.timetable.R;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;

public class Fragment_Select_Notifications extends Fragment
{
    // Объявляются переменные для работы с UI
    private View view;
    private ImageView button_back_select_notifications;
    private LinearLayout no_internet_select_notifications, group_select_notifications, institute_select_notifications, nefu_select_notifications;
    private View view_group_select_notifications;
    private TextView text_no_notification;

    // Объявляется таймер для проверки интернета
    private Timer internetCheckTimer;

    // SharedPreferences для загрузки роли пользователя
    private SharedPreferences sharedPreferences_load_role;
    private String role;

    // Константы для текста, который зависит от роли пользователя
    private static final String TEXT_TEACHER = "\n" +
            "        В данном разделе существуют два вида объявлений:\n" +
            "        - Для подразделения: их могут разместить все, кто учится или работает в вашем институте, факультете и т.д. Такие объявления видны только тем, кто также связан с этим институтом, факультетом и т.д.\n" +
            "        - Для всех: их видят все, у кого установлено данное приложение.\n" +
            "        Объявления для всех и для подразделения проходят проверку, чтобы избежать лишнего. Для групп нет модерации.\n" +
            "        Надеюсь, что вы будете соблюдать уважение к другим подразделениям и всем пользователям, особенно в группах без модерации.\n" +
            "        Чтобы получить возможность проверять и модерировать объявления, напишите мне в разделе Настройки -> Поддержка.";

    private static final String TEXT_STUDENT = "\n" +
            "        В данном разделе существуют три вида объявлений:\n" +
            "        - Для группы: их могут разместить только те, кто входит в вашу группу, и видят их только те, кто выбрал вашу группу.\n" +
            "        - Для подразделения: их могут разместить все, кто учится или работает в вашем институте, факультете и т.д. Такие объявления видны только тем, кто также связан с этим институтом, факультетом и т.д.\n" +
            "        - Для всех: их видят все, у кого установлено данное приложение.\n" +
            "        Объявления для всех и для подразделения проходят проверку, чтобы избежать лишнего. Для групп нет модерации.\n" +
            "        Надеюсь, что вы будете соблюдать уважение к другим подразделениям и всем пользователям, особенно в группах без модерации.\n" +
            "        Чтобы получить возможность проверять и модерировать объявления, напишите мне в разделе Настройки -> Поддержка.";


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // Надувается view из layout-файла
        view = inflater.inflate(R.layout.fragment_select_notifications, container, false);

        // Находятся все необходимые View элементы
        initViews();

        // Загружается роль пользователя
        loadUserRole();

        // Настраиваются UI элементы на основе роли
        setupUIBasedOnRole();

        // Устанавливается обработчик для кнопки "назад"
        button_back_select_notifications.setOnClickListener(v -> requireActivity().onBackPressed());

        // Проверяется наличие интернета
        checkInternetAndHandleUI();

        // Устанавливаются обработчики нажатий на кнопки
        group_select_notifications.setOnClickListener(v -> openNotificationAddFragment("Для группы"));
        institute_select_notifications.setOnClickListener(v -> openNotificationAddFragment("Для подразделения"));
        nefu_select_notifications.setOnClickListener(v -> openNotificationAddFragment("Для всех"));

        return view;
    }

    private void initViews()
    {
        button_back_select_notifications = view.findViewById(R.id.button_back_select_notifications);
        group_select_notifications = view.findViewById(R.id.group_select_notifications);
        institute_select_notifications = view.findViewById(R.id.institute_select_notifications);
        nefu_select_notifications = view.findViewById(R.id.nefu_select_notifications);
        view_group_select_notifications = view.findViewById(R.id.View_group_select_notifications);
        text_no_notification = view.findViewById(R.id.text_no_notification);
        no_internet_select_notifications = view.findViewById(R.id.no_internet_select_notifications);
    }

    // Функция загружает роль пользователя из SharedPreferences
    private void loadUserRole()
    {
        sharedPreferences_load_role = requireContext().getSharedPreferences("select_role", MODE_PRIVATE);
        role = sharedPreferences_load_role.getString("Role", null);
    }

    // Функция настраивает видимость UI элементов в зависимости от роли пользователя
    private void setupUIBasedOnRole() {
        if (role != null)
        {
            if (role.equals("Преподаватель"))
            {
                group_select_notifications.setVisibility(View.GONE);
                view_group_select_notifications.setVisibility(View.GONE);
                text_no_notification.setText(TEXT_TEACHER);
            }
            else if (role.equals("Студент"))
            {
                text_no_notification.setText(TEXT_STUDENT);
            }
        }
    }

    // Функция проверяет интернет и отображает/скрывает сообщение о его отсутствии
    private void checkInternetAndHandleUI()
    {
        if (!isInternetAvailable())
        {
            no_internet_select_notifications.setVisibility(View.VISIBLE);
            startInternetCheckTimer();
        }
    }

    // Функция для открытия фрагмента добавления уведомления
    private void openNotificationAddFragment(String source)
    {
        // Получается FragmentManager
        FragmentActivity activity = (FragmentActivity) view.getContext();
        if (activity == null)
        {
            return;
        }

        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        Fragment_Notification_Add fragment = new Fragment_Notification_Add();

        // Создается Bundle для передачи данных
        Bundle args = new Bundle();
        args.putString("select_notifications", source);
        fragment.setArguments(args);

        // Заменяется текущий фрагмент на новый
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        // Останавливается таймер при уничтожении фрагмента
        stopInternetCheckTimer();
    }

    // Запускается таймер проверки интернета
    private void startInternetCheckTimer()
    {
        internetCheckTimer = new Timer();
        internetCheckTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                // Проверяется доступность интернета
                if (isInternetAvailable())
                {
                    if (getActivity() != null)
                    {
                        getActivity().runOnUiThread(() -> no_internet_select_notifications.setVisibility(View.GONE));
                    }
                    stopInternetCheckTimer();  // Останавливается таймер
                }
            }
        }, 0, 2000); // Запускается каждые 2 секунды
    }

    // Функция для остановки таймера проверки интернета
    private void stopInternetCheckTimer()
    {
        if (internetCheckTimer != null)
        {
            internetCheckTimer.cancel();
            internetCheckTimer = null;
        }
    }

    // Функция для проверки наличия интернета
    private boolean isInternetAvailable()
    {
        if (isAdded())
        {
            ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null)
            {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                return networkInfo != null && networkInfo.isConnected();
            }
        }

        return false;
    }
}