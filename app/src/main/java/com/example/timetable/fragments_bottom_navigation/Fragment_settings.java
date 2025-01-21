package com.example.timetable.fragments_bottom_navigation;

import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.ViewPropertyAnimator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.timetable.R;
import com.example.timetable.fragments_settings.Setting_admin;
import com.example.timetable.fragments_settings.Setting_config;
import com.example.timetable.fragments_settings.Setting_help;
import com.example.timetable.fragments_settings.Setting_info;
import com.example.timetable.fragments_settings.Setting_language;
import com.example.timetable.fragments_settings.Setting_profile;
import com.example.timetable.fragments_settings.Setting_reference;
import com.example.timetable.fragments_settings.Setting_theme;
import com.example.timetable.fragments_settings.Setting_union_student;
import com.example.timetable.fragments_settings.Setting_union_teacher;
import com.example.timetable.fragments_settings.Setting_widget;

import java.util.Objects;

public class Fragment_settings extends Fragment implements View.OnClickListener
{
    private View view, control_setting_view;
    private String GroupName, loadedName, userPost, role;
    private TextView setting_select_group_name, setting_select_teacher_name;
    private TextView logo_title_back_setting, logo_subtitle_back_setting, logo_title_front_setting, logo_subtitle_front_setting;
    private LinearLayout union_teacher_setting, union_student_setting, control_setting;
    private LinearLayout logo_layout_front_setting, logo_layout_back_setting;
    private static final String BOSS = "Владелец";
    private static final String MODER = "Модератор";
    private static final String ADMIN = "Администратор";
    private static final String STUDENT_NODE = "Студент";
    private static final String TEACHER_NODE = "Преподаватель";
    private static final String SELECT_UNION = "Выбрать учебное подразделение";
    private static final String SELECT_NAME = "Выбрать ФИО преподователя";
    private static final String GROUP = "Группа: ";
    private static final int speedLogoRotation = 400;
    private RelativeLayout logo_frame_setting;
    private ImageView logo_image_back_setting, logo_image_front_setting;
    private ViewPropertyAnimator currentLogoAnimator;
    private String logoRotation = "front";
    private boolean clickAnimating = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        initViews();
        loadSharedPreferencesData();
        loadDataFromSharedPreferences();
        setClickListeners();
        setupLogoAnimation();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (Objects.equals(logoRotation, "front"))
        {
            logo_layout_back_setting.setVisibility(View.INVISIBLE);
            logo_layout_front_setting.setVisibility(View.VISIBLE);

            logo_image_front_setting.setImageResource(R.drawable.nefu_logo);
            logo_title_front_setting.setText("СВФУ");
            logo_subtitle_front_setting.setText("Северо-Восточный \nфедеральный университет");
        }
        else
        {
            logo_frame_setting.setRotationY(180f);
            logo_layout_front_setting.setVisibility(View.INVISIBLE);
            logo_layout_back_setting.setVisibility(View.VISIBLE);

            logo_image_back_setting.setImageResource(R.drawable.fti_logo);
            logo_title_back_setting.setText("ФТИ");
            logo_subtitle_back_setting.setText("Физико-технический институт");
        }
    }

    private void initViews()
    {
        setting_select_group_name = view.findViewById(R.id.setting_select_group_name);
        setting_select_teacher_name = view.findViewById(R.id.setting_select_teacher_name);
        union_teacher_setting = view.findViewById(R.id.union_teacher_setting);
        union_student_setting = view.findViewById(R.id.union_student_setting);
        logo_frame_setting = view.findViewById(R.id.logo_frame_setting);
        logo_layout_front_setting = view.findViewById(R.id.logo_layout_front_setting);
        logo_layout_back_setting = view.findViewById(R.id.logo_layout_back_setting);
        logo_image_back_setting = view.findViewById(R.id.logo_image_back_setting);
        logo_image_front_setting = view.findViewById(R.id.logo_image_front_setting);
        logo_title_back_setting = view.findViewById(R.id.logo_title_back_setting);
        logo_title_front_setting = view.findViewById(R.id.logo_title_front_setting);
        logo_subtitle_front_setting = view.findViewById(R.id.logo_subtitle_front_setting);
        logo_subtitle_back_setting = view.findViewById(R.id.logo_subtitle_back_setting);
        logo_layout_back_setting.setVisibility(View.INVISIBLE);
        control_setting = view.findViewById(R.id.control_setting);
        control_setting_view = view.findViewById(R.id.control_setting_view);
    }

    private void loadSharedPreferencesData()
    {
        GroupName = loadSharedPreferencesSample("select_group", "GroupName");
        if (GroupName != null)
        {
            GroupName = GroupName.replace("\\\\*", "/");
        }

        loadedName = loadSharedPreferencesSample("select_teacher_name", "Name");
        userPost = loadSharedPreferencesSample("select_post", "Post");
        role = loadSharedPreferencesSample("select_role", "Role");

        String localLogoRotation = loadSharedPreferencesSample("select_logo_rotation", "Rotate");
        if (localLogoRotation != null)
        {
            logoRotation = localLogoRotation;
        }
    }

    private String loadSharedPreferencesSample(String main, String key)
    {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(main, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    private void saveSharedPreferencesSample(String string)
    {
        SharedPreferences prefs = requireContext().getSharedPreferences("select_logo_rotation", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Rotate", string);
        editor.apply();
    }

    private void loadDataFromSharedPreferences()
    {
        if (userPost != null && (userPost.equals(BOSS) || userPost.equals(MODER) || userPost.equals(ADMIN)))
        {
            control_setting.setVisibility(View.VISIBLE);
            control_setting_view.setVisibility(View.VISIBLE);
        }

        if (role != null)
        {
            if (role.equals(STUDENT_NODE))
            {
                union_student_setting.setVisibility(View.VISIBLE);
                if (GroupName == null || GroupName.isEmpty())
                {
                    setting_select_group_name.setText(SELECT_UNION);
                }
                else
                {
                    String group_name = GROUP + GroupName;
                    setting_select_group_name.setText(group_name);
                }
            }
            else if (role.equals(TEACHER_NODE))
            {
                union_teacher_setting.setVisibility(View.VISIBLE);
                if (loadedName == null || loadedName.isEmpty())
                {
                    setting_select_teacher_name.setText(SELECT_NAME);
                }
                else
                {
                    setting_select_teacher_name.setText(loadedName);
                }
            }
        }
    }

    private void setClickListeners()
    {
        int[] buttonIds = {R.id.union_student_setting, R.id.union_teacher_setting, R.id.profile_setting, R.id.control_setting, R.id.configurate_setting,
                R.id.them_setting, R.id.language_setting, R.id.widget_setting, R.id.help_setting, R.id.reference_setting, R.id.info_setting};
        for (int buttonId : buttonIds)
        {
            View button = view.findViewById(buttonId);
            button.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view)
    {
        int selectedButtonId = view.getId();
        AppCompatActivity activity = (AppCompatActivity) getContext();
        Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment != null && currentFragment.getId() == selectedButtonId)
        {
            return;
        }
        openFragment(activity, selectedButtonId);
    }

    private void openFragment(AppCompatActivity activity, int selectedButtonId)
    {
        Fragment selectedFragment = null;

        switch(selectedButtonId)
        {
            case R.id.union_student_setting:
                selectedFragment = new Setting_union_student();
                break;
            case R.id.union_teacher_setting:
                selectedFragment = new Setting_union_teacher();
                break;
            case R.id.profile_setting:
                selectedFragment = new Setting_profile();
                break;
            case R.id.control_setting:
                selectedFragment = new Setting_admin();
                break;
            case R.id.configurate_setting:
                selectedFragment = new Setting_config();
                break;
            case R.id.them_setting:
                selectedFragment = new Setting_theme();
                break;
            case R.id.language_setting:
                selectedFragment = new Setting_language();
                break;
            case R.id.widget_setting:
                selectedFragment = new Setting_widget();
                break;
            case R.id.help_setting:
                selectedFragment = new Setting_help();
                break;
            case R.id.reference_setting:
                selectedFragment = new Setting_reference();
                break;
            case R.id.info_setting:
                selectedFragment = new Setting_info();
                break;
        }

        if (selectedFragment != null)
        {
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).addToBackStack(null).commit();
        }
    }

    private void setupLogoAnimation()
    {
        logo_frame_setting.setOnClickListener(v -> startLogoAnimation());
    }

    private void startLogoAnimation()
    {
        if(clickAnimating) return;
        clickAnimating = true;

        if (currentLogoAnimator != null)
        {
            currentLogoAnimator.cancel();
            logo_frame_setting.clearAnimation();
        }

        float startRotation = logo_frame_setting.getRotationY();
        float endRotation = startRotation + 180f;

        currentLogoAnimator = logo_frame_setting.animate().rotationY(endRotation).setDuration(speedLogoRotation).setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                if(Objects.equals(logoRotation, "front"))
                {
                    new Handler().postDelayed(() ->
                    {
                        logo_layout_back_setting.setVisibility(View.VISIBLE);
                        logo_layout_front_setting.setVisibility(View.INVISIBLE);
                        }, speedLogoRotation/2);
                    logoRotation = "back";
                    saveSharedPreferencesSample("back");
                }
                else
                {
                    new Handler().postDelayed(() ->
                    {
                        logo_layout_back_setting.setVisibility(View.INVISIBLE);
                        logo_layout_front_setting.setVisibility(View.VISIBLE);
                        }, speedLogoRotation/2);
                    logoRotation = "front";
                    saveSharedPreferencesSample("front");
                }
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                clickAnimating = false;
            }
            @Override
            public void onAnimationCancel(Animator animation)
            {
                clickAnimating = false;
            }
            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
        currentLogoAnimator.start();
    }
}