package com.example.timetable.fragments_settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.timetable.R;
import com.google.android.material.snackbar.Snackbar;

public class Setting_help extends Fragment
{
    View view;

    Toolbar toolbar_setting_help;

    Button open_WhatsApp_help, open_telegram_help, open_mail_help;

    String number = "+79841132824";
    String mail = "zorrovevil1@gmail.com";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.setting_help, container, false);

        open_WhatsApp_help = view.findViewById(R.id.open_WhatsApp_help);
        open_telegram_help = view.findViewById(R.id.open_telegram_help);
        open_mail_help = view.findViewById(R.id.open_mail_help);

        // Устанавливаем название института в Toolbar
        toolbar_setting_help = view.findViewById(R.id.toolbar_setting_help);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_setting_help);
        // Добавление кнопки "Назад"
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Обработчик нажатия на кнопку "Назад"
        toolbar_setting_help.setNavigationOnClickListener(v ->
        {
            // Вызвать метод возврата на предыдущий фрагмент
            getActivity().onBackPressed();
        });

        // Открыть чат в WhatsApp
        open_WhatsApp_help.setOnClickListener(v -> openWhatsAppChat());

        // Открыть чат в Telegram
        open_telegram_help.setOnClickListener(v -> openTelegramChat());

        // Открыть почтовый клиент для отправки письма
        open_mail_help.setOnClickListener(v -> sendEmail());

        return view;
    }

    // Метод для открытия чата в WhatsApp
    private void openWhatsAppChat()
    {
        try
        {
            Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=" + number);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            Snackbar snackbar = Snackbar.make(view, "WhatsApp не установлен", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    // Метод для открытия чата в Telegram
    private void openTelegramChat()
    {
        try
        {
            Uri uri = Uri.parse("https://t.me/" + number);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            Snackbar snackbar = Snackbar.make(view, "Telegram не установлен", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    // Метод для отправки письма по электронной почте
    private void sendEmail()
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mail});

        try
        {
            startActivity(Intent.createChooser(intent, "Отправить письмо"));
        }
        catch (ActivityNotFoundException e)
        {
            Snackbar snackbar = Snackbar.make(view, "Почтовый клиент не установлен", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }
}
