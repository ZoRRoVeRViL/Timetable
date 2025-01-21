package com.example.timetable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.timetable.fragments_bottom_navigation.Fragment_notifications;
import com.example.timetable.fragments_bottom_navigation.Fragment_settings;
import com.example.timetable.fragments_bottom_navigation.Fragment_staff;
import com.example.timetable.fragments_bottom_navigation.Fragment_timetable;
import com.example.timetable.holidays.SnowflakeView;
import com.example.timetable.widget.WidgetTimetable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
{
    private BottomNavigationView bottomNavigationView;
    private SharedPreferences sharedPreferences_load_uid, sharedPreferences_load_role, sharedPreferences_save_role;
    private String UID, role;

    private TextView title_alert_dialog, message_alert_dialog;
    private Button positive_button_alert_dialog, negative_button_alert_dialog;
    private AlertDialog.Builder builder;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference, databaseReference_post, databaseReference_userCount, databaseReference_Under_userCount,
            databaseReference_versions;
    private Context context;

    // ПРАЗДНИКИ
    private SnowflakeView snowflakeView;

    private static final String ADMINS_NODE = "Админы";
    private static final String STUDENT_NODE = "Студент";
    private static final String USERS_NODE = "Пользователи";
    private static final String TEACHER_NODE = "Преподаватель";
    private static final String USERS_VERSION = "Приложение/Версия";
    private static final String REGULAR_USER = "Обычный пользователь";
    private static final String USERS_COUNT_NODE = "Количество пользователей";
    private static final String STUDENT_COUNT_NODE = "Количество пользователей студентов";
    private static final String TEACHER_COUNT_NODE = "Количество пользователей преподавателей";
    private static final String URL = "https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String URL_DOWNLOAD = "https://drive.google.com/drive/u/0/folders/1N3He1a9LIJbA7vIdbN38rAMWvV-1pnjV";

    private static final String WELCOME_TEXT = "Вы студент или преподаватель?\nПозже вы можете изменить свою роль в: Настройки → Профиль → *нажать на Роль*";
    private static final String UPDATE_VERSION_TO = "\nОбновите приложение до версии: ";
    private static final String NOW_VERSION = "Текущая версия приложения: ";
    private static final String NEW_VERSION = "Доступна новая версия!";
    private static final String WELCOME = "Добро пожаловать!";
    private static final String WELCOME_TEACHER = "Препод.";
    private static final String WELCOME_STUDENT = "Студ.";
    private static final String DOWNLOAD = "Скачать";
    private static final String LATER = "Отложить";

    // ВИДЖЕТ
    private final BroadcastReceiver timeTickReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (Intent.ACTION_TIME_TICK.equals(intent.getAction()))
            {
                WidgetTimetable.updateWidgetManually(context);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ПРАЗДНИКИ
        loadSnowAnimation();

        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        context = this;
        database = FirebaseDatabase.getInstance(URL);

        loadSharedPreferences();
        checkUserRole();
        postSaveIfUIDExists();
        increaseAppOpenCount();
        loadAndCheckAppVersion();

        setupBottomNavigation();

        // Обновляем виджет при старте приложения
        WidgetTimetable.updateWidgetManually(this);
    }

    // ВИДЖЕТ
    @Override
    protected void onStart()
    {
        super.onStart();

        registerReceiver(timeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }
    @Override
    protected void onStop()
    {
        super.onStop();

        unregisterReceiver(timeTickReceiver);
    }

    private void loadSharedPreferences()
    {
        sharedPreferences_load_role = context.getSharedPreferences("select_role", Context.MODE_PRIVATE);
        role = sharedPreferences_load_role.getString("Role", null);

        sharedPreferences_load_uid = context.getSharedPreferences("UID_User", Context.MODE_PRIVATE);
        UID = sharedPreferences_load_uid.getString("UID", null);
    }

    private void checkUserRole()
    {
        if (role == null)
        {
            showSelect(WELCOME, WELCOME_TEXT, WELCOME_TEACHER, WELCOME_STUDENT);
        }
        else
        {
            loadRoleData(role);
        }
    }

    private void postSaveIfUIDExists()
    {
        if (UID != null && !UID.isEmpty())
        {
            postSave();
        }
    }

    private void loadAndCheckAppVersion()
    {
        loadVersionsFireBase();
    }

    private void setupBottomNavigation()
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_timetable()).commit();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.timetable);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

    //загрузить роли пользователя
    public void loadRoleData(String role_str)
    {
        if (role_str == null || role_str.isEmpty()) return;

        try
        {
            databaseReference = database.getReference(USERS_NODE);
            databaseReference_userCount = databaseReference.child(USERS_COUNT_NODE);

            if (role_str.equals(STUDENT_NODE))
            {
                databaseReference_Under_userCount = databaseReference.child(STUDENT_COUNT_NODE);
            }
            else if (role_str.equals(TEACHER_NODE))
            {
                databaseReference_Under_userCount = databaseReference.child(TEACHER_COUNT_NODE);
            }

            //Всего пользователей
            loadUserCount(databaseReference_userCount);
            //Определенных пользователей
            loadUserCount(databaseReference_Under_userCount);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void loadUserCount(DatabaseReference databaseReference)
    {
        try
        {
            databaseReference.runTransaction(new Transaction.Handler()
            {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData count)
                {
                    Long currentCount = count.getValue(Long.class);

                    if (currentCount == null) currentCount = 0L;

                    currentCount++;
                    count.setValue(currentCount);

                    return Transaction.success(count);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {}
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Проверяем и сохраняем роль пользователя
    public void postSave()
    {
        try
        {
            databaseReference_post = database.getReference(USERS_NODE).child(ADMINS_NODE);
            databaseReference_post.child(UID).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    sharedPreferences_save_role = context.getSharedPreferences("select_post", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences_save_role.edit();

                    if (dataSnapshot.exists())
                    {
                        String adminValue = dataSnapshot.getValue(String.class);

                        editor.putString("Post", adminValue);
                    }
                    else
                    {
                        editor.putString("Post", REGULAR_USER);
                    }

                    editor.apply();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Получение версии приложения из Firebase
    public void loadVersionsFireBase()
    {
        try
        {
            databaseReference_versions = database.getReference(USERS_VERSION);
            databaseReference_versions.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    String firebaseVersion = dataSnapshot.getValue(String.class);
                    String Version = loadVersions();

                    if (firebaseVersion != null && Version != null && !firebaseVersion.equals(Version))
                    {
                        showSelect(NEW_VERSION,  NOW_VERSION + Version + UPDATE_VERSION_TO + firebaseVersion, DOWNLOAD, LATER);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                { }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Получение версии приложения из памяти
    private String loadVersions()
    {
        try
        {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    private void increaseAppOpenCount()
    {
        try
        {
            SharedPreferences sharedPreferences_count_open = getSharedPreferences("Count_open", MODE_PRIVATE);
            int appOpenCount = sharedPreferences_count_open.getInt("count", 0);
            appOpenCount++;

            SharedPreferences.Editor editor = sharedPreferences_count_open.edit();
            editor.putInt("count", appOpenCount);
            editor.apply();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Метод для отображения диалога выбора
    private void showSelect(String welcome, String text, String pos, String neg)
    {
        // Версии разные, отобразить окно обновления
        builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        title_alert_dialog = dialogView.findViewById(R.id.title_alert_dialog);
        message_alert_dialog = dialogView.findViewById(R.id.message_alert_dialog);
        positive_button_alert_dialog = dialogView.findViewById(R.id.positive_button_alert_dialog);
        negative_button_alert_dialog = dialogView.findViewById(R.id.negative_button_alert_dialog);

        //вывести текст
        title_alert_dialog.setText(welcome);
        message_alert_dialog.setText(text);
        positive_button_alert_dialog.setText(pos);
        negative_button_alert_dialog.setText(neg);

        AlertDialog dialog = builder.create();

        dialog.show();

        negative_button_alert_dialog.setOnClickListener(v ->
        {
            if(welcome.equals(WELCOME))
            {
                saveRole(STUDENT_NODE);
            }

            dialog.dismiss();
        });

        positive_button_alert_dialog.setOnClickListener(v ->
        {
            if(welcome.equals(WELCOME))
            {
                saveRole(TEACHER_NODE);
            }
            else
            {
                openDownloadLink();
            }

            dialog.dismiss();
        });
    }

    // Открываем ссылку на скачивание
    private void openDownloadLink()
    {
        Uri uri = Uri.parse(URL_DOWNLOAD);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        if (intent.resolveActivity(getPackageManager()) != null)
        {
            startActivity(intent);
        }
    }

    // Сохранить role в SharedPreferences
    public void saveRole(String role)
    {
        // Перезагрузить BottomNavigationView
        reloadBottomNavigationView();

        try
        {
            sharedPreferences_save_role = context.getSharedPreferences("select_role", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences_save_role.edit();
            editor.putString("Role", role);
            editor.apply();

            loadRoleData(role);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Перезагрузить BottomNavigationView
    private void reloadBottomNavigationView()
    {
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        bottomNavigationView.setSelectedItemId(bottomNavigationView.getSelectedItemId());
    }

    //функция BottomNavigationView
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item ->
    {
        Fragment selectedFragment;

        switch (item.getItemId())
        {
            case R.id.timetable:
                selectedFragment = new Fragment_timetable();
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;
            case R.id.notifications:
                selectedFragment = new Fragment_notifications();
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;
            case R.id.staff:
                selectedFragment = new Fragment_staff();
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;
            case R.id.settings:
                selectedFragment = new Fragment_settings();
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;
            default:
                return false;
        }

        if (selectedFragment != null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        }

        return false;
    };

    // Загрузка значения настройки анимации снега
    private void loadSnowAnimation()
    {
        SharedPreferences settingsPrefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        boolean isSnowAnimationEnabled = settingsPrefs.getBoolean("snow_animation", true);
        setSnowAnimation(isSnowAnimationEnabled);
    }
    public void setSnowAnimation(boolean enable)
    {
        if(enable)
        {
            addSnowflakeView();
        }
        else
        {
            removeSnowflakeView();
        }
    }
    private void removeSnowflakeView()
    {
        if (snowflakeView != null)
        {
            ViewGroup parent = (ViewGroup) snowflakeView.getParent();

            if(parent!=null)
                parent.removeView(snowflakeView);
            snowflakeView = null;
        }
    }
    private void addSnowflakeView()
    {
        if (snowflakeView == null)
        {
            snowflakeView = new SnowflakeView(this);
            ViewGroup layout = findViewById(R.id.snowflake_container);

            if(layout!=null)
                layout.addView(snowflakeView);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
        {
            getSupportFragmentManager().popBackStack();
        }
        else
        {
            int selectedItemId = bottomNavigationView.getSelectedItemId();

            if (selectedItemId != R.id.timetable)
            {
                getSupportFragmentManager().popBackStack();
                Fragment selectedFragment = new Fragment_timetable();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                bottomNavigationView.setSelectedItemId(R.id.timetable);
            }
            else
            {
                super.onBackPressed();
            }
        }
    }
}