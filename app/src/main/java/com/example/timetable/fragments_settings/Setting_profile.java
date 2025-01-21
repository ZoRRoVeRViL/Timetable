package com.example.timetable.fragments_settings;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.timetable.MainActivity;
import com.example.timetable.R;
import com.example.timetable.fragments_bottom_navigation.Fragment_timetable;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;

import static android.content.Context.MODE_PRIVATE;

public class Setting_profile extends Fragment
{
    View view;

    Toolbar toolbar_setting_profile;

    TextView post_setting_profile, role_setting_profile, UID_setting_profile, id_setting_profile, registration_date_setting_profile,
            institute_setting_profile, group_setting_profile, copyID, select_student, select_teacher, count_open_setting_profile, history_group_setting_profile,
            groupLabel;
    LinearLayout LinearLayout_UID_setting_profile, LinearLayout_role_setting_profile, LinearLayout_id_setting_profile;

    ImageView delete_profile_setting_profile;

    //загрузка
    SharedPreferences sharedPreferences_load_student, sharedPreferences_load_teacher, sharedPreferences_load_uid, sharedPreferences_load_reg_date, sharedPreferences_load_role,
            sharedPreferences_load_post, sharedPreferences_count_open, sharedPreferences_save_role, sharedPreferences_switch_parse;

    String GroupUnion, GroupName, TeacherUnion, TeacherName, TeacherID, History_group, post, UID, regDATE, role, old_role;

    // Для отображения меню
    PopupWindow popupMenuID, popupMenuRole;
    Context context;

    //окно подтверждения
    TextView title_alert_dialog, message_alert_dialog;
    Button positive_button_alert_dialog, negative_button_alert_dialog;
    AlertDialog.Builder builder;

    // Таймер для проверки интернета
    private Timer internetCheckTimer;

    DatabaseReference database, databaseUserCountMinus, databaseUserCountPlus;

