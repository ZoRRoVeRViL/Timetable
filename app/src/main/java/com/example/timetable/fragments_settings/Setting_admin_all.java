package com.example.timetable.fragments_settings;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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

import static android.content.Context.MODE_PRIVATE;

public class Setting_admin_all extends Fragment
{
    View view;

    Toolbar toolbar_setting_admin_all;

    RecyclerView recyclerview_admin_all;

    //загрузка анимация
    private SpinKitView progressBar;

    LinearLayout no_internet_admin_all;

    ImageView fab_add_admin_all;

    // Firebase
    DatabaseReference databaseReference;
    List<Class_admin> adminList;
    AdminAdapter adminAdapter;

    //загрузка
    SharedPreferences sharedPreferences_load_uid;
    String UID;

    // Таймер для проверки интернета
    private Timer internetCheckTimer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.setting_admin_all, container, false);

        // Устанавливаем название института в Toolbar
        toolbar_setting_admin_all = view.findViewById(R.id.toolbar_setting_admin_all);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_setting_admin_all);
        // Добавление кнопки "Назад"
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Обработчик нажатия на кнопку "Назад"
        toolbar_setting_admin_all.setNavigationOnClickListener(v ->
        {
            // Вызвать метод возврата на предыдущий фрагмент
            getActivity().onBackPressed();
        });

        //Загрузка из памяти UID_User
        sharedPreferences_load_uid = requireContext().getSharedPreferences("UID_User", MODE_PRIVATE);
        UID = sharedPreferences_load_uid.getString("UID", null);

        //загрузка анимация
        progressBar = view.findViewById(R.id.progress_bar_admin_all);

        //кнопка добавления админов
        fab_add_admin_all = view.findViewById(R.id.fab_add_admin_all);

        //нет интернета
        no_internet_admin_all = view.findViewById(R.id.no_internet_admin_all);

        recyclerview_admin_all = view.findViewById(R.id.recyclerview_admin_all);

        databaseReference = FirebaseDatabase.getInstance("https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Пользователи").child("Админы");

        // Инициализация RecyclerView и адаптера
        recyclerview();

        // Проверка наличия интернета
        if (isInternetAvailable())
        {
            progressBar.setVisibility(View.VISIBLE);

            // Загрузка списка админов
            loadAdmins();
        }
        else
        {
            startInternetCheckTimer();

            // Если нет интернета, показать noInternetAdminAl
            no_internet_admin_all.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        fab_add_admin_all.setOnClickListener(v -> addAdmin());

        return view;
    }

    // Инициализация RecyclerView и адаптера
    private void recyclerview()
    {
        adminList = new ArrayList<>();
        adminAdapter = new AdminAdapter(adminList);
        recyclerview_admin_all.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerview_admin_all.setAdapter(adminAdapter);
    }

    // Загрузка списка админов из Firebase
    private void loadAdmins()
    {
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                adminList.clear();

                for (DataSnapshot adminSnapshot : dataSnapshot.getChildren())
                {
                    String adminName = adminSnapshot.getKey();
                    String adminValue = adminSnapshot.getValue(String.class);

                    if (adminName != null && adminValue != null)
                    {
                        if (adminName.equals(UID))
                        {
                            adminValue = adminValue + " (Я)";
                        }

                        Class_admin admin = new Class_admin(adminName, adminValue);
                        adminList.add(admin);
                    }
                }

                adminAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //открыть фрагмент добавления админа
    private void addAdmin()
    {
        // Создайте новый фрагмент, который будет открываться
        Setting_admin_add addAdminFragment = new Setting_admin_add();

        // Откройте новый фрагмент
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, addAdminFragment);
        transaction.addToBackStack(null);
        transaction.commit();
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
                    getActivity().runOnUiThread(() -> no_internet_admin_all.setVisibility(View.GONE));

                    // Отменяем таймер
                    stopInternetCheckTimer();

                    // Загрузка списка админов
                    loadAdmins();
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