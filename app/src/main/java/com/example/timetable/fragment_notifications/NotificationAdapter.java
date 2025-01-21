package com.example.timetable.fragment_notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    List<Class_NotificationItem> notificationList;
    private OnNotificationClickListener onNotificationClickListener;
    private final Context context;

    // Константы для типов уведомлений
    private static final int TYPE_GROUP = 0;
    private static final int TYPE_INSTITUTE = 1;
    private static final int TYPE_UNIVERSE = 2;

    public NotificationAdapter(Context context, List<Class_NotificationItem> notificationList)
    {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType)
        {
            case TYPE_GROUP:
                View groupView = inflater.inflate(R.layout.list_item_notification_group, parent, false);
                return new GroupViewHolder(groupView);

            case TYPE_INSTITUTE:
                View instituteView = inflater.inflate(R.layout.list_item_notification_institute, parent, false);
                return new InstituteViewHolder(instituteView);

            case TYPE_UNIVERSE:
            default:
                View universeView = inflater.inflate(R.layout.list_item_notification_universe, parent, false);
                return new UniverseViewHolder(universeView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        Class_NotificationItem notification = notificationList.get(position);

        // Загрузка из памяти должности пользователя
        SharedPreferences sharedPreferences_load_post = context.getSharedPreferences("select_post", Context.MODE_PRIVATE);
        String userPost = sharedPreferences_load_post.getString("Post", null);

        boolean userPost_boolean = userPost.equals("Владелец") || userPost.equals("Администратор") || userPost.equals("Модератор");

        if (holder instanceof GroupViewHolder)
        {
            GroupViewHolder groupViewHolder = (GroupViewHolder) holder;
            groupViewHolder.verification_notification_group.setText(notification.getVerification());
            groupViewHolder.id_notification_group.setText(notification.getId());
            groupViewHolder.name_notification_group.setText(notification.getTitle());
            groupViewHolder.time_notification_group.setText(notification.getTime());
            groupViewHolder.url_notification_group.setText(notification.getUrl());
            groupViewHolder.type_notification_group.setText(notification.getType());
            groupViewHolder.description_notification_group.setText(notification.getDescription());

            // Проверяем status_verification
            int statusVerification = Integer.parseInt(notification.getVerification());
            if (userPost_boolean)
            {
                if (statusVerification == 0)
                {
                    groupViewHolder.status_verification_group.setBackgroundResource(R.drawable.verification_red);
                }
                else if ((statusVerification == 1))
                {
                    groupViewHolder.status_verification_group.setBackgroundResource(R.drawable.verification_green);
                }
            }

            groupViewHolder.CardView_notification_group.setOnClickListener(v ->
            {
                if (onNotificationClickListener != null)
                {
                    onNotificationClickListener.onNotificationClick(notification.getVerification(), notification.getId(), notification.getTitle(), notification.getTime(),
                            notification.getUrl(), notification.getType(), notification.getDescription());
                }
            });

            // Проверяем, указано ли время, и скрываем/показываем соответствующим образом
            if (notification.getTime().equals("Не указан"))
            {
                groupViewHolder.time_notification_group.setVisibility(View.GONE);
            }
        }
        else if (holder instanceof InstituteViewHolder)
        {
            InstituteViewHolder instituteViewHolder = (InstituteViewHolder) holder;
            instituteViewHolder.verification_notification_institute.setText(notification.getVerification());
            instituteViewHolder.id_notification_institute.setText(notification.getId());
            instituteViewHolder.name_notification_institute.setText(notification.getTitle());
            instituteViewHolder.time_notification_institute.setText(notification.getTime());
            instituteViewHolder.url_notification_institute.setText(notification.getUrl());
            instituteViewHolder.type_notification_institute.setText(notification.getType());
            instituteViewHolder.description_notification_institute.setText(notification.getDescription());

            // Проверяем status_verification
            int statusVerification = Integer.parseInt(notification.getVerification());
            if (userPost_boolean)
            {
                if (statusVerification == 0)
                {
                    instituteViewHolder.status_verification_institute.setBackgroundResource(R.drawable.verification_red);
                }
                else if ((statusVerification == 1))
                {
                    instituteViewHolder.status_verification_institute.setBackgroundResource(R.drawable.verification_green);
                }
            }

            instituteViewHolder.CardView_notification_institute.setOnClickListener(v ->
            {
                if (onNotificationClickListener != null)
                {
                    onNotificationClickListener.onNotificationClick(notification.getVerification(), notification.getId(), notification.getTitle(), notification.getTime(),
                            notification.getUrl(), notification.getType(), notification.getDescription());
                }
            });

            // Проверяем, указано ли время, и скрываем/показываем соответствующим образом
            if (notification.getTime().equals("Не указан"))
            {
                instituteViewHolder.time_notification_institute.setVisibility(View.GONE);
            }
        }
        else if (holder instanceof UniverseViewHolder)
        {
            UniverseViewHolder universeViewHolder = (UniverseViewHolder) holder;
            universeViewHolder.verification_notification_universe.setText(notification.getVerification());
            universeViewHolder.id_notification_universe.setText(notification.getId());
            universeViewHolder.name_notification_universe.setText(notification.getTitle());
            universeViewHolder.time_notification_universe.setText(notification.getTime());
            universeViewHolder.url_notification_universe.setText(notification.getUrl());
            universeViewHolder.type_notification_universe.setText(notification.getType());
            universeViewHolder.description_notification_universe.setText(notification.getDescription());

            // Проверяем status_verification
            int statusVerification = Integer.parseInt(notification.getVerification());
            if (userPost_boolean)
            {
                if (statusVerification == 0)
                {
                    universeViewHolder.status_verification_universe.setBackgroundResource(R.drawable.verification_red);
                }
                else if (statusVerification == 1)
                {
                    universeViewHolder.status_verification_universe.setBackgroundResource(R.drawable.verification_green);
                }
            }

            universeViewHolder.CardView_notification_universe.setOnClickListener(v ->
            {
                if (onNotificationClickListener != null)
                {
                    onNotificationClickListener.onNotificationClick(notification.getVerification(), notification.getId(), notification.getTitle(), notification.getTime(),
                            notification.getUrl(), notification.getType(), notification.getDescription());
                }
            });

            // Проверяем, указано ли время, и скрываем/показываем соответствующим образом
            if (notification.getTime().equals("Не указан"))
            {
                universeViewHolder.time_notification_universe.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return notificationList.size();
    }

    // Метод для определения типа элемента по его позиции
    @Override
    public int getItemViewType(int position)
    {
        Class_NotificationItem notification = notificationList.get(position);
        String type = notification.getType();

        // Определение типа уведомления и возвращение соответствующего типа
        if ("Группа".equals(type))
        {
            return TYPE_GROUP;
        }
        else if ("Подразделение".equals(type))
        {
            return TYPE_INSTITUTE;
        }
        else if ("Все".equals(type))
        {
            return TYPE_UNIVERSE;
        }

        return position;
    }

    // Классы ViewHolder для разных макетов
    private static class GroupViewHolder extends RecyclerView.ViewHolder
    {
        TextView id_notification_group, name_notification_group, description_notification_group, time_notification_group, url_notification_group, type_notification_group, verification_notification_group;
        CardView CardView_notification_group;
        RelativeLayout status_verification_group;

        GroupViewHolder(View itemView)
        {
            super(itemView);
            verification_notification_group = itemView.findViewById(R.id.verification_notification_group);
            name_notification_group = itemView.findViewById(R.id.name_notification_group);
            id_notification_group = itemView.findViewById(R.id.id_notification_group);
            description_notification_group = itemView.findViewById(R.id.description_notification_group);
            time_notification_group = itemView.findViewById(R.id.time_notification_group);
            url_notification_group = itemView.findViewById(R.id.url_notification_group);
            type_notification_group = itemView.findViewById(R.id.type_notification_group);
            CardView_notification_group = itemView.findViewById(R.id.CardView_notification_group);
            status_verification_group = itemView.findViewById(R.id.status_verification_group);
        }
    }

    private static class InstituteViewHolder extends RecyclerView.ViewHolder
    {
        TextView id_notification_institute, name_notification_institute, description_notification_institute, time_notification_institute, url_notification_institute, type_notification_institute, verification_notification_institute;
        CardView CardView_notification_institute;
        RelativeLayout status_verification_institute;

        InstituteViewHolder(View itemView)
        {
            super(itemView);
            verification_notification_institute = itemView.findViewById(R.id.verification_notification_institute);
            name_notification_institute = itemView.findViewById(R.id.name_notification_institute);
            id_notification_institute = itemView.findViewById(R.id.id_notification_institute);
            description_notification_institute = itemView.findViewById(R.id.description_notification_institute);
            time_notification_institute = itemView.findViewById(R.id.time_notification_institute);
            url_notification_institute = itemView.findViewById(R.id.url_notification_institute);
            type_notification_institute = itemView.findViewById(R.id.type_notification_institute);
            CardView_notification_institute = itemView.findViewById(R.id.CardView_notification_institute);
            status_verification_institute = itemView.findViewById(R.id.status_verification_institute);
        }
    }

    private static class UniverseViewHolder extends RecyclerView.ViewHolder
    {
        TextView id_notification_universe, name_notification_universe, description_notification_universe, time_notification_universe, url_notification_universe, type_notification_universe, verification_notification_universe;
        CardView CardView_notification_universe;
        RelativeLayout status_verification_universe;

        UniverseViewHolder(View itemView)
        {
            super(itemView);
            verification_notification_universe = itemView.findViewById(R.id.verification_notification_universe);
            name_notification_universe = itemView.findViewById(R.id.name_notification_universe);
            id_notification_universe = itemView.findViewById(R.id.id_notification_universe);
            description_notification_universe = itemView.findViewById(R.id.description_notification_universe);
            time_notification_universe = itemView.findViewById(R.id.time_notification_universe);
            url_notification_universe = itemView.findViewById(R.id.url_notification_universe);
            type_notification_universe = itemView.findViewById(R.id.type_notification_universe);
            CardView_notification_universe = itemView.findViewById(R.id.CardView_notification_universe);
            status_verification_universe = itemView.findViewById(R.id.status_verification_universe);
        }
    }

    public interface OnNotificationClickListener
    {
        void onNotificationClick(String verification, String id, String name, String time, String url, String type, String description);
    }
    public void setOnNotificationClickListener(OnNotificationClickListener listener)
    {
        this.onNotificationClickListener = listener;
    }
}