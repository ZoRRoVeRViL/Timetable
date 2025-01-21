package com.example.timetable.fragments_settings;

import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.ViewPropertyAnimator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Setting_groups_student extends Fragment
{
    private static final String TAG = "Setting_groups_student";
    private static final String NO_GROUP = "Такой группы в данном подразделении нет";
    private static final String SETTING_GROUP_PREF = "Setting-group-student";
    private View view;
    private RecyclerView recyclerview_groups;
    private RecyclerViewAdapterGroupsStudent recyclerViewAdapterGroupsStudent;
    private EditText edit_text_search_setting_group;
    private ImageView button_search_setting_group, button_clear_setting_group;
    private TextView no_personal_setting_group;
    private Toolbar toolbar_setting_groups;
    static public String unionName = "";
    private List<String> savedGroupList;
    private ViewPropertyAnimator currentAnimator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.setting_groups, container, false);
        initViews();
        setupToolbar();
        loadGroups();
        setupSearch();
        return view;
    }

    private void initViews() {
        recyclerview_groups = view.findViewById(R.id.recyclerview_setting_groups);
        recyclerview_groups.setLayoutManager(new LinearLayoutManager(getContext()));
        edit_text_search_setting_group = view.findViewById(R.id.edit_text_search_setting_group);
        no_personal_setting_group = view.findViewById(R.id.no_personal_setting_group);
        button_search_setting_group = view.findViewById(R.id.button_search_setting_group);
        button_clear_setting_group = view.findViewById(R.id.button_clear_setting_group);
        button_clear_setting_group.setVisibility(View.GONE);
        button_clear_setting_group.setRotation(45);
        recyclerViewAdapterGroupsStudent = new RecyclerViewAdapterGroupsStudent();
        recyclerview_groups.setAdapter(recyclerViewAdapterGroupsStudent);
    }

    private void setupToolbar()
    {
        toolbar_setting_groups = view.findViewById(R.id.toolbar_setting_groups);
        toolbar_setting_groups.setTitle(unionName);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_setting_groups);
        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            toolbar_setting_groups.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        }
    }

    private void loadGroups()
    {
        if (unionName != null && !unionName.isEmpty())
        {
            savedGroupList = loadGroupList(unionName);

            if (savedGroupList != null && !savedGroupList.isEmpty())
            {
                recyclerViewAdapterGroupsStudent.RecyclerViewAdapterGroups(savedGroupList);
            }
            else
            {
                showError("Нет групп в данном институте");
            }
        }
    }

    private void setupSearch()
    {
        edit_text_search_setting_group.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString();
                if (searchText.isEmpty())
                {
                    animateButtonVisibility(button_clear_setting_group, false);
                    animateButtonVisibility(button_search_setting_group, true);
                }
                else
                {
                    animateButtonVisibility(button_clear_setting_group, true);
                    animateButtonVisibility(button_search_setting_group, false);
                }

                performSearch(searchText);
            }
            @Override
            public void afterTextChanged(Editable s)
            {}
        });
        edit_text_search_setting_group.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(edit_text_search_setting_group.getText().toString());
                hideKeyboard();
                return true;
            }
            return false;
        });
        button_search_setting_group.setOnClickListener(v -> {
            performSearch(edit_text_search_setting_group.getText().toString());
            hideKeyboard();
        });

        button_clear_setting_group.setOnClickListener(v ->
        {
            edit_text_search_setting_group.setText("");
            animateButtonVisibility(button_clear_setting_group, false);
            animateButtonVisibility(button_search_setting_group, true);
            performSearch("");
        });
    }

    private void animateButtonVisibility(ImageView button, boolean show)
    {
        if (currentAnimator != null)
        {
            currentAnimator.cancel();
            button.clearAnimation();
        }

        if (show)
        {
            button.setVisibility(View.VISIBLE);
        }

        currentAnimator = button.animate().alpha(show ? 1f : 0f).setDuration(200);

        if (!show) {
            currentAnimator.setListener(new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation)
                {
                    button.setVisibility(View.GONE);
                    currentAnimator.setListener(null);
                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
        }

        currentAnimator.start();
    }

    private void performSearch(String searchText)
    {
        no_personal_setting_group.setVisibility(View.GONE);
        List<String> filteredList = filterGroups(searchText);
        recyclerViewAdapterGroupsStudent.RecyclerViewAdapterGroups(filteredList);

        if (filteredList.isEmpty())
        {
            no_personal_setting_group.setVisibility(View.VISIBLE);
            no_personal_setting_group.setText(NO_GROUP);
        }
    }
    private List<String> filterGroups(String searchText)
    {
        List<String> filteredList = new ArrayList<>();

        if (savedGroupList != null && !searchText.isEmpty())
        {
            String normalizedSearchText = searchText.replaceAll("-", " ").toLowerCase();
            for (String groupData : savedGroupList)
            {
                String[] parts = groupData.split(" \\| ");
                if (parts.length == 2) {
                    String groupName = parts[0].trim().replaceAll("-", " ").toLowerCase();

                    if (groupName.contains(normalizedSearchText)) {
                        filteredList.add(groupData);
                    }
                }
            }
        }
        else if (savedGroupList != null)
        {
            filteredList.addAll(savedGroupList);
        }

        return filteredList;
    }
    private void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private List<String> loadGroupList(String union)
    {
        List<String> processedList = new ArrayList<>();

        try
        {
            if (isAdded() && getContext() != null)
            {
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SETTING_GROUP_PREF + union, Context.MODE_PRIVATE);
                String jsonList = sharedPreferences.getString("group-list", null);
                if (jsonList != null)
                {
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<String>>()
                    {}.getType();
                    List<String> loadedList = gson.fromJson(jsonList, type);

                    if (loadedList != null)
                    {
                        for (String groupData : loadedList)
                        {
                            String[] parts = groupData.split(" \\| ");

                            if (parts.length == 5)
                            {
                                String group = parts[2].trim();
                                String link = parts[3].trim();
                                processedList.add(group + " | " + link);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error loading group list: " + e.getMessage());
            e.printStackTrace();
        }

        return processedList;
    }
    private void showError(String message)
    {
        if (getActivity() != null)
        {
            getActivity().runOnUiThread(() ->
            {
                View rootView = getActivity().findViewById(android.R.id.content);
                Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
            });
        }
    }
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }
}