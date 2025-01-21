package com.example.timetable.fragments_staff;

import android.content.Context;
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
import java.util.List;

public class RecyclerViewAdapterUnionStaff extends RecyclerView.Adapter<RecyclerViewAdapterUnionStaff.MyViewHolder>
{
    private final List<String> Links = new ArrayList<>();
    private final List<String> Name = new ArrayList<>();

    public void RecyclerViewAdapterUnionStaff(List<String> union_staffItemList)
    {
        for (String item : union_staffItemList)
        {
            if (item.contains("https"))
            {
                Links.add(item);
            }
            else
            {
                Name.add(item);
            }
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        int layoutRes = (viewType == 0) ? R.layout.list_item_staff_union_main : R.layout.list_item_staff_union;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);

        return new MyViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        String link = Links.get(position);
        String name = Name.get(position);
        if (!isSpecialCategory(name))
        {
            holder.union_staff_name.setText(name);
            holder.union_staff_name_address.setText(link);
        }
        else
        {
            holder.union_staff_name.setText(name);
            holder.union_staff_name_address.setText("");
        }
    }

    @Override
    public int getItemCount()
    {
        return Name.size();
    }

    private boolean isSpecialCategory(String name)
    {
        return name.matches("(Институты|Факультеты|Филиалы|Колледжи|Общеуниверситетские кафедры|Дополнительное образование|" +
                "Инклюзивное образование|НИИ|Центры, полигоны, лаборатории|Среднее общее образование)");
    }

    //разные layout
    @Override
    public int getItemViewType(int position)
    {
        return isSpecialCategory(Name.get(position)) ? 0 : 1;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private final TextView union_staff_name, union_staff_name_address;
        private LinearLayout linear_layout_item_union_staff;

        private final Context context;

        public MyViewHolder(@NonNull View itemView, int viewType)
        {
            super(itemView);

            context = itemView.getContext();

            union_staff_name = itemView.findViewById((viewType == 1) ? R.id.union_staff_name : R.id.union_staff_name_main);
            union_staff_name_address = itemView.findViewById((viewType == 1) ? R.id.union_staff_name_address : R.id.union_staff_name_address1);
            linear_layout_item_union_staff = itemView.findViewById((viewType == 1) ? R.id.linear_layout_item_union_staff : R.id.linear_layout_item_union_staff_main);

            linear_layout_item_union_staff.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            String groupName = union_staff_name.getText().toString();
            String addressText = union_staff_name_address.getText().toString();

            if (!isSpecialCategory(groupName))
            {
                //открыть фрагмент списке сотрудников
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Staff staff = new Staff();

                Staff.url = addressText;
                Staff.union = groupName;

                fragmentTransaction.add(R.id.fragment_container, staff);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }
    }
}