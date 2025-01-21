package com.example.timetable.fragments_settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.timetable.R;

public class Setting_admin extends Fragment implements View.OnClickListener
{
    View view;

    Toolbar toolbar_setting_admin;

    //Должность
    TextView post_setting_admin;

    //фрагмент для смены окон в настройках
    Fragment selectedFragment;

    //загрузка
    SharedPreferences sharedPreferences_load_post;

    private static final String SELECT_GROUP_NODE = "Выберите свою группу в настройках";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.setting_admin, container, false);

        // Устанавливаем название института в Toolbar
        toolbar_setting_admin = view.findViewById(R.id.toolbar_setting_admin);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_setting_admin);
        // Добавление кнопки "Назад"
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Обработчик нажатия на кнопку "Назад"
        toolbar_setting_admin.setNavigationOnClickListener(v -> getActivity().onBackPressed());

        //Должность
        post_setting_admin = view.findViewById(R.id.post_setting_admin);

        // Загрузка из памяти должности пользователя
        sharedPreferences_load_post = requireContext().getSharedPreferences("select_post", Context.MODE_PRIVATE);
        String userPost = sharedPreferences_load_post.getString("Post", null);
        if (userPost != null)
        {
            post_setting_admin.setText(userPost);
        }
        else
        {
            post_setting_admin.setText(SELECT_GROUP_NODE);
        }

        //все кнопки и устанавить для них обработчик нажатия
        setupButtonListeners();

        return view;
    }

    //нажатие на кнопки
    private void setupButtonListeners()
    {
        view.findViewById(R.id.admins_setting_admin).setOnClickListener(this);
        view.findViewById(R.id.users_setting_admin).setOnClickListener(this);
        view.findViewById(R.id.deleted_users_setting_admin).setOnClickListener(this);
        view.findViewById(R.id.settings_setting_admin).setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        selectedFragment = null;
        AppCompatActivity activity = (AppCompatActivity) getContext();

        if (activity == null)
        {
            return;
        }

        // Получаем ID выбранной кнопки
        int selectedButtonId = view.getId();

        // Проверяем, находится ли уже выбранный фрагмент в стеке
        Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        // Если текущий фрагмент не null и его ID соответствует выбранной кнопке, то не открываем его снова
        if (currentFragment != null && currentFragment.getId() == selectedButtonId)
        {
            return;
        }

        // Создаем новый фрагмент в зависимости от выбранной кнопки
        switch(view.getId())
        {
            case R.id.admins_setting_admin:
                selectedFragment = new Setting_admin_all();
                break;
            case R.id.users_setting_admin:
                selectedFragment = new Setting_all_users();
                break;
            case R.id.deleted_users_setting_admin:
                selectedFragment = new Setting_deleted_users();
                break;
            case R.id.settings_setting_admin:
                selectedFragment = new Setting_admin_panel();
                break;
        }

        // Открываем новый фрагмент, если он был выбран
        if (selectedFragment != null)
        {
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).addToBackStack(null).commit();
        }
    }
}