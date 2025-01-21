package com.example.timetable.fragments_bottom_navigation;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;
import com.example.timetable.fragment_notifications.Class_NotificationItem;
import com.example.timetable.fragment_notifications.Fragment_Select_Notifications;
import com.example.timetable.fragment_notifications.NotificationAdapter;
import com.example.timetable.fragment_notifications.Notifications;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;

public class Fragment_notifications extends Fragment
{
    View view;

    //парсинг
    RecyclerView recyclerview_fragment_notification;
    NotificationAdapter notificationAdapter;
    List<Class_NotificationItem> notificationList = new ArrayList<>();

    ImageView fab_add_fragment_notification;

    DatabaseReference databaseReference_notifications;

    //загрузка
    SharedPreferences sharedPreferences_load_student, sharedPreferences_load_teacher, sharedPreferences_load_post, sharedPreferences_load_role;
    String Union, Name, userPost, role;

    private SpinKitView progressBar;
    LinearLayout no_internet_fragment_notification, first_window_fragment_notification;
    TextView text_no_fragment_notification;

    // Таймер для проверки интернета
    private Timer internetCheckTimer;

    private static final String FIREBASE_URL = "https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String UNIONS = "Подразделения";
    private static final String NOTIFICATION = "Объявления";
    private static final String GROUP = "Группа";
    private static final String UNION = "Подразделение";
    private static final String ALL = "Все";
    private static final String BOSS = "Владелец";
    private static final String ADMIN = "Администратор";
    private static final String MODER = "Модератор";
    private static final String TITLE = "Заголовок";
    private static final String TIME = "Время";
    private static final String URL = "Ссылка";
    private static final String DESCRIPTION = "Описание";
    private static final String CHECK = "Проверка";
    private static final String STUDENT = "Студент";
    private static final String TEACHER = "Преподаватель";

    //меняются
    private static final String GROUP_NOT_SELECT = "Группа не выбрана \nвыберите свою группу в настройках";
    private static final String ERROR_SELECT_GROUP = "Ошибка. Выберите свою группу в настройках";
    private static final String NO_NOTIFICATION = "Объявлений нет";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerview_fragment_notification = view.findViewById(R.id.recyclerview_fragment_notification);

        fab_add_fragment_notification = view.findViewById(R.id.fab_add_fragment_notification);

        progressBar = view.findViewById(R.id.progress_bar_fragment_notification);
        no_internet_fragment_notification = view.findViewById(R.id.no_internet_fragment_notification);
        first_window_fragment_notification = view.findViewById(R.id.first_window_fragment_notification);
        text_no_fragment_notification = view.findViewById(R.id.text_no_fragment_notification);

        //Загрузка из памяти роли пользователя
        sharedPreferences_load_role = requireContext().getSharedPreferences("select_role", MODE_PRIVATE);
        role = sharedPreferences_load_role.getString("Role", null);

        if (role.equals(STUDENT))
        {
            // Загрузка из памяти название группы и института
            sharedPreferences_load_student = requireContext().getSharedPreferences("select_group", Context.MODE_PRIVATE);
            Union = sharedPreferences_load_student.getString("GroupUnion", null);
            Name = sharedPreferences_load_student.getString("GroupName", null);
        }
        else if (role.equals(TEACHER))
        {
            // Загрузка из памяти название института и ФИО
            sharedPreferences_load_teacher = requireContext().getSharedPreferences("select_teacher_name", MODE_PRIVATE);
            Union = sharedPreferences_load_teacher.getString("Union", null);
            Name = sharedPreferences_load_teacher.getString("Name", null);

            //замена института для препода
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
        }

        // Загрузка из памяти должности пользователя
        sharedPreferences_load_post = requireContext().getSharedPreferences("select_post", Context.MODE_PRIVATE);
        userPost = sharedPreferences_load_post.getString("Post", null);

        // Проверка на null перед созданием адаптера
        if (requireContext() != null)
        {
            notificationAdapter = new NotificationAdapter(requireContext(), notificationList);
            recyclerview_fragment_notification.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerview_fragment_notification.setAdapter(notificationAdapter);
        }

        setNotificationClickListener();

