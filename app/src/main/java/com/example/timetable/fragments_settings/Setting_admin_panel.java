package com.example.timetable.fragments_settings;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.timetable.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Setting_admin_panel extends Fragment
{
    View view;

    Toolbar toolbar_setting_admin_panel;

    EditText version_admin_panel, count_parse_admin_panel, count_all_users_admin_panel,
            count_students_all_users_admin_panel, count_teachers_all_users_admin_panel,
            group_parse_teachers_all_users_admin_panel, count_delete_user_admin_panels;

    //загрузка анимация
    private SpinKitView progressBar;
    LinearLayout no_internet_setting_admin_panel;

    Button button_admin_panel;

    // Firebase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference_versions, databaseReferenceCountUsers, databaseReferenceCountStudent,
            databaseReferenceCountTeacher, databaseReferenceCountParse, databaseReferenceGroupParseTeacher,
            databaseReferenceStudents, databaseReferenceTeachers;

    // Таймер для проверки интернета
    private Timer internetCheckTimer;

    private static final String ERROR = "Ошибка";
    private static final String ERROR_LOAD = "Ошибка загрузки, заполните все поля";
    private static final String LOAD_STOP = "Загрузка отменена";
    private static final String ERROR_IN_LOAD_DATA = "Ошибка при загрузке данных";
    private static final String DATA_SAVE_ACCESS = "Данные успешно загружены";
    private static final String VERSION = "Приложение/Версия";
    private static final String COUNT_PARSE = "Приложение/Количество парсинга";
    private static final String COUNT_USERS = "Пользователи/Количество пользователей";
    private static final String COUNT_STUDENT = "Пользователи/Количество пользователей студентов";
    private static final String COUNT_TEACHER = "Пользователи/Количество пользователей преподавателей";
    private static final String GROUP_PARSE_TEACHER = "Приложение/Группа парсинга преподавателей";
    private static final String USERS = "Пользователи";
    private static final String DELETED = "Удалённые";
    private static final String STUDENT = "Студент";
    private static final String TEACHER = "Преподаватель";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.setting_admin_panel, container, false);

        // Устанавливаем название института в Toolbar
        toolbar_setting_admin_panel = view.findViewById(R.id.toolbar_setting_admin_panel);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_setting_admin_panel);
        // Добавление кнопки "Назад"
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Обработчик нажатия на кнопку "Назад"
        toolbar_setting_admin_panel.setNavigationOnClickListener(v -> getActivity().onBackPressed());

        //загрузка анимация
        progressBar = view.findViewById(R.id.progress_bar_setting_admin_panel);
        progressBar.setVisibility(View.VISIBLE);

        //Кнопка сохранения
        button_admin_panel = view.findViewById(R.id.button_admin_panel);

        version_admin_panel = view.findViewById(R.id.version_admin_panel);
        count_parse_admin_panel = view.findViewById(R.id.count_parse_admin_panel);

        count_all_users_admin_panel = view.findViewById(R.id.count_all_users_admin_panel);
        count_students_all_users_admin_panel = view.findViewById(R.id.count_students_all_users_admin_panel);
        count_teachers_all_users_admin_panel = view.findViewById(R.id.count_teachers_all_users_admin_panel);
        group_parse_teachers_all_users_admin_panel = view.findViewById(R.id.group_parse_teachers_all_users_admin_panel);
        count_delete_user_admin_panels = view.findViewById(R.id.count_delete_user_admin_panels);

        //нет интернета
        no_internet_setting_admin_panel = view.findViewById(R.id.no_internet_setting_admin_panel);

        // Инициализация DatabaseReference
        DatabaseReferences();

        // Проверка наличия интернета
        if (isInternetAvailable())
        {
            progressBar.setVisibility(View.VISIBLE);

            // Загрузка
            loadData();
        }
        else
        {
            startInternetCheckTimer();

            // Если нет интернета, показать noInternetAdminAl
            no_internet_setting_admin_panel.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            button_admin_panel.setVisibility(View.GONE);
        }

        button_admin_panel.setOnClickListener(v -> saveDataToFirebase());

        return view;
    }

    // Инициализация DatabaseReference
    private void DatabaseReferences()
    {
        try
        {
            // Инициализация DatabaseReference
            firebaseDatabase = FirebaseDatabase.getInstance("https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/");

            databaseReference_versions = firebaseDatabase.getReference(VERSION);
            databaseReferenceCountParse = firebaseDatabase.getReference(COUNT_PARSE);
            databaseReferenceCountUsers = firebaseDatabase.getReference(COUNT_USERS);
            databaseReferenceCountStudent = firebaseDatabase.getReference(COUNT_STUDENT);
            databaseReferenceGroupParseTeacher = firebaseDatabase.getReference(GROUP_PARSE_TEACHER);
            databaseReferenceCountTeacher = firebaseDatabase.getReference(COUNT_TEACHER);
            databaseReferenceStudents = firebaseDatabase.getReference(USERS).child(DELETED).child(STUDENT);
            databaseReferenceTeachers = firebaseDatabase.getReference(USERS).child(DELETED).child(TEACHER);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    int deletedUserCount = 0;
    private void loadData()
    {
        deletedUserCount = 0;
        try
        {
            //Версия приложения
            databaseReference_versions.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String firebaseVersion = dataSnapshot.getValue(String.class);

                    version_admin_panel.setText(firebaseVersion != null ? firebaseVersion : ERROR);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    progressBar.setVisibility(View.GONE);
                }
            });
            // Получение количества парсинга расписания в день
            databaseReferenceCountParse.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    Integer firebaseCountParse = dataSnapshot.getValue(Integer.class);

                    count_parse_admin_panel.setText(firebaseCountParse != null ? String.valueOf(firebaseCountParse) : ERROR);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    progressBar.setVisibility(View.GONE);
                }
            });
            //количество пользователей
            databaseReferenceCountUsers.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    Long countUsersValue = dataSnapshot.getValue(Long.class);

                    count_all_users_admin_panel.setText(countUsersValue != null ? String.valueOf(countUsersValue) : "0");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    progressBar.setVisibility(View.GONE);
                }
            });
            //количество студентов
            databaseReferenceCountStudent.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    Long countStudentValue = dataSnapshot.getValue(Long.class);

                    count_students_all_users_admin_panel.setText(countStudentValue != null ? String.valueOf(countStudentValue) : "0");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    progressBar.setVisibility(View.GONE);
                }
            });
            //количество преподов
            databaseReferenceCountTeacher.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    Long countTeacherValue = dataSnapshot.getValue(Long.class);

                    count_teachers_all_users_admin_panel.setText(countTeacherValue != null ? String.valueOf(countTeacherValue) : "0");

                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    progressBar.setVisibility(View.GONE);
                }
            });
            //группа парсинга преподов
            databaseReferenceGroupParseTeacher.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String countTeacherValue = dataSnapshot.getValue(String.class);

                    group_parse_teachers_all_users_admin_panel.setText(countTeacherValue != null ? countTeacherValue : ERROR);

                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    progressBar.setVisibility(View.GONE);
                }
            });
            //студенты
            databaseReferenceStudents.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    for (DataSnapshot studentSnapshot : dataSnapshot.getChildren())
                    {
                        deletedUserCount++;
                    }

                    count_delete_user_admin_panels.setText(String.valueOf(deletedUserCount));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    progressBar.setVisibility(View.GONE);
                }
            });
            //препода
            databaseReferenceTeachers.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    for (DataSnapshot teacherSnapshot : dataSnapshot.getChildren())
                    {
                        deletedUserCount++;
                    }

                    count_delete_user_admin_panels.setText(String.valueOf(deletedUserCount));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //сохранение изменений
    private void saveDataToFirebase()
    {
        clearFocusAndHideKeyboard();

        String version = version_admin_panel.getText().toString();
        String countParse = count_parse_admin_panel.getText().toString();
        String countAllUsers = count_all_users_admin_panel.getText().toString();
        String countStudents = count_students_all_users_admin_panel.getText().toString();
        String countTeachers = count_teachers_all_users_admin_panel.getText().toString();
        String groupParseTeachers = group_parse_teachers_all_users_admin_panel.getText().toString();

        if (!version.isEmpty() && !countParse.isEmpty() && !countAllUsers.isEmpty() && !countStudents.isEmpty() && !countTeachers.isEmpty() && !groupParseTeachers.isEmpty())
        {
            //объект для сохранения данных
            Map<String, Object> data = new HashMap<>();
            data.put(VERSION, version);
            data.put(COUNT_PARSE, Integer.parseInt(countParse));
            data.put(COUNT_USERS, Long.parseLong(countAllUsers));
            data.put(COUNT_STUDENT, Long.parseLong(countStudents));
            data.put(COUNT_TEACHER, Long.parseLong(countTeachers));
            data.put(GROUP_PARSE_TEACHER, groupParseTeachers);

            // ссылка на базу данных
            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

            // Обновление данные в базе данных
            databaseReference.updateChildren(data).addOnSuccessListener(aVoid ->
            {
                // Данные успешно сохранены
                showSnackbar(DATA_SAVE_ACCESS);
            }).addOnFailureListener(e ->
            {
                // Ошибка при сохранении данных
                showSnackbar(ERROR_IN_LOAD_DATA + e.getMessage());
            }).addOnCanceledListener(() ->
            {
                // Операция отменена
                showSnackbar(LOAD_STOP);
            });
        }
        else
        {
            // Ошибка пустые строки
            showSnackbar(ERROR_LOAD);
        }
    }

    //очистка фокуса
    private void clearFocusAndHideKeyboard()
    {
        version_admin_panel.clearFocus();
        count_parse_admin_panel.clearFocus();
        count_all_users_admin_panel.clearFocus();
        count_students_all_users_admin_panel.clearFocus();
        count_teachers_all_users_admin_panel.clearFocus();

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //уведомления
    private void showSnackbar(String message)
    {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
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
                    getActivity().runOnUiThread(() -> no_internet_setting_admin_panel.setVisibility(View.GONE));

                    // Отменяем таймер
                    stopInternetCheckTimer();

                    // Загрузка списка админов
                    loadData();
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