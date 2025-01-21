package com.example.timetable.fragments_settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder>
{
    private final List<Class_admin> adminList;

    public AdminAdapter(List<Class_admin> adminList)
    {
        this.adminList = adminList;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_layout, parent, false);

        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position)
    {
        Class_admin admin = adminList.get(position);
        holder.adminId.setText(admin.getAdminID());
        holder.adminPost.setText(admin.getAdminValue());

        // Сохраняем имя админа в поле AdminViewHolder
        holder.adminIDStr = admin.getAdminID();
        holder.adminPostStr = admin.getAdminValue();
    }

    @Override
    public int getItemCount()
    {
        return adminList.size();
    }

    public static class AdminViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView adminId;
        TextView adminPost;
        LinearLayout linear_layout_item_admin;

        private String adminIDStr, adminPostStr;

        // Для отображения меню
        PopupWindow popupMenu;
        // Кнопки меню
        TextView makeOwner, makeAdmin, makeModerator, delete;

        public final Context context;

        SharedPreferences sharedPreferences_load_post;
        String userPost;

        public AdminViewHolder(View itemView)
        {
            super(itemView);

            context = itemView.getContext();

            adminId = itemView.findViewById(R.id.admin_id);
            adminPost = itemView.findViewById(R.id.admin_post);
            linear_layout_item_admin = itemView.findViewById(R.id.linear_layout_item_admin);

            linear_layout_item_admin.setOnClickListener(this);

            // Инициализация кнопок меню
            View popupView = LayoutInflater.from(context).inflate(R.layout.popup_menu_item_admin, null);
            makeOwner = popupView.findViewById(R.id.button_owner_popup_menu_item_admin);
            makeAdmin = popupView.findViewById(R.id.button_admin_popup_menu_item_admin);
            makeModerator = popupView.findViewById(R.id.button_moderator_popup_menu_item_admin);
            delete = popupView.findViewById(R.id.button_delete_popup_menu_item_admin);

            // Загрузка из памяти должности пользователя
            sharedPreferences_load_post = context.getSharedPreferences("select_post", Context.MODE_PRIVATE);
            userPost = sharedPreferences_load_post.getString("Post", null);
            if (userPost.equals("Владелец"))
            {
                makeOwner.setVisibility(View.VISIBLE);
                makeAdmin.setVisibility(View.VISIBLE);
            }
            if (userPost.equals("Администратор"))
            {
                makeAdmin.setVisibility(View.VISIBLE);
            }

            //Настройка PopupWindow
            popupMenu = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupMenu.setElevation(8);
        }

        @Override
        public void onClick(View v)
        {
            if (v.getId() == R.id.linear_layout_item_admin)
            {
                //вывести относительно экрана справа посередине
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                int x = location[0] + v.getWidth();
                int y = location[1] + v.getHeight() / 2;

                // Показать меню всплывающего окна
                popupMenu.showAtLocation(v, Gravity.NO_GRAVITY, x, y);

                // Создайте анимацию для масштабирования от 0 до 1
                Animation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
                scaleAnimation.setDuration(250);

                // Создайте набор анимаций и добавьте в него обе анимации
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(scaleAnimation);

                // Установите анимацию для вашего popupMenu
                popupMenu.getContentView().startAnimation(animationSet);

                makeOwner.setOnClickListener(view ->
                {
                    if ((userPost.equals("Модератор") && !adminPostStr.equals("Владелец") && !adminPostStr.equals("Администратор")) ||
                            (userPost.equals("Администратор") && !adminPostStr.equals("Владелец")) || (userPost.equals("Владелец")))
                    {
                        showSelect(itemView.getContext(), "Назначить владельцем", "Вы уверены, что хотите назначить " + adminIDStr + " владельцем?", "Владелец", adminIDStr);
                        popupMenu.dismiss();
                    }
                    else
                    {
                        // отображения уведомления
                        View rootView = ((AppCompatActivity) context).findViewById(R.id.fragment_container);
                        Snackbar snackbar = Snackbar.make(rootView, "У вас не хватает прав", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                });

                makeAdmin.setOnClickListener(view ->
                {
                    if ((userPost.equals("Модератор") && !adminPostStr.equals("Владелец") && !adminPostStr.equals("Администратор")) ||
                            (userPost.equals("Администратор") && !adminPostStr.equals("Владелец")) || (userPost.equals("Владелец")))
                    {
                        showSelect(itemView.getContext(), "Назначить администратором", "Вы уверены, что хотите назначить " + adminIDStr + " администратором?", "Администратор", adminIDStr);
                        popupMenu.dismiss();
                    }
                    else
                    {
                        // отображения уведомления
                        View rootView = ((AppCompatActivity) context).findViewById(R.id.fragment_container);
                        Snackbar snackbar = Snackbar.make(view, "Ошибка! У вас не хватает прав", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                });

                makeModerator.setOnClickListener(view ->
                {
                    if ((userPost.equals("Модератор") && !adminPostStr.equals("Владелец") && !adminPostStr.equals("Администратор")) ||
                            (userPost.equals("Администратор") && !adminPostStr.equals("Владелец")) || (userPost.equals("Владелец")))
                    {
                        showSelect(itemView.getContext(), "Назначить модератором", "Вы уверены, что хотите назначить " + adminIDStr + " модератором?", "Модератор", adminIDStr);
                        popupMenu.dismiss();
                    }
                    else
                    {
                        // отображения уведомления
                        View rootView = ((AppCompatActivity) context).findViewById(R.id.fragment_container);
                        Snackbar snackbar = Snackbar.make(rootView, "У вас не хватает прав", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                });
                delete.setOnClickListener(view ->
                {
                    if ((userPost.equals("Модератор") && !adminPostStr.equals("Владелец") && !adminPostStr.equals("Администратор")) ||
                            (userPost.equals("Администратор") && !adminPostStr.equals("Владелец")) || (userPost.equals("Владелец")))
                    {
                        showSelect(itemView.getContext(), "Удалить", "Вы уверены, что хотите удалить " + adminIDStr + "?", "Удалить", adminIDStr);
                        popupMenu.dismiss();
                    }
                    else
                    {
                        // отображения уведомления
                        View rootView = ((AppCompatActivity) context).findViewById(R.id.fragment_container);
                        Snackbar snackbar = Snackbar.make(rootView, "У вас не хватает прав", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                });
            }
        }

        // Метод для отображения диалога подтверждения
        private static void showSelect(Context context, String title, String message, String action, String UID)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null);
            builder.setView(dialogView);

            TextView title_alert_dialog = dialogView.findViewById(R.id.title_alert_dialog);
            TextView message_alert_dialog = dialogView.findViewById(R.id.message_alert_dialog);
            Button positive_button_alert_dialog = dialogView.findViewById(R.id.positive_button_alert_dialog);
            Button negative_button_alert_dialog = dialogView.findViewById(R.id.negative_button_alert_dialog);

            //вывести заголовок и текст
            title_alert_dialog.setText(title);
            message_alert_dialog.setText(message);
            positive_button_alert_dialog.setText("Да");
            negative_button_alert_dialog.setText("Нет");

            AlertDialog dialog = builder.create();

            positive_button_alert_dialog.setOnClickListener(v ->
            {
                if (action.equals("Удалить"))
                {
                    // Удалить админа
                    delete(context, UID);
                }
                else
                {
                    // Изменить должность
                    change(context, action, UID);
                }

                dialog.dismiss();
            });

            negative_button_alert_dialog.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }

        // Метод для изменения должности
        private static void change(Context context, String newRole, String UID)
        {
            try
            {
                // Проверка доступности интернета
                if (isInternetAvailable(context))
                {
                    FirebaseDatabase.getInstance("https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference("Пользователи").child("Админы").child(UID).setValue(newRole);

                    // отображения уведомления
                    View rootView = ((AppCompatActivity) context).findViewById(R.id.fragment_container);
                    Snackbar snackbar = Snackbar.make(rootView, "Должность переназначена", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
                else
                {
                    // отображения уведомления
                    View rootView = ((AppCompatActivity) context).findViewById(R.id.fragment_container);
                    Snackbar snackbar = Snackbar.make(rootView, "Ошибка! Отсутствует интернет", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        // Метод для удаления админа
        private static void delete(Context context, String UID)
        {
            try
            {
                // Проверка доступности интернета
                if (isInternetAvailable(context))
                {
                    FirebaseDatabase.getInstance("https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference("Пользователи").child("Админы").child(UID).removeValue();

                    // отображения уведомления
                    View rootView = ((AppCompatActivity) context).findViewById(R.id.fragment_container);
                    Snackbar snackbar = Snackbar.make(rootView, "Пользователь снят с должности", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
                else
                {
                    // отображения уведомления
                    View rootView = ((AppCompatActivity) context).findViewById(R.id.fragment_container);
                    Snackbar snackbar = Snackbar.make(rootView, "Ошибка! Отсутствует интернет", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    //проверка наличия интернета
    private static boolean isInternetAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }
}