        // Проверка на выбрана ли группа
        if (Union != null && Name != null)
        {
            try
            {
                // Инициализация Firebase Realtime Database
                databaseReference_notifications = FirebaseDatabase.getInstance(FIREBASE_URL).getReference(UNIONS);

                // Проверяем доступность интернета
                if (!isInternetAvailable())
                {
                    no_internet_fragment_notification.setVisibility(View.VISIBLE);

                    // Запускаем таймер проверки интернета
                    startInternetCheckTimer();
                }
                else
                {
                    progressBar.setVisibility(View.VISIBLE);

                    notificationList.clear();

                    // Проверка наличия новых объявлений в Firebase Realtime Database
                    databaseReference_notifications.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            progressBar.setVisibility(View.GONE);
                            notificationList.clear();

                            // Проверка наличия новых объявлений
                            boolean hasNewNotifications = checkNotifications(dataSnapshot);

                            if (hasNewNotifications)
                            {
                                fireBase_load_notifications();
                            }
                            else
                            {
                                if (notificationList.isEmpty())
                                {
                                    first_window_fragment_notification.setVisibility(View.VISIBLE);
                                    text_no_fragment_notification.setText(NO_NOTIFICATION);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            first_window_fragment_notification.setVisibility(View.VISIBLE);
            text_no_fragment_notification.setText(GROUP_NOT_SELECT);
        }

        //нажатие на кнопку добавления обьявления
        fab_add_fragment_notification.setOnClickListener(v ->
        {
            // Проверка на выбрана ли группа для добавления группы
            if (Union != null && Name != null)
            {
                add_notification();
            }
            else
            {
                if (view != null)
                {
                    Snackbar snackbar = Snackbar.make(view, ERROR_SELECT_GROUP, Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
        });

        return view;
    }

    //нажатие на обьявление
    private void setNotificationClickListener()
    {
        notificationAdapter.setOnNotificationClickListener((verification, id, name, time, url, type, description) ->
        {
            // Создайте новый фрагмент и передайте в него данные
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            Notifications newFragment = new Notifications();

            Bundle args = new Bundle();
            args.putString("verification", verification);
            args.putString("id", id);
            args.putString("name", name);
            args.putString("time", time);
            args.putString("url", url);
            args.putString("type", type);
            args.putString("description", description);
            newFragment.setArguments(args);

            // Открыть новый фрагмент
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    //загрузка из FireBase обьявлений
    public void fireBase_load_notifications()
    {
        // Очистка список уведомлений перед каждой загрузкой
        notificationList.clear();

        try
        {
            //Фильтр для студента и препода
            if (role.equals(STUDENT))
            {
                //для группы
                databaseReference_notifications.child(Union).child(Name).child(NOTIFICATION).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        addNotificationsFromSnapshot(dataSnapshot, GROUP);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
            //для подразделения
            databaseReference_notifications.child(Union).child(NOTIFICATION).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    addNotificationsFromSnapshot(dataSnapshot, UNION);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
            //для всех
            databaseReference_notifications.child(NOTIFICATION).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    addNotificationsFromSnapshot(dataSnapshot, ALL);
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Метод для добавления уведомлений
    private void addNotificationsFromSnapshot(DataSnapshot dataSnapshot, String type)
    {
        List<Class_NotificationItem> updatedNotifications = new ArrayList<>();

        for (DataSnapshot notificationSnapshot : dataSnapshot.getChildren())
        {
            String UID = notificationSnapshot.getKey();
            String name = notificationSnapshot.child(TITLE).getValue(String.class);
            String time = notificationSnapshot.child(TIME).getValue(String.class);
            String url = notificationSnapshot.child(URL).getValue(String.class);
            String description = notificationSnapshot.child(DESCRIPTION).getValue(String.class);
            String verification = notificationSnapshot.child(CHECK).getValue(String.class);

            if ((verification != null && verification.equals("1")) || userPost.equals(BOSS) || userPost.equals(ADMIN) || userPost.equals(MODER))
            {
                for (Class_NotificationItem existingNotification : notificationList)
                {
                    if (existingNotification.getId().equals(UID))
                    {
                        break;
                    }
                }

                Class_NotificationItem notification = new Class_NotificationItem(verification, UID, type, name, time, url, description);

                notificationList.add(notification);

                // Добавляем уведомление в список, который будет обновлен
                updatedNotifications.add(notification);

                //если нет обьявлений показать
                //if (verification.isEmpty())
                //{
                //    first_window_fragment_notification.setVisibility(View.VISIBLE);
                //    text_no_fragment_notification.setText(NO_NOTIFICATION);
                //}
            }
        }

        notificationAdapter.notifyDataSetChanged();
    }

    //проверка наличия обьявления
    private boolean checkNotifications(DataSnapshot dataSnapshot)
    {
        boolean hasNewNotifications = false;

        // Проверка наличие новых объявлений в ветке для группы
        if (dataSnapshot.child(Union).child(Name).child(NOTIFICATION).exists())
        { hasNewNotifications = true; }

        // Проверка наличие новых объявлений в ветке для подразделения
        if (dataSnapshot.child(Union).child(NOTIFICATION).exists())
        { hasNewNotifications = true; }

        // Проверка наличие новых объявлений в общей ветке
        if (dataSnapshot.child(NOTIFICATION).exists())
        { hasNewNotifications = true; }

        return hasNewNotifications;
    }

    //метод добавления обьявления
    public void add_notification()
    {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        Fragment_Select_Notifications fragment = new Fragment_Select_Notifications();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //отмена таймера при уничтожении представления
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

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
                try
                {
                    // Проверяем доступность интернета
                    if (isInternetAvailable())
                    {
                        getActivity().runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
                        getActivity().runOnUiThread(() -> no_internet_fragment_notification.setVisibility(View.GONE));

                        fireBase_load_notifications();

                        // Отменяем таймер
                        stopInternetCheckTimer();
                    }
                }
                catch (Exception e)
                {
                    // Обработка ошибки при проверке интернета
                    e.printStackTrace();
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
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            return networkInfo != null && networkInfo.isConnected();
        }

        return false;
    }
}