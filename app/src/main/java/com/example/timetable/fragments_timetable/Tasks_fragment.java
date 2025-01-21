package com.example.timetable.fragments_timetable;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.timetable.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class Tasks_fragment extends Fragment
{
    View view;

    //кнопки
    ImageView button_back, button_delete, button_save, button_share;

    //название предмета
    TextView name_lesson_tasks, name_teacher_tasks;

    //задание
    EditText edit_tasks;

    LinearLayout linear_layout_edit_tasks, no_internet_item_additional_info;

    //загрузка
    SharedPreferences sharedPreferences_save, sharedPreferences_load, sharedPreferences_load_tasks, sharedPreferences_delete;
    //Сохранения
    String GroupUnion, GroupName;

    DatabaseReference databaseReference, homeworkLoad, homeworkDelete, homeworkSave;

    //название предмета и препода
    String lessonName, lessonTeacher, lessonNameFormat, lessonTeacherFormat;

    // Таймер для проверки интернета
    private Timer internetCheckTimer;

    private SpinKitView progressBar;

    private static final String TASKS_FIREBASE = "Задания";

    private static final String TASKS_EMPTY = "Ошибка, строка задания пустое";
    private static final String TASKS_SHARE = "Поделиться заданием";
    private static final String TASKS = "СРС ";
    private static final String DELETE = "Удалить";
    private static final String DELETE_TASKS_TEXT = "Удалить данное задание?";
    private static final String YES = "Да";
    private static final String NO = "Нет";
    private static final String ERROR_NO_INTERNET = "Ошибка удаления, нет интернета";
    private static final String ERROR_DELETE_TASKS = "Ошибка при удалении задания";
    private static final String TASKS_DELETED = "Задание удалено";
    private static final String ERROR_SAVE_NO_INTERNET = "Ошибка сохранения, нет интернета";
    private static final String ERROR_SAVE = "Ошибка при сохранении задания: ";
    private static final String TASKS_SAVE = "Задание сохранено";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.item_additional_info, container, false);

        //Загрузка из памяти название группы и института
        sharedPreferences_load = requireContext().getSharedPreferences("select_group", Context.MODE_PRIVATE);
        GroupUnion = sharedPreferences_load.getString("GroupUnion", null);
        GroupName = sharedPreferences_load.getString("GroupName", null);

        //выгрузка элементов интерфейса
        FindID();

        //получаем переданные аргументы
        Bundle args = getArguments();
        if (args != null)
        {
            lessonName = args.getString("предмет");
            lessonTeacher = args.getString("преподователь");
            name_lesson_tasks.setText(lessonName);
            name_teacher_tasks.setText(lessonTeacher);
        }

        //Удалить лишнии знаки
        lessonNameFormat = lessonName.replaceAll("[\\.\\-#\\$\\[\\]]", " ");
        lessonTeacherFormat = lessonTeacher.replaceAll("[\\.\\-#\\$\\[\\]]", "");

        //включения офлайн-режима
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //получить ссылку на Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance("https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Подразделения").child(GroupUnion).child(GroupName);
        //получить ссылку на Firebase Realtime Database для загрузки
        homeworkLoad = databaseReference.child(TASKS_FIREBASE).child(lessonNameFormat + " - " + lessonTeacherFormat);

        //Загрузка из памяти задания
        sharedPreferences_load_tasks = requireContext().getSharedPreferences(GroupUnion + "-" + GroupName, Context.MODE_PRIVATE);
        String memory_load_tasks = sharedPreferences_load_tasks.getString(lessonNameFormat + " - " + lessonTeacherFormat, null);

        //если есть сохранение
        if (memory_load_tasks != null && !memory_load_tasks.isEmpty())
        {
            edit_tasks.setText(memory_load_tasks);

            //загрузка задания из Firebase
            if (isInternetAvailable())
            {
                progressBar.setVisibility(View.VISIBLE);

                LoadTasks();
            }
            else
            {
                noInternet();
            }
        }
        else
        {
            if (isInternetAvailable())
            {
                progressBar.setVisibility(View.VISIBLE);

                LoadTasks();
            }
            else
            {
                noInternet();
            }
        }

        // Обработчик для кнопки сохранения
        button_save.setOnClickListener(v -> SaveTasks());

        // Обработчик  нажатия на кнопку удаления
        button_delete.setOnClickListener(v -> showSelect(view.getContext()));

        // Обработчик для кнопки "назад"
        button_back.setOnClickListener(v -> getFragmentManager().popBackStack());

        // Обработчик для кнопки "поделиться"
        button_share.setOnClickListener(v -> ShareTasks());

        // Обработчик для фоукса на EditText
        linear_layout_edit_tasks.setOnClickListener(v ->
        {
            // Передать фокус EditText
            edit_tasks.requestFocus();
            // Переместить курсор в конец текста
            edit_tasks.setSelection(edit_tasks.getText().length());

            // Откройте клавиатуру
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edit_tasks, InputMethodManager.SHOW_IMPLICIT);
        });

        return view;
    }

    //выгрузка элементов интерфейса
    private void FindID()
    {
        button_back = view.findViewById(R.id.button_back);
        button_delete = view.findViewById(R.id.button_delete);
        button_save = view.findViewById(R.id.button_save);
        button_share = view.findViewById(R.id.button_share);
        linear_layout_edit_tasks = view.findViewById(R.id.linear_layout_edit_tasks);
        no_internet_item_additional_info = view.findViewById(R.id.no_internet_item_additional_info);
        name_lesson_tasks = view.findViewById(R.id.name_lesson_tasks);
        name_teacher_tasks = view.findViewById(R.id.name_teacher_tasks);
        edit_tasks = view.findViewById(R.id.edit_tasks);
        progressBar = view.findViewById(R.id.progress_bar_tasks);
    }

    //Загрузка из FireBase
    private void LoadTasks()
    {
        //Иконка нет интернета
        getActivity().runOnUiThread(() -> no_internet_item_additional_info.setVisibility(View.GONE));

        //загрузить данные из Firebase
        homeworkLoad.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (isAdded())
                {
                    if (dataSnapshot.exists())
                    {
                        String homework = dataSnapshot.getValue(String.class);
                        edit_tasks.setText(homework);

                        try
                        {
                            // Сохраняем задание в память при загрузке
                            sharedPreferences_load = requireContext().getSharedPreferences(GroupUnion + "-" + GroupName, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences_load.edit();
                            editor.putString(lessonNameFormat + " - " + lessonTeacherFormat, homework);
                            editor.apply();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //сохранение в FireBase
    public void SaveTasks()
    {
        //если есть инет
        if (isInternetAvailable())
        {
            progressBar.setVisibility(View.VISIBLE);

            // Получаем задание из EditText
            String homework = edit_tasks.getText().toString();

            if (!homework.isEmpty())
            {
                //получить ссылку на Firebase Realtime Database для сохранения
                homeworkSave = databaseReference.child(TASKS_FIREBASE).child(lessonNameFormat + " - " + lessonTeacherFormat);

                homeworkSave.setValue(homework).addOnSuccessListener(aVoid ->
                {
                    // сообщение об успешном сохранении
                    showSnackbar(TASKS_SAVE);

                    progressBar.setVisibility(View.GONE);
                }).addOnFailureListener(e ->
                {
                    // Обработка ошибки при сохранении
                    showSnackbar(ERROR_SAVE + e.getMessage());

                    progressBar.setVisibility(View.GONE);
                });

                if (isAdded())
                {
                    try
                    {
                        // Сохраняем задание в память
                        sharedPreferences_save = requireContext().getSharedPreferences(GroupUnion + "-" + GroupName, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences_save.edit();
                        editor.putString(lessonNameFormat + " - " + lessonTeacherFormat, homework);
                        editor.apply();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                //сообщение об ошибке, если поле задания пусто
                showSnackbar(TASKS_EMPTY);

                progressBar.setVisibility(View.GONE);
            }
        }
        else
        {
            //сообщение об ошибке, если поле задания пусто
            showSnackbar(ERROR_SAVE_NO_INTERNET);
        }

        clearFocusAndHideKeyboard();
    }

    //удаление задания из FireBase
    public void DeleteTasks()
    {
        //если есть инет
        if (isInternetAvailable())
        {
            progressBar.setVisibility(View.VISIBLE);

            // Получить ссылку на узел для очиститки
            homeworkDelete = databaseReference.child(TASKS_FIREBASE).child(lessonNameFormat + " - " + lessonTeacherFormat);

            try
            {
                // Очистить узел в Firebase, установив его значение в null
                homeworkDelete.setValue("").addOnSuccessListener(aVoid ->
                {
                    // Удалить данные из памяти устройства
                    sharedPreferences_delete = requireContext().getSharedPreferences(GroupUnion + "-" + GroupName, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences_delete.edit();
                    editor.remove(lessonNameFormat + " - " + lessonTeacherFormat);
                    editor.apply();

                    // Успешно очищено
                    showSnackbar(TASKS_DELETED);

                    // Очистите EditText после очистки задания
                    edit_tasks.setText("");

                    progressBar.setVisibility(View.GONE);
                }).addOnFailureListener(e ->
                {
                    // Ошибка при очистке
                    showSnackbar(ERROR_DELETE_TASKS);
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            //сообщение об ошибке, если поле задания пусто
            showSnackbar(ERROR_NO_INTERNET);
        }
    }

    // Метод для отображения диалога подтверждения
    private void showSelect(Context context)
    {
        clearFocusAndHideKeyboard();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null);
        builder.setView(dialogView);

        TextView title_alert_dialog = dialogView.findViewById(R.id.title_alert_dialog);
        TextView message_alert_dialog = dialogView.findViewById(R.id.message_alert_dialog);
        Button positive_button_alert_dialog = dialogView.findViewById(R.id.positive_button_alert_dialog);
        Button negative_button_alert_dialog = dialogView.findViewById(R.id.negative_button_alert_dialog);

        //вывести заголовок и текст
        title_alert_dialog.setText(DELETE);
        message_alert_dialog.setText(DELETE_TASKS_TEXT);
        positive_button_alert_dialog.setText(YES);
        negative_button_alert_dialog.setText(NO);

        AlertDialog dialog = builder.create();

        positive_button_alert_dialog.setOnClickListener(v ->
        {
            DeleteTasks();

            dialog.dismiss();
        });

        negative_button_alert_dialog.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    //очистка фокуса
    private void clearFocusAndHideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        edit_tasks.clearFocus();
    }

    //уведомления
    private void showSnackbar(String message)
    {
        if (view != null)
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
    }

    //Поделиться заданием
    private void ShareTasks()
    {
        clearFocusAndHideKeyboard();

        String taskText = edit_tasks.getText().toString();

        if (!taskText.equals(""))
        {
            //если есть инет
            if (isInternetAvailable())
            {
                String subject = TASKS + lessonName + ":";

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, subject + "\n\n" + taskText);

                // Показать диалог Поделиться
                startActivity(Intent.createChooser(shareIntent, TASKS_SHARE));
            }
            else
            {
                noInternet();
            }
        }
        else
        {
            //сообщение об ошибке, если поле задания пусто
            showSnackbar(TASKS_EMPTY);
        }
    }

    public void noInternet()
    {
        //Иконка нет интернета
        no_internet_item_additional_info.setVisibility(View.VISIBLE);

        // Запускаем таймер проверки интернета
        startInternetCheckTimer();

        progressBar.setVisibility(View.GONE);
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

                        // Отменяем таймер
                        stopInternetCheckTimer();

                        //загрузка задания из Firebase
                        LoadTasks();
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            return networkInfo != null && networkInfo.isConnected();
        }

        return false;
    }
}