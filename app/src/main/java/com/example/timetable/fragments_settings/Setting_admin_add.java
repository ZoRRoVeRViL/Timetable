package com.example.timetable.fragments_settings;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.timetable.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.MODE_PRIVATE;

public class Setting_admin_add extends Fragment
{
    View view;
    Toolbar toolbar_admin_add;

    Button button_admin_add;
    Spinner spinner_post_admin_add;
    EditText edit_id_user_admin_add;

    // Массив должностей
    private final String[] adminRoles = {"Должность", "Владелец", "Администратор", "Модератор"};

    //загрузка
    SharedPreferences sharedPreferences_load_post;
    String post;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.setting_admin_add, container, false);

        // Устанавливаем название института в Toolbar
        toolbar_admin_add = view.findViewById(R.id.toolbar_admin_add);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_admin_add);
        // Добавление кнопки "Назад"
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Обработчик нажатия на кнопку "Назад"
        toolbar_admin_add.setNavigationOnClickListener(v ->
        {
            // Вызвать метод возврата на предыдущий фрагмент
            getActivity().onBackPressed();
        });

        // Загрузка из памяти должности пользователя
        sharedPreferences_load_post = requireContext().getSharedPreferences("select_post", MODE_PRIVATE);
        post = sharedPreferences_load_post.getString("Post", null);

        button_admin_add = view.findViewById(R.id.button_admin_add);
        spinner_post_admin_add = view.findViewById(R.id.spinner_post_admin_add);
        edit_id_user_admin_add = view.findViewById(R.id.edit_id_user_admin_add);

        // Установить адаптер для Spinner с выбором должности
        CustomArrayAdapter<String> adapter = new CustomArrayAdapter<>(requireContext(), R.layout.custom_spinner_item, adminRoles);
        adapter.setDropdownLayoutResource(R.layout.custom_spinner_dropdown_item);
        spinner_post_admin_add.setAdapter(adapter);
        // Установить "Должность" в качестве выбранного элемента по умолчанию
        spinner_post_admin_add.setSelection(0);

        // Установите слушатель выбора элемента
        spinner_post_admin_add.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                // Установите выбранный текст в Spinner
                spinner_post_admin_add.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView)
            {}
        });

        // Добавить обработчик нажатия на кнопку "Добавить"
        button_admin_add.setOnClickListener(v ->
        {
            if (isInternetAvailable())
            {
                // Получить текст из EditText и выбранную должность из Spinner
                String adminId = edit_id_user_admin_add.getText().toString();
                String adminRole = spinner_post_admin_add.getSelectedItem().toString();

                // Проверить, что выбранная должность не равна "Должность"
                if (!adminRole.equals("Должность") && !adminId.equals("") && !adminRole.equals(""))
                {
                    if (isValidUid(adminId))
                    {
                        // Вызвать метод для добавления админа с полученными данными
                        if (post.equals("Модератор") && adminRole.equals("Модератор") ||
                                post.equals("Администратор") && adminRole.equals("Администратор") || (post.equals("Администратор") && adminRole.equals("Модератор")) ||
                                post.equals("Владелец"))
                        {
                            showSelect(view.getContext(), adminId, adminRole);
                        }
                        else
                        {
                            showSnackbar("Ошибка. У вас не хватает прав");
                        }
                    }
                    else
                    {
                        showSnackbar("Ошибка. Некорректный формат UID");
                    }
                }
                else
                {
                    showSnackbar("Ошибка. Заполните все поля");
                }
            }
            else
            {
                showSnackbar("Ошибка. Отсутствует интернет");
            }

            //скрыть клавиатуру
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });

        return view;
    }

    // Метод для отображения диалога подтверждения
    private void showSelect(Context context, String UId, String Role)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null);
        builder.setView(dialogView);

        TextView title_alert_dialog = dialogView.findViewById(R.id.title_alert_dialog);
        TextView message_alert_dialog = dialogView.findViewById(R.id.message_alert_dialog);
        Button positive_button_alert_dialog = dialogView.findViewById(R.id.positive_button_alert_dialog);
        Button negative_button_alert_dialog = dialogView.findViewById(R.id.negative_button_alert_dialog);

        //вывести заголовок и текст
        title_alert_dialog.setText("Подтверждение");
        String txt = "Дать пользователю: " + UId + " права: " + Role + "?";
        message_alert_dialog.setText(txt);
        positive_button_alert_dialog.setText("Да");
        negative_button_alert_dialog.setText("Нет");

        AlertDialog dialog = builder.create();

        positive_button_alert_dialog.setOnClickListener(v ->
        {
            addAdmin(UId, Role);

            dialog.dismiss();
        });

        negative_button_alert_dialog.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // В обработчике кнопки "Добавить админа"
    private void addAdmin(String UId, String Role)
    {
        try
        {
            DatabaseReference adminReference = FirebaseDatabase.getInstance("https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("Пользователи").child("Админы");

            // Добавить нового админа
            adminReference.child(UId).setValue(Role);

            showSnackbar("Пользователь получил новые права");

            getActivity().getSupportFragmentManager().popBackStack();
        }
        catch (Exception e)
        {
            e.printStackTrace();

            showSnackbar("Произошла ошибка на стороне сервера");
        }
    }

    // Метод для проверки корректности формата UID
    private boolean isValidUid(String uid)
    {
        // UID должен быть числом и иметь длину 17 символов
        return uid != null && uid.length() == 17 && uid.matches("\\d+");
    }

    //Уведомление
    public void showSnackbar(String message)
    {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
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
