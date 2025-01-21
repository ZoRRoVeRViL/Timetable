package com.example.timetable.fragments_settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.example.timetable.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetVisibilitySettings extends BottomSheetDialogFragment
{
    private ImageView closeButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.bottom_sheet_visibility_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        closeButton = view.findViewById(R.id.close_button_bottom_sheet);

        if (getChildFragmentManager().findFragmentById(R.id.bottom_sheet_container) == null)
        {
            getChildFragmentManager().beginTransaction().add(R.id.bottom_sheet_container, new Settings_Visibility_Timetable()).commit();
        }

        closeButton.setOnClickListener(v -> dismiss());
    }
}