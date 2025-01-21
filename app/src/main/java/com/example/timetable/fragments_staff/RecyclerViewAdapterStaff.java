package com.example.timetable.fragments_staff;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.timetable.R;

import java.util.List;

public class RecyclerViewAdapterStaff extends RecyclerView.Adapter<RecyclerViewAdapterStaff.MyViewHolder>
{
    private final List<Class_staff> staffItemList;

    public void addItem(Class_staff staff)
    {
        staffItemList.add(staff);
        notifyItemInserted(staffItemList.size() - 1);
    }

    // Метод для очистки списка данных в адаптере
    public void clear()
    {
        staffItemList.clear();
        notifyDataSetChanged();
    }

    public RecyclerViewAdapterStaff(List<Class_staff> staffItemList)
    {
        this.staffItemList = staffItemList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_staff, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        Class_staff staff = staffItemList.get(position);

        // Установка данных в элементы ViewHolder
        holder.staff_name.setText(staff.getName());
        holder.staff_position.setText(staff.getPosition());
        holder.staff_url.setText(staff.getUrl());

        // Загрузка фото с использованием библиотеки Glide с округленными углами
        Glide.with(holder.itemView.getContext())
                .load(staff.getPhotoUrl())
                .placeholder(R.drawable.ic_people)  // Ваша картинка-заглушка
                .transition(DrawableTransitionOptions.withCrossFade()) // Плавное переключение между изображениями
                .centerCrop() // Изображение подгоняется к размерам с сохранением пропорций
                .into(holder.staff_photo);
    }

    @Override
    public int getItemCount()
    {
        return staffItemList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        ImageView staff_photo;
        TextView staff_name, staff_position, staff_url;
        CardView linear_layout_item_staff;

        Context context;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);

            context = itemView.getContext();

            staff_photo = itemView.findViewById(R.id.staff_photo);
            staff_name = itemView.findViewById(R.id.staff_name);
            staff_position = itemView.findViewById(R.id.staff_position);
            staff_url = itemView.findViewById(R.id.staff_url);
            linear_layout_item_staff = itemView.findViewById(R.id.linear_layout_item_staff);

            // Устанавливаем обработчик нажатий на linear_layout_item_staff
            linear_layout_item_staff.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            // Получаем текст staff_url
            String url = staff_url.getText().toString();
            String name = staff_name.getText().toString();

            if (!url.equals("Empty_URL"))
            {
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Staff_personal staff_personal = new Staff_personal();

                Staff_personal.url_personal = url;
                Staff_personal.name_personal = name;

                fragmentTransaction.add(R.id.fragment_container, staff_personal);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }
    }
}