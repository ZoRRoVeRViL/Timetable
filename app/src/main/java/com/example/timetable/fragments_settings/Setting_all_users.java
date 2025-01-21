package com.example.timetable.fragments_settings;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Setting_all_users extends Fragment
{
    View view;

    Toolbar toolbar_setting_all_users;

    TextView count_all_users, count_students_all_users, count_teachers_all_users;

    RecyclerView recyclerview_setting_all_users_students, recyclerview_setting_all_users_teachers;

    //загрузка анимация
    private SpinKitView progressBar;
    LinearLayout no_internet_setting_all_users;

    // Firebase
    DatabaseReference databaseReferenceStudents, databaseReferenceTeachers, databaseReferenceCountUsers, databaseReferenceCountStudent, databaseReferenceCountTeacher;
    List<Class_users> usersStudentsList, usersTeachersList;
    UsersAdapter usersStudentsAdapter, usersTeachersAdapter;

    // Таймер для проверки интернета
    private Timer internetCheckTimer;

    private static final String USERS = "Пользователи";
    private static final String STUDENT = "Студент";
    private static final String GROUP = "Группа";
    private static final String USER = "Преподаватель";
    private static final String COUNT_USERS = "Количество пользователей";
    private static final String COUNT_STUDENTS = "Количество пользователей студентов";
    private static final String COUNT_TEACHERS = "Количество пользователей преподавателей";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.setting_all_users, container, false);

        // Устанавливаем название института в Toolbar
        toolbar_setting_all_users = view.findViewById(R.id.toolbar_setting_all_users);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_setting_all_users);
        // Добавление кнопки "Назад"
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Обработчик нажатия на кнопку "Назад"
        toolbar_setting_all_users.setNavigationOnClickListener(v ->
        {
            // Вызвать метод возврата на предыдущий фрагмент
            getActivity().onBackPressed();
        });

        //загрузка анимация
        progressBar = view.findViewById(R.id.progress_bar_setting_all_users);
        progressBar.setVisibility(View.VISIBLE);

        //инфо про кол-во пользователей
        count_all_users = view.findViewById(R.id.count_all_users);
        count_students_all_users = view.findViewById(R.id.count_students_all_users);
        count_teachers_all_users = view.findViewById(R.id.count_teachers_all_users);

        //показать всех польователей
        recyclerview_setting_all_users_students = view.findViewById(R.id.recyclerview_setting_all_users_students);
        recyclerview_setting_all_users_teachers = view.findViewById(R.id.recyclerview_setting_all_users_teachers);

        //нет интернета
        no_internet_setting_all_users = view.findViewById(R.id.no_internet_setting_all_users);

        // Инициализация RecyclerView и адаптеров
        initRecyclerViews();

        // Инициализация DatabaseReference
        initDatabaseReferences();

        // Проверка наличия интернета
        if (isInternetAvailable())
        {
            progressBar.setVisibility(View.VISIBLE);

            // Загрузка списка админов
            loadUsers();
        }
        else
        {
            startInternetCheckTimer();

            // Если нет интернета, показать noInternetAdminAl
            no_internet_setting_all_users.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        return view;
    }

    // Инициализация RecyclerView и адаптеров
    private void initRecyclerViews()
    {
        // Инициализация RecyclerView и адаптера студентов
        usersStudentsList = new ArrayList<>();
        usersStudentsAdapter = new UsersAdapter(usersStudentsList);
        recyclerview_setting_all_users_students.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerview_setting_all_users_students.setAdapter(usersStudentsAdapter);

        // Инициализация RecyclerView и адаптера преподов
        usersTeachersList = new ArrayList<>();
        usersTeachersAdapter = new UsersAdapter(usersTeachersList);
        recyclerview_setting_all_users_teachers.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerview_setting_all_users_teachers.setAdapter(usersTeachersAdapter);
    }

    // Инициализация DatabaseReference
    private void initDatabaseReferences()
    {
        // Инициализация DatabaseReference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference(USERS);

        databaseReferenceStudents = databaseReference.child(STUDENT);
        databaseReferenceTeachers = databaseReference.child(USER);
        databaseReferenceCountUsers = databaseReference.child(COUNT_USERS);
        databaseReferenceCountStudent = databaseReference.child(COUNT_STUDENTS);
        databaseReferenceCountTeacher = databaseReference.child(COUNT_TEACHERS);
    }

    // Загрузка списка админов из Firebase
    private void loadUsers()
    {
        //студенты
        databaseReferenceStudents.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                usersStudentsList.clear();

                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren())
                {
                    String studentID = studentSnapshot.getKey();
                    String studentValue = studentSnapshot.child(GROUP).getValue(String.class);

                    // Восстанавливаем исходные значения, заменяя '*' обратно на '/'
                    if (studentID != null && studentValue != null)
                    {
                        studentValue = studentValue.replace("\\\\*", "/");

                        Class_users students_users = new Class_users(studentID, studentValue);
                        usersStudentsList.add(students_users);
                    }
                }

                usersStudentsAdapter.notifyDataSetChanged();
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
                usersTeachersList.clear();

                for (DataSnapshot teacherSnapshot : dataSnapshot.getChildren())
                {
                    String teacherName = teacherSnapshot.getKey();
                    String teacherValue = teacherSnapshot.child(GROUP).getValue(String.class);

                    if (teacherName != null && teacherValue != null)
                    {
                        Class_users teacher_users = new Class_users(teacherName, teacherValue);
                        usersTeachersList.add(teacher_users);
                    }
                }

                usersTeachersAdapter.notifyDataSetChanged();
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

                if (countUsersValue != null)
                {
                    String countUsersValueText = countUsersValue.toString();
                    count_all_users.setText(countUsersValueText);
                }
                else
                {
                    count_all_users.setText("0");
                }
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

                if (countStudentValue != null)
                {
                    String countStudentValueText = countStudentValue.toString();
                    count_students_all_users.setText(countStudentValueText);
                }
                else
                {
                    count_students_all_users.setText("0");
                }
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

                if (countTeacherValue != null)
                {
                    String countTeacherValueText = countTeacherValue.toString();
                    count_teachers_all_users.setText(countTeacherValueText);
                }
                else
                {
                    count_teachers_all_users.setText("0");
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                progressBar.setVisibility(View.GONE);
            }
        });
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
                    getActivity().runOnUiThread(() -> no_internet_setting_all_users.setVisibility(View.GONE));

                    // Отменяем таймер
                    stopInternetCheckTimer();

                    // Загрузка списка админов
                    loadUsers();
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