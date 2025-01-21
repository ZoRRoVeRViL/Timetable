package com.example.timetable.fragments_settings;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.timetable.MainActivity;
import com.example.timetable.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class Setting_config extends Fragment
{
    private static final String PREFS_NAME = "settings_prefs";
    private static final String LOG_KEY = "log_mode";
    private static final String SHOW_CURRENT_KEY = "show_current_lesson";
    private static final String NOTIFY_TASK_KEY = "notification_tasks";
    private static final String NOTIFY_ANNOUNCEMENT_KEY = "notification_announcements";
    private static final String NOTIFY_COUPLE_KEY = "notification_start_couple";
    private static final String MINUTES_BEFORE_KEY = "minutes_before_notification";
    private static final String MINUTES_BEFORE_SWITCH_KEY = "minutes_before_notification_switch";
    private static final String NOTIFICATION_SOUND = "notification_sound";
    private View view;
    private Toolbar toolbar_setting_config;
    private SwitchMaterial switchParse, switchShowCurrent, switchNotificationTasks,
            switchNotificationCouple, switchNotificationAnnouncement, switchMinutesBefore;
    private SeekBar seekBarMinutesBefore;
    private TextView seekBarProgressTextView;
    private Button selectSoundButton, visibilitySettingsButton;
    private SharedPreferences settingsPrefs;
    private static final String SNOW_ANIMATION_KEY = "snow_animation";
    private SwitchMaterial switchSnowAnimation;
    private SharedPreferences sharedPreferences_snow_animation;

    private MainActivity mainActivity;

    private final ActivityResultLauncher<Intent> ringtonePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
            {
                if(result.getResultCode() == Activity.RESULT_OK)
                {
                    Intent data = result.getData();

                    if(data != null && data.getData() != null)
                    {
                        Uri uri = data.getData();
                        String ringtonePath = uri.toString();
                        settingsPrefs.edit().putString(NOTIFICATION_SOUND, ringtonePath).apply();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.setting_config, container, false);

        initViews();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_setting_config);
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar_setting_config.setNavigationOnClickListener(v -> getActivity().onBackPressed());

        settingsPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupSwitchesColor();
        loadConfig();

        seekBarMinutesBefore.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                seekBarProgressTextView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                saveConfig();
            }
        });

        switchMinutesBefore.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            seekBarMinutesBefore.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            saveConfig();
        });

        loadConfig();

        visibilitySettingsButton.setOnClickListener(v -> openVisibilitySettings());
        selectSoundButton.setOnClickListener(v -> selectRingtone());
        switchSnowAnimation.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            saveConfig();
            if (mainActivity != null)
            {
                mainActivity.setSnowAnimation(isChecked);
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);

        if (context instanceof MainActivity)
        {
            mainActivity = (MainActivity) context;
        }
        else
        {
            // Обработка ситуации, когда контекст не является MainActivity
        }
    }

    private void initViews()
    {
        toolbar_setting_config = view.findViewById(R.id.toolbar_setting_config);
        switchParse = view.findViewById(R.id.switch_parse_setting_config);
        switchShowCurrent = view.findViewById(R.id.switch_current_lesson_setting_config);
        switchNotificationTasks = view.findViewById(R.id.switch_notification_task_setting_config);
        switchNotificationCouple = view.findViewById(R.id.switch_notification_couple_setting_config);
        switchNotificationAnnouncement = view.findViewById(R.id.switch_notification_announcement_setting_config);
        selectSoundButton = view.findViewById(R.id.select_sound_button_setting_config);
        seekBarMinutesBefore = view.findViewById(R.id.seekBar_minutes_before_setting_config);
        seekBarProgressTextView = view.findViewById(R.id.text_view_seekbar_setting_config);
        switchMinutesBefore = view.findViewById(R.id.switch_minutes_before_setting_config);
        visibilitySettingsButton = view.findViewById(R.id.visibility_settings_button_setting_config);
        switchSnowAnimation = view.findViewById(R.id.switch_snow_animation_setting_config);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        saveConfig();
    }

    private void openVisibilitySettings()
    {
        BottomSheetVisibilitySettings bottomSheet = new BottomSheetVisibilitySettings();
        bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
    }

    private void selectRingtone()
    {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
        {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},10);
        }

        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);

        if(intent.resolveActivity(requireContext().getPackageManager())!=null)
            ringtonePickerLauncher.launch(intent);
    }

    private void loadConfig()
    {
        boolean logMode = settingsPrefs.getBoolean(LOG_KEY, false);
        switchParse.setChecked(logMode);
        boolean showCurrentLesson = settingsPrefs.getBoolean(SHOW_CURRENT_KEY, true);
        switchShowCurrent.setChecked(showCurrentLesson);
        boolean notificationTasks = settingsPrefs.getBoolean(NOTIFY_TASK_KEY, false);
        switchNotificationTasks.setChecked(notificationTasks);
        boolean notificationAnnouncements = settingsPrefs.getBoolean(NOTIFY_ANNOUNCEMENT_KEY, false);
        switchNotificationAnnouncement.setChecked(notificationAnnouncements);
        boolean notificationStartCouples = settingsPrefs.getBoolean(NOTIFY_COUPLE_KEY, false);
        switchNotificationCouple.setChecked(notificationStartCouples);
        int minutesBeforeNotification = settingsPrefs.getInt(MINUTES_BEFORE_KEY, 0);
        seekBarMinutesBefore.setProgress(minutesBeforeNotification);
        seekBarProgressTextView.setText(String.valueOf(minutesBeforeNotification));
        boolean switchMinutesBeforeState = settingsPrefs.getBoolean(MINUTES_BEFORE_SWITCH_KEY, false);
        switchMinutesBefore.setChecked(switchMinutesBeforeState);
        seekBarMinutesBefore.setVisibility(switchMinutesBeforeState ? View.VISIBLE : View.GONE);
        boolean isSnowAnimationEnabled = settingsPrefs.getBoolean(SNOW_ANIMATION_KEY, true);
        switchSnowAnimation.setChecked(isSnowAnimationEnabled);
        setSwitchState(switchSnowAnimation, isSnowAnimationEnabled);

        switchParse.setChecked(logMode);
        setSwitchState(switchParse, logMode);
        switchMinutesBefore.setChecked(switchMinutesBeforeState);
        setSwitchState(switchMinutesBefore, switchMinutesBeforeState);

        seekBarMinutesBefore.setVisibility(switchMinutesBeforeState ? View.VISIBLE : View.GONE);
    }

    private void saveConfig()
    {
        SharedPreferences.Editor editor = settingsPrefs.edit();
        editor.putBoolean(LOG_KEY, switchParse.isChecked());
        editor.putBoolean(SHOW_CURRENT_KEY, switchShowCurrent.isChecked());
        editor.putBoolean(NOTIFY_TASK_KEY, switchNotificationTasks.isChecked());
        editor.putBoolean(NOTIFY_ANNOUNCEMENT_KEY, switchNotificationAnnouncement.isChecked());
        editor.putBoolean(NOTIFY_COUPLE_KEY, switchNotificationCouple.isChecked());
        editor.putInt(MINUTES_BEFORE_KEY, seekBarMinutesBefore.getProgress());
        editor.putBoolean(MINUTES_BEFORE_SWITCH_KEY, switchMinutesBefore.isChecked());
        editor.putBoolean(SNOW_ANIMATION_KEY, switchSnowAnimation.isChecked());
        editor.apply();
    }

    private void setSwitchColors(SwitchMaterial switchMaterial)
    {
        int activeColor = ContextCompat.getColor(requireContext(), R.color.teal_200);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.gray);

        ColorStateList thumbColorStateList = new ColorStateList(new int[][]
                {
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]
                        {
                                inactiveColor,
                                activeColor
                        }
                );

        ColorStateList trackColorStateList = new ColorStateList(new int[][]
                {
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]
                        {
                                Color.parseColor("#E0E0E0"),
                                activeColor
                        }
                );

        switchMaterial.setThumbTintList(thumbColorStateList);
        switchMaterial.setTrackTintList(trackColorStateList);
    }

    private void setSwitchState(SwitchMaterial switchMaterial, boolean state)
    {
        switchMaterial.setChecked(state);
    }

    private void setupSwitchesColor()
    {
        setSwitchColors(switchParse);
        setSwitchColors(switchShowCurrent);
        setSwitchColors(switchNotificationTasks);
        setSwitchColors(switchNotificationAnnouncement);
        setSwitchColors(switchNotificationCouple);
        setSwitchColors(switchMinutesBefore);
        setSwitchColors(switchSnowAnimation);
    }
}