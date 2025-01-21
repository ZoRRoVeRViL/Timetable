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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class RecyclerViewAdapterUnion extends RecyclerView.Adapter<RecyclerViewAdapterUnion.MyViewHolder> {

    private List<String> unionItemList = new ArrayList<>();
    private Map<String, String> instituteDateMap = new LinkedHashMap<>();
    private final static String SELECT_ROLE_PREFS = "select_role";
    private final static String ROLE_KEY = "Role";


    // Использование метода setUnionItemList
    public void RecyclerViewAdapterUnion(List<String> unionItemList) {
        this.unionItemList = unionItemList;
        this.instituteDateMap = getInstituteDateMap(unionItemList);
        notifyDataSetChanged();
    }
    // Метод для получения уникальных институтов и дат
    private Map<String,String> getInstituteDateMap(List<String> unionItemList)
    {
        Map<String, String> tempInstituteDateMap = new LinkedHashMap<>();
        for (String unionData : unionItemList)
        {
            if (unionData != null)
            {
                String[] parts = unionData.split(" \\| ");

                if (parts.length == 5)
                {
                    String institute = parts[0].trim();
                    String formattedDate = parts[4].trim();
                    tempInstituteDateMap.put(institute, formattedDate);
                }
            }
        }
        return tempInstituteDateMap;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_union, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String institute = new ArrayList<>(instituteDateMap.keySet()).get(position);
        String date = instituteDateMap.get(institute);

        holder.setUnionName(institute);
        holder.unionNameTextView.setText(institute);
        holder.unionDateTextView.setText(date);
    }

    @Override
    public int getItemCount() {
        return instituteDateMap.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView unionNameTextView;
        private final TextView unionDateTextView;
        private final LinearLayout linear_layout_item_union;
        private final Context context;
        private String unionName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            unionNameTextView = itemView.findViewById(R.id.union_name);
            unionDateTextView = itemView.findViewById(R.id.union_date);
            linear_layout_item_union = itemView.findViewById(R.id.linear_layout_item_union);
            linear_layout_item_union.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //Загрузка из памяти роли пользователя
            SharedPreferences sharedPreferencesLoadRole = context.getSharedPreferences(SELECT_ROLE_PREFS, MODE_PRIVATE);
            String role = sharedPreferencesLoadRole.getString(ROLE_KEY, null);

            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            if (role != null) {
                if (role.equals("Студент")) {
                    navigateToFragment(fragmentTransaction, new Setting_groups_student(),unionName);

                } else if (role.equals("Преподаватель")) {
                    navigateToFragment(fragmentTransaction, new Setting_groups_teacher(),unionName);
                }
            }
        }


        private  void navigateToFragment(FragmentTransaction fragmentTransaction, Object Fragment, String unionName) {
            if(Fragment instanceof Setting_groups_student){
                Setting_groups_student fragment = (Setting_groups_student) Fragment;
                Setting_groups_student.unionName = unionName;
            }
            else if(Fragment instanceof Setting_groups_teacher){
                Setting_groups_teacher fragment = (Setting_groups_teacher) Fragment;
                Setting_groups_teacher.UNION = unionName;
            }
            fragmentTransaction.replace(R.id.fragment_container, (androidx.fragment.app.Fragment) Fragment).addToBackStack(null).commit();
        }


        public void setUnionName(String name) {
            unionName = name;
        }
    }
}