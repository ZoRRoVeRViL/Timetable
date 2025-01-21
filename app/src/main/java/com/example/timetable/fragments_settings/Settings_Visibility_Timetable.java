package com.example.timetable.fragments_settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.timetable.R;
import com.google.android.material.snackbar.Snackbar;
import java.util.HashMap;
import java.util.Map;

public class Settings_Visibility_Timetable extends Fragment
{
    private static final String PREFS_NAME = "visibility_prefs";
    private static final String SHOW_TIME_KEY = "show_time";
    private static final String SHOW_SUBJECT_KEY = "show_subject";
    private static final String SHOW_TEACHER_KEY = "show_teacher";
    private static final String SHOW_TYPE_KEY = "show_type";
    private static final String SHOW_AUDITORY_KEY = "show_auditory";
    private View view;
    private SharedPreferences settingsPrefs;
    private final Map<String, ViewSettings> viewSettingsMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.settings_visibility_timetable, container, false);

        initViews();

        settingsPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadConfig();
        checkLayoutShow();

        return view;
    }

    private void initViews()
    {
        ImageView eyeShowTime = view.findViewById(R.id.eye_show_time_visibility_settings);
        ImageView eyeShowSubject = view.findViewById(R.id.eye_show_subject_visibility_settings);
        ImageView eyeShowTeacher = view.findViewById(R.id.eye_show_teacher_visibility_settings);
        ImageView eyeShowType = view.findViewById(R.id.eye_show_type_visibility_settings);
        ImageView eyeShowAuditory = view.findViewById(R.id.eye_show_auditory_visibility_settings);

        TextView time_lesson_item_timetable_student = view.findViewById(R.id.time_lesson_item_timetable_student);
        TextView name_lesson_item_timetable_student = view.findViewById(R.id.name_lesson_item_timetable_student);
        TextView form_lesson_item_timetable_student = view.findViewById(R.id.form_lesson_item_timetable_student);
        TextView teacher_lesson_item_timetable_student = view.findViewById(R.id.teacher_lesson_item_timetable_student);
        TextView auditorium_lesson_item_timetable_student = view.findViewById(R.id.auditorium_lesson_item_timetable_student);

        viewSettingsMap.put(SHOW_TIME_KEY, new ViewSettings(time_lesson_item_timetable_student, eyeShowTime));
        viewSettingsMap.put(SHOW_SUBJECT_KEY, new ViewSettings(name_lesson_item_timetable_student, eyeShowSubject));
        viewSettingsMap.put(SHOW_TEACHER_KEY, new ViewSettings(teacher_lesson_item_timetable_student, eyeShowTeacher));
        viewSettingsMap.put(SHOW_TYPE_KEY, new ViewSettings(form_lesson_item_timetable_student, eyeShowType));
        viewSettingsMap.put(SHOW_AUDITORY_KEY, new ViewSettings(auditorium_lesson_item_timetable_student, eyeShowAuditory));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        saveConfig();
    }

    private void checkLayoutShow()
    {
        Map<RelativeLayout, String> layoutKeyMap = new HashMap<>();
        layoutKeyMap.put(view.findViewById(R.id.layout_time_visibility_settings), SHOW_TIME_KEY);
        layoutKeyMap.put(view.findViewById(R.id.layout_subject_visibility_settings), SHOW_SUBJECT_KEY);
        layoutKeyMap.put(view.findViewById(R.id.layout_teacher_visibility_settings), SHOW_TEACHER_KEY);
        layoutKeyMap.put(view.findViewById(R.id.layout_type_visibility_settings), SHOW_TYPE_KEY);
        layoutKeyMap.put(view.findViewById(R.id.layout_auditory_visibility_settings), SHOW_AUDITORY_KEY);

        for(Map.Entry<RelativeLayout, String> entry : layoutKeyMap.entrySet())
        {
            RelativeLayout layout = entry.getKey();
            String key = entry.getValue();
            layout.setOnClickListener(v -> toggleVisibility(key));
        }
    }

    private void toggleVisibility(String key)
    {
        ViewSettings viewSettings = viewSettingsMap.get(key);

        if(viewSettings == null)
        {
            return;
        }

        boolean isCurrentlyVisible = settingsPrefs.getBoolean(key, true);

        if (isCurrentlyVisible && isOnlyOneLayoutVisible())
        {
            showSnackbar();

            return;
        }

        settingsPrefs.edit().putBoolean(key, !isCurrentlyVisible).apply();
        updateView(viewSettings.textView, !isCurrentlyVisible);
        updateImageView(viewSettings.imageView, !isCurrentlyVisible);
    }

    private void updateView(TextView textView, boolean isVisible)
    {
        if(isVisible)
        {
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        }
        else
        {
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
        }
    }

    private void updateImageView(ImageView imageView, boolean isVisible)
    {
        if(isVisible)
        {
            imageView.setImageResource(R.drawable.ic_eye);
        }
        else
        {
            imageView.setImageResource(R.drawable.ic_eye_off);
        }
    }

    private void loadConfig()
    {
        for(Map.Entry<String, ViewSettings> entry : viewSettingsMap.entrySet())
        {
            String key = entry.getKey();
            ViewSettings viewSettings = entry.getValue();
            boolean isVisible = settingsPrefs.getBoolean(key, true);
            updateView(viewSettings.textView, isVisible);
            updateImageView(viewSettings.imageView, isVisible);
        }
    }

    private static class ViewSettings
    {
        TextView textView;
        ImageView imageView;

        ViewSettings(TextView textView, ImageView imageView)
        {
            this.textView = textView;
            this.imageView = imageView;
        }
    }

    private void showSnackbar()
    {
        TextView errorMessageTextView = getView().findViewById(R.id.error_message_text_view);
        if(errorMessageTextView != null)
        {
            errorMessageTextView.setVisibility(View.VISIBLE);

            new Handler().postDelayed(() -> errorMessageTextView.setVisibility(View.GONE), 3000);
        }
    }

    private boolean isOnlyOneLayoutVisible()
    {
        int countVisible = 0;

        for (String key : viewSettingsMap.keySet())
        {
            if (settingsPrefs.getBoolean(key, true))
            {
                countVisible++;
            }
        }
        return countVisible == 1;
    }

    private void saveConfig()
    {
        SharedPreferences.Editor editor = settingsPrefs.edit();
        editor.apply();
    }
}