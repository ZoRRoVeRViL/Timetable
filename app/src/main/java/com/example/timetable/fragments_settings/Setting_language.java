package com.example.timetable.fragments_settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.timetable.R;

public class Setting_language extends Fragment
{
    View view;

    Toolbar toolbar_setting_language;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.setting_language, container, false);

        // Устанавливаем название института в Toolbar
        toolbar_setting_language = view.findViewById(R.id.toolbar_setting_language);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_setting_language);
        // Добавление кнопки "Назад"
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Обработчик нажатия на кнопку "Назад"
        toolbar_setting_language.setNavigationOnClickListener(v ->
        {
            // Вызвать метод возврата на предыдущий фрагмент
            getActivity().onBackPressed();
        });

        return view;
    }
}
