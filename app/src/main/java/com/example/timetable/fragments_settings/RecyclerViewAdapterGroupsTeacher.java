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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class RecyclerViewAdapterGroupsTeacher extends RecyclerView.Adapter<RecyclerViewAdapterGroupsTeacher.MyViewHolder>
{
    private List<String> groupItemList = new ArrayList<>();
    List<String> name = new ArrayList<>();
    List<String> url = new ArrayList<>();
    String UNION;

    private static final String URL = "https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private static final String USERS = "Пользователи";
    private static final String GROUP = "Группа";
    private static final String DATE_REGISTRATION = "Дата регистрации";
    private static final String TXT_YOU_SELECT_GROUP = "Добро пожаловать ";
    private static final String REGULAR_USER = "Обычный пользователь";

    public void RecyclerViewAdapterGroupsTeacher(List<String> groupItemList,  String UNION)
    {
        this.groupItemList = groupItemList;
        this.UNION = UNION;

        // Очистка и обновление данных
        name.clear();
        url.clear();

        try
        {
            if (!groupItemList.isEmpty())
            {
                for (int i = 0; i < groupItemList.size(); i += 2)
                {
                    String name_str = groupItemList.get(i);
                    String url_str = groupItemList.get(i + 1);

                    if (!name_str.isEmpty() && !url_str.isEmpty())
                    {
                        name.add(name_str);
                        url.add(url_str);
                    }
                }

                notifyDataSetChanged();
            }
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
        return new MyViewHolder(view, UNION);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        holder.groups_name.setText(groupItemList.get(position));
        if (!groupItemList.isEmpty())
        {
            holder.groups_name.setText(name.get(position));//имя
            holder.id_name.setText(url.get(position));//ссылка
        }
    }

    @Override
    public int getItemCount()
    {
        return groupItemList.size()/2;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private TextView groups_name, id_name;
        private LinearLayout linear_layout_item_group;

        String UID, regDATE, role, UNION;

        public MyViewHolder(@NonNull View itemView, String union)
        {
            super(itemView);

            UNION = union;

            try
            {
                groups_name = itemView.findViewById(R.id.groups_name);
                id_name = itemView.findViewById(R.id.groups_link);
                id_name.setVisibility(View.VISIBLE);
                linear_layout_item_group = itemView.findViewById(R.id.linear_layout_item_group);

                linear_layout_item_group.setOnClickListener(this);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v)
        {
            try
            {
                String name = groups_name.getText().toString();
                String id = id_name.getText().toString();

                //Загрузка из памяти UID
                SharedPreferences sharedPreferences_load_uid = v.getContext().getSharedPreferences("UID_User", MODE_PRIVATE);
                UID = sharedPreferences_load_uid.getString("UID", null);

                //Загрузка из памяти даты регистрации
                SharedPreferences sharedPreferences_load_reg_date = v.getContext().getSharedPreferences("registration_Date", MODE_PRIVATE);
                regDATE = sharedPreferences_load_reg_date.getString("DATE", null);

                //Если UID нет то создать
                if (UID == null || UID.equals(""))
                {
                    //генерации уникального идентификатора
                    UID = generateUID();

                    //генерации даты регистрации
                    regDATE = registrationDate();

                    saveData("UID_User", "UID", UID, v.getContext());
                    saveData("registration_Date", "DATE", regDATE, v.getContext());
                }

                // Сохраняем выбранное название группы в SharedPreferences
                saveData("select_teacher_name", "Name", name, v.getContext());
                saveData("select_teacher_name", "id", id, v.getContext());
                saveData("select_teacher_name", "Union", UNION, v.getContext());

                //История выбранных групп
                addToSelectionHistory(name, id, v);

                // Загрузка из памяти должности пользователя
                SharedPreferences sharedPreferences_load_post = v.getContext().getSharedPreferences("select_post", MODE_PRIVATE);
                String post = sharedPreferences_load_post.getString("Post", null);
                if (post == null || post.equals(""))
                {
                    // Сохраняем новую должность пользователя
                    saveData("select_post", "Post", REGULAR_USER, v.getContext());
                }

                //Загрузка из памяти роли пользователя
                SharedPreferences sharedPreferences_load_role = v.getContext().getSharedPreferences("select_role", MODE_PRIVATE);
                role = sharedPreferences_load_role.getString("Role", null);

                //если роль выбрана, группа выбрана создать ветки польователя на сервере
                DatabaseReference database = FirebaseDatabase.getInstance(URL).getReference(USERS);
                // Загрузить этот UID в Firebase
                database.child(role).child(UID).child(GROUP).setValue(id + " | " + name);
                database.child(role).child(UID).child(DATE_REGISTRATION).setValue(regDATE);

                // отображения уведомления
                View rootView = ((AppCompatActivity) v.getContext()).findViewById(R.id.fragment_container);
                Snackbar snackbar = Snackbar.make(rootView, TXT_YOU_SELECT_GROUP + name, Snackbar.LENGTH_SHORT);
                snackbar.show();

                // Возвращаемся назад к предыдущему фрагменту
                FragmentManager fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                fragmentManager.popBackStackImmediate();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
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

        //история выбранных
        private void addToSelectionHistory(String Name, String URL, View view)
        {
            try
            {
                // Получиь текущую историю из SharedPreferences
                SharedPreferences sharedPreferences_history = view.getContext().getSharedPreferences("Group_History_teacher", MODE_PRIVATE);
                String selectionHistory = sharedPreferences_history.getString("History", "");

                // Добавить новый выбор в историю
                if (selectionHistory == null || selectionHistory.isEmpty())
                {
                    selectionHistory = URL + " | " + Name;
                }
                else
                {
                    selectionHistory = selectionHistory + "\n" + URL + " | " + Name;
                }

                // Сохранить обновленную историю в SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences_history.edit();
                editor.putString("History", selectionHistory);
                editor.apply();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
