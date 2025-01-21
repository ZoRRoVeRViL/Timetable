package com.example.timetable.fragments_settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class RecyclerViewAdapterGroupsStudent extends RecyclerView.Adapter<RecyclerViewAdapterGroupsStudent.MyViewHolder>
{
    private List<String> groupItemList = new ArrayList<>();

    private static final String URL = "https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String TXT_UNION = "Подразделения";
    private static final String USERS = "Пользователи";
    private static final String GROUP = "Группа";
    private static final String TASKS = "Задания";
    private static final String EMPTY = "Пусто";
    private static final String DATE_REGISTRATION = "Дата регистрации";
    private static final String TXT_YOU_SELECT_GROUP = "Вы выбрали группу: ";
    private static final String REGULAR_USER = "Обычный пользователь";
    private static final String UID_PREF = "UID_User";
    private static final String DATE_PREF = "registration_Date";
    private static final String SELECT_GROUP_PREF = "select_group";
    private static final String SELECT_POST_PREF = "select_post";
    private static final String SELECT_ROLE_PREF = "select_role";
    private static final String GROUP_HISTORY_PREF = "Group_History_student";
    private static final String HISTORY_KEY = "History";

    private String formattedUnionName;

    public void RecyclerViewAdapterGroups(List<String> groupItemList)
    {
        try
        {
            this.groupItemList = groupItemList;
            notifyDataSetChanged();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_groups, parent, false);
        return new MyViewHolder(view, groupItemList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        String groupData = groupItemList.get(position);
        if (groupData != null)
        {
            String[] parts = groupData.split(" \\| ");

            if(parts.length == 2)
            {
                String group = parts[0].trim();
                String link = parts[1].trim();
                holder.groupNameTextView.setText(group);
                holder.groupLinkTextView.setText(link);
                holder.setLink(link);
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return groupItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private TextView groupNameTextView;
        private TextView groupLinkTextView;
        private LinearLayout linear_layout_item_group;
        private List<String> groupItemList;
        private String link;
        String UID, regDATE, role;

        public MyViewHolder(@NonNull View itemView, List<String> groupItemList)
        {
            super(itemView);
            this.groupItemList = groupItemList;
            groupNameTextView = itemView.findViewById(R.id.groups_name);
            groupLinkTextView = itemView.findViewById(R.id.groups_link);
            linear_layout_item_group = itemView.findViewById(R.id.linear_layout_item_group);
            linear_layout_item_group.setOnClickListener(this);
            formattedUnionName = Setting_groups_student.unionName.replace("/", "\\\\*");
        }

        public void setLink(String link)
        {
            this.link = link;
        }

        @Override
        public void onClick(View v)
        {
            try
            {
                if (v == linear_layout_item_group)
                {
                    String groupName = groupNameTextView.getText().toString();
                    String linkData = groupLinkTextView.getText().toString();
                    String groupUnion = formattedUnionName;

                    //загрузка из памяти
                    loadDataFromSharedPref(v.getContext());

                    if (groupName != null && linkData != null)
                    {
                        String formattedGroupName = groupName.replace("/", "\\\\*");
                        if (UID == null || UID.isEmpty())
                        {
                            UID = generateUID();
                            regDATE = registrationDate();
                            saveData(UID_PREF, "UID", UID, v.getContext());
                            saveData(DATE_PREF, "DATE", regDATE, v.getContext());
                        }
                        saveData(SELECT_GROUP_PREF, "GroupName", formattedGroupName, v.getContext());
                        saveData(SELECT_GROUP_PREF, "GroupUnion", groupUnion, v.getContext());
                        saveData(SELECT_GROUP_PREF, "GroupLink", linkData, v.getContext());

                        addToSelectionHistory(formattedGroupName, groupUnion, v);
                        // Загрузка из памяти должности пользователя
                        SharedPreferences sharedPreferences_load_post = v.getContext().getSharedPreferences(SELECT_POST_PREF, MODE_PRIVATE);
                        String post = sharedPreferences_load_post.getString("Post", null);
                        if (post == null || post.isEmpty())
                        {
                            saveData(SELECT_POST_PREF, "Post", REGULAR_USER, v.getContext());
                        }

                        //создание Firebase Realtime Database
                        createFirebaseDatabase(formattedGroupName, groupUnion, v);

                        //Загрузка из памяти роли пользователя
                        SharedPreferences sharedPreferences_load_role = v.getContext().getSharedPreferences(SELECT_ROLE_PREF, MODE_PRIVATE);
                        role = sharedPreferences_load_role.getString("Role", null);

                        //если роль выбрана, группа выбрана создать ветки польователя на сервере
                        createFirebaseUser(formattedGroupName, groupUnion, v);
                        showSnackbar(formattedGroupName, v);
                        navigateBack(v);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        //загрузка из памяти
        private void loadDataFromSharedPref(Context v)
        {
            SharedPreferences sharedPreferences_load_uid = v.getSharedPreferences(UID_PREF, MODE_PRIVATE);
            UID = sharedPreferences_load_uid.getString("UID", null);
            SharedPreferences sharedPreferences_load_reg_date = v.getSharedPreferences(DATE_PREF, MODE_PRIVATE);
            regDATE = sharedPreferences_load_reg_date.getString("DATE", null);
        }

        // создать Firebase Realtime Database
        private void createFirebaseDatabase(String groupName, String groupUnion, View v)
        {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance(URL).getReference(TXT_UNION).child(groupUnion).child(groupName);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if (!dataSnapshot.exists())
                    {
                        databaseReference.child(TASKS).setValue(EMPTY);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {}
            });
        }

        // создать Firebase Realtime Database user
        private void createFirebaseUser(String groupName, String groupUnion, View v)
        {
            DatabaseReference database = FirebaseDatabase.getInstance(URL).getReference(USERS);
            // Загрузить этот UID в Firebase
            database.child(role).child(UID).child(GROUP).setValue(groupUnion + " | " + groupName);
            database.child(role).child(UID).child(DATE_REGISTRATION).setValue(regDATE);
        }

        // сохранить в SharedPreferences
        private void saveData(String data_sharedPreferences, String putString, String data, Context v)
        {
            try
            {
                SharedPreferences sharedPreferences = v.getSharedPreferences(data_sharedPreferences, MODE_PRIVATE);
                SharedPreferences.Editor editor_save_UID = sharedPreferences.edit();
                editor_save_UID.putString(putString, data);
                editor_save_UID.apply();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        // Метод для генерации уникального идентификатора
        private String generateUID()
        {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat gen_date = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
            return gen_date.format(calendar.getTime());
        }

        // Метод для генерации даты регистрации
        private String registrationDate()
        {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat reg_date = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.getDefault());
            return reg_date.format(calendar.getTime());
        }

        //история выбранных групп
        private void addToSelectionHistory(String groupName, String groupUnion, View view)
        {
            try
            {
                SharedPreferences sharedPreferences_history = view.getContext().getSharedPreferences(GROUP_HISTORY_PREF, MODE_PRIVATE);
                String selectionHistory = sharedPreferences_history.getString(HISTORY_KEY, "");

                if (selectionHistory == null || selectionHistory.isEmpty())
                {
                    selectionHistory = groupUnion + " | " + groupName;
                }
                else
                {
                    selectionHistory = selectionHistory + "\n" + groupUnion + " | " + groupName;
                }
                SharedPreferences.Editor editor = sharedPreferences_history.edit();
                editor.putString(HISTORY_KEY, selectionHistory);
                editor.apply();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        // отображения уведомления
        private void showSnackbar(String groupName, View v)
        {
            View rootView = ((AppCompatActivity) v.getContext()).findViewById(R.id.fragment_container);
            Snackbar snackbar = Snackbar.make(rootView, TXT_YOU_SELECT_GROUP + groupName, Snackbar.LENGTH_SHORT);
            snackbar.show();
        }

        // навигация назад
        private void navigateBack(View v)
        {
            FragmentManager fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
            fragmentManager.popBackStackImmediate();
        }
    }
}