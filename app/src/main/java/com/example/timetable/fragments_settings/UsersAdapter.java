package com.example.timetable.fragments_settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>
{
    List<Class_users> userList;

    public UsersAdapter(List<Class_users> userList)
    {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_user_layout, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position)
    {
        Class_users user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount()
    {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTextView;
        TextView userValueTextView;

        public UserViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.id_all_users);
            userValueTextView = itemView.findViewById(R.id.group_all_users);
        }

        public void bind(Class_users user)
        {
            userNameTextView.setText(user.getUserName());
            userValueTextView.setText(user.getUserValue());
        }
    }
}