    private static final String URL = "https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String STUDENT_ROLE = "Студент";
    private static final String TEACHER_ROLE = "Преподаватель";
    private static final String USERS = "Пользователи";
    private static final String GROUP = "Группа";
    private static final String DATE_REGISTRATION = "Дата регистрации";
    private static final String TXT_GEN_UID = "Ваш UID еще не сгенрерирован";
    private static final String TXT_SELECT_GROUP = "Выберите свою группу в настройках";
    private static final String TXT_SELECT_UNION = "Выберите свое УЧП в настройках";
    private static final String TXT_SELECT_ID = "СВФУ id";
    private static final String TXT_SELECT_NAME = "Выберите свое ФИО в настройках";
    private static final String TXT_COUNT_USERS = "Количество пользователей";
    private static final String TXT_COUNT_USERS_STUDENTS = "Количество пользователей студентов";
    private static final String TXT_COUNT_USERS_TEACHERS = "Количество пользователей преподавателей";
    private static final String TXT_ERROR_SELECT_GROUP = "Ошибка. Ваш UID ещё не сгенерирован, выберите свою группу в настройках";
    private static final String TXT_ERROR_NO_INTERNET = "Ошибка. Нет интернет.";
    private static final String TXT_YOUR_UID_COPY = "Ваш UID скопирован в буфер обмена";
    private static final String TXT_YOUR_UID_NOT_GENERATE = "Ошибка. Ваш UID ещё не сгенерирован";
    private static final String TXT_YOU_ALREADY = "Вы и так ";
    private static final String TXT_ROLE = "Роль";
    private static final String TXT_SELECT_YOUR_ROLE_IN = "Сменить роль на: ";
    private static final String TXT_YOUR_SELECT_ROLE_IN = "Ваша роль изменена на: ";
    private static final String TXT_IN = " на: ";
    private static final String TXT_YES = "Да";
    private static final String TXT_NO = "Нет";
    private static final String TXT_NAME = "ФИО";
    private static final String TXT_GROUP = "Учебная группа:";
    private static final String TXT_NO_HISTORY = "Истории ещё нет...";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.setting_profile, container, false);

        //выгрузка элементов интерфейса
        FindID();

        context = view.getContext();

        loadSharePreferences();

        // Устанавливаем название института в Toolbar
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_setting_profile);
        // Добавление кнопки "Назад"
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Обработчик нажатия на кнопку "Назад"
        toolbar_setting_profile.setNavigationOnClickListener(v -> getActivity().onBackPressed());

        //нажать на роль
        LinearLayout_role_setting_profile.setOnClickListener(v ->
        {
            if (!UID.equals(TXT_GEN_UID))
            {
                //загрузка новой роли для изменения
                sharedPreferences_load_role = requireContext().getSharedPreferences("select_role", MODE_PRIVATE);
                old_role = sharedPreferences_load_role.getString("Role", null);

                // Инициализация кнопок меню
                View popupViewRefactorRole = LayoutInflater.from(context).inflate(R.layout.popup_menu_refactor_role_profile, null);
                select_student = popupViewRefactorRole.findViewById(R.id.button_student_popup_menu_profile);
                select_teacher = popupViewRefactorRole.findViewById(R.id.button_teacher_popup_menu_profile);
                //Настройка PopupWindow
                popupMenuRole = new PopupWindow(popupViewRefactorRole, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupMenuRole.setElevation(8);

                menu(v, popupMenuRole);

                //студент
                select_student.setOnClickListener(view ->
                {
                    String new_role = select_student.getText().toString().trim();
                    if (!old_role.equals(new_role))
                    {
                        switchRole(old_role, new_role);
                    }
                    else
                    {
                        showSnackBar(TXT_YOU_ALREADY + new_role);
                    }

                    popupMenuRole.dismiss();
                });

                //Преподователь
                select_teacher.setOnClickListener(view ->
                {
                    String new_role = select_teacher.getText().toString().trim();

                    if (!old_role.equals(new_role))
                    {
                        switchRole(old_role, new_role);
                    }
                    else
                    {
                        showSnackBar(TXT_YOU_ALREADY + new_role);
                    }

                    popupMenuRole.dismiss();
                });
            }
            else
            {
                showSnackBar(TXT_ERROR_SELECT_GROUP);
            }
        });

        //нажать на UID
        LinearLayout_UID_setting_profile.setOnClickListener(v ->
        {
            if (!UID.equals(TXT_GEN_UID))
            {
                // Инициализация кнопок меню
                View popupViewCopyID = LayoutInflater.from(context).inflate(R.layout.popup_menu_copy_id_profile, null);
                copyID = popupViewCopyID.findViewById(R.id.button_copy_id_owner_popup_menu_profile);
                //Настройка PopupWindow
                popupMenuID = new PopupWindow(popupViewCopyID, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupMenuID.setElevation(8);

                String UID = UID_setting_profile.getText().toString().trim();

                menu(v, popupMenuID);

                copyID.setOnClickListener(view ->
                {
                    if (!UID.isEmpty())
                    {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("UID", UID);
                        clipboard.setPrimaryClip(clip);

                        showSnackBar(TXT_YOUR_UID_COPY);
                    }
                    else
                    {
                        showSnackBar(TXT_YOUR_UID_NOT_GENERATE);
                    }

                    popupMenuID.dismiss();
                });
            }
            else
            {
                showSnackBar(TXT_ERROR_SELECT_GROUP);
            }
        });

        //удалить профиль
        delete_profile_setting_profile.setOnClickListener(v ->
        {
            if (UID != null && !UID.isEmpty())
            {
                deleteProfile(1);
            }
            else
            {
                //уведомление
                showSnackBar("Ошибка, ваш профиль пока еще не создан");
            }
        });

        return view;
    }

    //выгрузка элементов интерфейса
    private void FindID()
    {
        post_setting_profile = view.findViewById(R.id.post_setting_profile);
        role_setting_profile = view.findViewById(R.id.role_setting_profile);
        LinearLayout_role_setting_profile = view.findViewById(R.id.LinearLayout_role_setting_profile);
        LinearLayout_id_setting_profile = view.findViewById(R.id.LinearLayout_id_setting_profile);
        UID_setting_profile = view.findViewById(R.id.UID_setting_profile);
        id_setting_profile = view.findViewById(R.id.id_setting_profile);
        registration_date_setting_profile = view.findViewById(R.id.registration_date_setting_profile);
        LinearLayout_UID_setting_profile = view.findViewById(R.id.LinearLayout_UID_setting_profile);
        institute_setting_profile = view.findViewById(R.id.institute_setting_profile);
        group_setting_profile = view.findViewById(R.id.group_setting_profile);
        count_open_setting_profile = view.findViewById(R.id.count_open_setting_profile);
        history_group_setting_profile = view.findViewById(R.id.history_group_setting_profile);
        groupLabel = view.findViewById(R.id.groupLabel);
        toolbar_setting_profile = view.findViewById(R.id.toolbar_setting_profile);
        delete_profile_setting_profile = view.findViewById(R.id.delete_profile_setting_profile);
    }

    // Метод загрузки из памяти
    public void loadSharePreferences()
    {
        try
        {
            // Загрузка из памяти должности пользователя
            sharedPreferences_load_post = requireContext().getSharedPreferences("select_post", MODE_PRIVATE);
            post = sharedPreferences_load_post.getString("Post", null);
            post_setting_profile.setText(post);

            //Загрузка из памяти роли пользователя
            sharedPreferences_load_role = requireContext().getSharedPreferences("select_role", MODE_PRIVATE);
            role = sharedPreferences_load_role.getString("Role", null);
            if (role == null)
            { role = TXT_SELECT_GROUP; }
            role_setting_profile.setText(role);

            //Загрузка из памяти UID_User
            sharedPreferences_load_uid = requireContext().getSharedPreferences("UID_User", MODE_PRIVATE);
            UID = sharedPreferences_load_uid.getString("UID", null);
            if (UID == null)
            { UID = TXT_GEN_UID; }
            UID_setting_profile.setText(UID);

            // Загрузка состояния Switch из SharedPreferences
            sharedPreferences_switch_parse = requireContext().getSharedPreferences("switch_parse", MODE_PRIVATE);

            //Загрузка из памяти даты регистрации
            sharedPreferences_load_reg_date = requireContext().getSharedPreferences("registration_Date", MODE_PRIVATE);
            regDATE = sharedPreferences_load_reg_date.getString("DATE", null);
            if (regDATE == null)
            { regDATE = TXT_SELECT_GROUP; }
            registration_date_setting_profile.setText(regDATE);

            // Загрузка из памяти название группы и института
            sharedPreferences_load_student = requireContext().getSharedPreferences("select_group", MODE_PRIVATE);
            String loadedGroupUnion = sharedPreferences_load_student.getString("GroupUnion", null);
            String loadedGroupName = sharedPreferences_load_student.getString("GroupName", null);
            // Восстанавливаем исходные значения, заменяя '*' обратно на '/'
            if (loadedGroupUnion != null && loadedGroupName != null)
            { GroupUnion = loadedGroupUnion.replace("\\\\*", "/");
                GroupName = loadedGroupName.replace("\\\\*", "/");}
            else
            { GroupUnion = TXT_SELECT_UNION;
                GroupName = TXT_SELECT_GROUP;}

            // Загрузка из памяти название института и ФИО
            sharedPreferences_load_teacher = requireContext().getSharedPreferences("select_teacher_name", MODE_PRIVATE);
            TeacherUnion = sharedPreferences_load_teacher.getString("Union", null);
            TeacherID = sharedPreferences_load_teacher.getString("id", null);
            TeacherName = sharedPreferences_load_teacher.getString("Name", null);
            if (TeacherUnion == null && TeacherID == null && TeacherName == null)
            { TeacherUnion = TXT_SELECT_UNION;
                TeacherID = TXT_SELECT_ID;
                TeacherName = TXT_SELECT_NAME;}

            SharedPreferences sharedPreferences_group_history = null;
            //если студент или препод
            if (role.equals(STUDENT_ROLE))
            {
                sharedPreferences_group_history = requireContext().getSharedPreferences("Group_History_student", MODE_PRIVATE);
                institute_setting_profile.setText(GroupUnion);
                groupLabel.setText(TXT_GROUP);
                group_setting_profile.setText(GroupName);

                LinearLayout_id_setting_profile.setVisibility(View.GONE);
            }
            else if (role.equals(TEACHER_ROLE))
            {
                id_setting_profile.setText(TeacherID);

                sharedPreferences_group_history = requireContext().getSharedPreferences("Group_History_teacher", MODE_PRIVATE);
                institute_setting_profile.setText(TeacherUnion);

                groupLabel.setText(TXT_NAME);
                group_setting_profile.setText(TeacherName);

                LinearLayout_id_setting_profile.setVisibility(View.VISIBLE);
            }

            //загрузка истории выбора групп
            String history_group = sharedPreferences_group_history.getString("History", "");
            if (history_group != null && !history_group.isEmpty())
            { History_group = history_group.replace("\\\\*", "/"); }
            else { History_group = TXT_NO_HISTORY; }
            history_group_setting_profile.setText(History_group);

            // загрузка счетчика открытия приложения
            sharedPreferences_count_open = requireContext().getSharedPreferences("Count_open", MODE_PRIVATE);
            String count = String.valueOf(sharedPreferences_count_open.getInt("count", 0));
            count_open_setting_profile.setText(count);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //вызова меню
    public void menu(View v, PopupWindow popupWindow)
    {
        //вывести относительно экрана справа посередине
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        int x = location[0] + v.getWidth();
        int y = location[1] + v.getHeight() / 2;

        // Показать меню всплывающего окна
        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, x, y);

        // Создайте анимацию для масштабирования от 0 до 1
        Animation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
        scaleAnimation.setDuration(250);

        // Создайте набор анимаций и добавьте в него обе анимации
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);

        // Установите анимацию для вашего popupMenu
        popupWindow.getContentView().startAnimation(animationSet);
    }

    private void deleteProfile(int i)
    {
        //окно подтверждения
        builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        title_alert_dialog = dialogView.findViewById(R.id.title_alert_dialog);
        message_alert_dialog = dialogView.findViewById(R.id.message_alert_dialog);
        positive_button_alert_dialog = dialogView.findViewById(R.id.positive_button_alert_dialog);
        negative_button_alert_dialog = dialogView.findViewById(R.id.negative_button_alert_dialog);

        if (i == 1)
        {
            //вывести текст
            title_alert_dialog.setText("Удалить профиль");
            message_alert_dialog.setText("Удалить/стереть ваш профиль навсегда?");
            positive_button_alert_dialog.setText("Удалить");
            negative_button_alert_dialog.setText("Нет");
        }
        else if (i == 2)
        {
            //вывести текст
            title_alert_dialog.setText("Вы уверены?");
            message_alert_dialog.setText("Вы точно хотите удалить/стереть профиль навсегда?");
            positive_button_alert_dialog.setText("Удалить");
            negative_button_alert_dialog.setText("Нет");
        }
        else if (i == 3)
        {
            //вывести текст
            title_alert_dialog.setText("Вы точно уверены?");
            message_alert_dialog.setText("Точно точно удалить/стереть ваши данные НАВСЕГДА? Вы уверены? Точно? Вдруг вы случайно 3 раза нажали удалить");
            positive_button_alert_dialog.setText("Удалить");
            negative_button_alert_dialog.setText("Нет");
        }

        AlertDialog dialog = builder.create();

        dialog.show();

        //Да
        positive_button_alert_dialog.setOnClickListener(v ->
        {
            if (i == 1)
            {
                deleteProfile(2);
            }
            else if (i == 2)
            {
                deleteProfile(3);
            }
            else if (i == 3)
            {
                deleteProfileFireBase(role);
            }

            dialog.dismiss();
        });

        //Нет
        negative_button_alert_dialog.setOnClickListener(v -> dialog.dismiss());
    }

    // Метод для отображения диалога выбора
    private void switchRole(String old_role, String new_role)
    {
        //окно подтверждения
        builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        title_alert_dialog = dialogView.findViewById(R.id.title_alert_dialog);
        message_alert_dialog = dialogView.findViewById(R.id.message_alert_dialog);
        positive_button_alert_dialog = dialogView.findViewById(R.id.positive_button_alert_dialog);
        negative_button_alert_dialog = dialogView.findViewById(R.id.negative_button_alert_dialog);

        //вывести текст
        title_alert_dialog.setText(TXT_ROLE);
        String text_role = TXT_SELECT_YOUR_ROLE_IN + old_role + TXT_IN + new_role + "?";
        message_alert_dialog.setText(text_role);
        positive_button_alert_dialog.setText(TXT_YES);
        negative_button_alert_dialog.setText(TXT_NO);

        AlertDialog dialog = builder.create();

        dialog.show();

        //Да
        positive_button_alert_dialog.setOnClickListener(v ->
        {
            if (isInternetAvailable())
            {
                showSelect(old_role, new_role);
            }
            else
            {
                showSnackBar(TXT_ERROR_NO_INTERNET);
            }
            dialog.dismiss();
        });

        //Нет
        negative_button_alert_dialog.setOnClickListener(v -> dialog.dismiss());
    }

    //Метод удаление профиля
    private void deleteProfileFireBase(String role)
    {
        try
        {
            //если роль выбрана, группа выбрана создать ветки польователя на сервере
            database = FirebaseDatabase.getInstance(URL).getReference(USERS);

            // Проверяем наличие ветки
            database.child(role).child(UID).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    try
                    {
                        //удалить старый
                        database.child(role).child(UID).removeValue();

                        //если студент или препод
                        if (role.equals(STUDENT_ROLE))
                        {
                            // Ветка не существует создаем ее и устанавливаем значение
                            database.child("Удалённые").child(role).child(UID).child(GROUP).setValue(GroupUnion + " | " + GroupName);
                            database.child("Удалённые").child(role).child(UID).child(DATE_REGISTRATION).setValue(regDATE);
                        }
                        else if (role.equals(TEACHER_ROLE))
                        {
                            // Ветка не существует создаем ее и устанавливаем значение
                            database.child("Удалённые").child(role).child(UID).child(GROUP).setValue(TeacherID + " | " + TeacherName);
                            database.child("Удалённые").child(role).child(UID).child(DATE_REGISTRATION).setValue(regDATE);
                        }

                        // Создание Firebase Realtime Database и получение ссылки на нужную ветку
                        if (role.equals(STUDENT_ROLE))
                        {
                            databaseUserCountMinus = database.child(TXT_COUNT_USERS_STUDENTS);
                        }
                        else
                        {
                            databaseUserCountMinus = database.child(TXT_COUNT_USERS_TEACHERS);
                        }
                        //уменьшение счетчика после удаления
                        minisUserDeleteProfile();
                        databaseUserCountMinus = database.child(TXT_COUNT_USERS);
                        minisUserDeleteProfile();

                        // Получение объекта для удаления
                        String[] sharedPreferencesNames = {
                                "Group_History_teacher",
                                "Group_History_student",
                                "select_teacher_name",
                                "select_group",
                                "registration_Date",
                                "UID_User",
                                "select_role",
                                "registration_Date",
                                "select_post",
                                "count_parse",
                                "parse_group_teacher",
                                "Count_open",
                                "day_week_click",
                                "Setting-union-student",
                                "Setting-union-teacher",
                                "Setting-group-student",
                                "Setting-group-teacher",
                                TeacherUnion + "-" + TeacherName,
                                GroupUnion + "-" + GroupName
                        };

                        //код для очистки всех SharedPreferences
                        for (String sharedPreferencesName : sharedPreferencesNames)
                        {
                            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear().apply();
                        }

                        //уведомление
                        showSnackBar("Ваш профиль успешно удален. До скорых встреч, удачи)");

                        new Handler().postDelayed(() ->
                        {
                            Intent intent = new Intent(requireContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        }, 2500);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
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

    //уменьшение счетчика после удаления
    private void minisUserDeleteProfile()
    {
        try
        {
            databaseUserCountMinus.runTransaction(new Transaction.Handler()
            {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData count)
                {
                    Long currentCount = count.getValue(Long.class);

                    if (currentCount == null)
                    {
                        currentCount = 0L;
                    }

                    currentCount--;
                    count.setValue(currentCount);

                    return Transaction.success(count);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) { }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //смена роли
    public void showSelect(String old_role, String new_role)
    {
        try
        {
            //если роль выбрана, группа выбрана создать ветки польователя на сервере
            database = FirebaseDatabase.getInstance(URL).getReference(USERS);

            // Проверяем наличие ветки
            database.child(old_role).child(UID).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    try
                    {
                        //удалить старый
                        database.child(old_role).child(UID).removeValue();

                        //если студент или препод
                        if (new_role.equals(STUDENT_ROLE))
                        {
                            // Ветка не существует создаем ее и устанавливаем значение
                            database.child(new_role).child(UID).child(GROUP).setValue(GroupUnion + " | " + GroupName);
                            database.child(new_role).child(UID).child(DATE_REGISTRATION).setValue(regDATE);
                        }
                        else if (new_role.equals(TEACHER_ROLE))
                        {
                            // Ветка не существует создаем ее и устанавливаем значение
                            database.child(new_role).child(UID).child(GROUP).setValue(TeacherID + " | " + TeacherName);
                            database.child(new_role).child(UID).child(DATE_REGISTRATION).setValue(regDATE);
                        }

                        // Сохранить new_role в SharedPreferences
                        sharedPreferences_save_role = requireContext().getSharedPreferences("select_role", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences_save_role.edit();
                        editor.putString("Role", new_role);
                        editor.apply();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    // Создание Firebase Realtime Database и получение ссылки на нужную ветку
                    if (new_role.equals(STUDENT_ROLE))
                    {
                        databaseUserCountPlus = database.child(TXT_COUNT_USERS_STUDENTS);
                        databaseUserCountMinus = database.child(TXT_COUNT_USERS_TEACHERS);
                    }
                    else
                    {
                        databaseUserCountMinus = database.child(TXT_COUNT_USERS_STUDENTS);
                        databaseUserCountPlus = database.child(TXT_COUNT_USERS_TEACHERS);
                    }

                    countUser();

                    //обновить окно
                    loadSharePreferences();

                    //уведомление
                    showSnackBar(TXT_YOUR_SELECT_ROLE_IN + new_role);
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

    //изменить счетчик пределенных пользователей
    public void countUser()
    {
        try
        {
            databaseUserCountMinus.runTransaction(new Transaction.Handler()
            {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData count)
                {
                    Long currentCount = count.getValue(Long.class);

                    if (currentCount == null)
                    {
                        currentCount = 0L;
                    }

                    currentCount--;
                    count.setValue(currentCount);

                    return Transaction.success(count);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) { }
            });
            databaseUserCountPlus.runTransaction(new Transaction.Handler()
            {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData count)
                {
                    Long currentCount = count.getValue(Long.class);

                    if (currentCount == null)
                    {
                        currentCount = 0L;
                    }

                    currentCount++;
                    count.setValue(currentCount);

                    return Transaction.success(count);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) { }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // метод вывода уведомлений
    public void showSnackBar(String message)
    {
        try
        {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        // Отменяем таймер при уничтожении представления
        stopInternetCheckTimer();
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