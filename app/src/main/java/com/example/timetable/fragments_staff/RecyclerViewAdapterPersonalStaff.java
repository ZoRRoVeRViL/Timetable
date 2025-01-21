package com.example.timetable.fragments_staff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;

import java.util.List;

public class RecyclerViewAdapterPersonalStaff extends RecyclerView.Adapter<RecyclerViewAdapterPersonalStaff.ViewHolder>
{
    private final List<Class_staff_personal> staff_personals;

    public RecyclerViewAdapterPersonalStaff(List<Class_staff_personal> staff_personals)
    {
        this.staff_personals = staff_personals;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_staff_personal_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Class_staff_personal class_staff_personal = staff_personals.get(position);
        holder.personal_staff_head.setText(class_staff_personal.getTitle());
        holder.personal_staff_body.setText(class_staff_personal.getText());
    }

    @Override
    public int getItemCount()
    {
        return staff_personals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView personal_staff_head, personal_staff_body;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            personal_staff_head = itemView.findViewById(R.id.personal_staff_head);
            personal_staff_body = itemView.findViewById(R.id.personal_staff_body);
        }
    }
}
