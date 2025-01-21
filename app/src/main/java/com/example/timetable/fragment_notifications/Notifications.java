package com.example.timetable.fragment_notifications;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.timetable.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Notifications extends Fragment
{
    View view;
    Context context;

    TextView id_notification, type_notification, text_verification;
    EditText name_notification, time_notification, url_notification, edit_notification;
    String str_id_notification, str_url_notification, str_type_notification;
    Button open_url_notification;

    LinearLayout no_internet_notification, linear_layout_edit_notification, LinearLayout_url_notification;
    RelativeLayout RelativeLayout_verification;
    View verification_view;

    ImageView button_back_notification, button_share_notification, button_delete_notification, button_save_notification, button_verified, button_not_verified;

    private SpinKitView progressBar;

    // Таймер для проверки интернета
    private Timer internetCheckTimer;

    DatabaseReference databaseReference, databaseNotification;

    //загрузка
    SharedPreferences sharedPreferences_load, sharedPreferences_load_post;
    String GroupUnion, GroupName, userPost;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.notifications, container, false);

        //инициализцаия интерфейса
        findID();

        // Получение контекста из фрагмента
        context = getContext();

        // Загрузка из памяти название группы и института
        sharedPreferences_load = requireContext().getSharedPreferences("select_group", Context.MODE_PRIVATE);
        GroupUnion = sharedPreferences_load.getString("GroupUnion", null);
        GroupName = sharedPreferences_load.getString("GroupName", null);

        // Загрузка из памяти должности пользователя
        sharedPreferences_load_post = requireContext().getSharedPreferences("select_post", Context.MODE_PRIVATE);
        userPost = sharedPreferences_load_post.getString("Post", null);

        // Получите данные из аргументов
        Bundle args = getArguments();
        if (args != null)
        {
            String verification = args.getString("verification");
            if (verification.equals("0"))
            {
                text_verification.setText("Статус: ждёт модерацию...");
            }
            else
            {
                text_verification.setText("Статус: прошел модерацию!");
            }
            String uid = "UID" + args.getString("id");
            id_notification.setText(uid);
            str_id_notification = args.getString("id");
            name_notification.setText(args.getString("name"));
            if(!args.getString("time").equals(""))
            {
                time_notification.setText(args.getString("time"));
            }
            else
            {
                time_notification.setText("Время не указано");
            }
            edit_notification.setText(args.getString("description"));
            str_url_notification = args.getString("url");
            String url;
            if(!args.getString("url").equals(""))
            {
                url = args.getString("url");
            }
            else
            {
                url = "Ссылка не указана";
            }
            url_notification.setText(url);
            type_notification.setText(args.getString("type"));
            str_type_notification = args.getString("type");
        }

        //условия для должности
        if ((userPost.equals("Владелец") || userPost.equals("Администратор") || userPost.equals("Модератор")) &&
                (str_type_notification.equals("Группа") || str_type_notification.equals("Подразделение")))
        {
            Permissions();
        }
        else if ((userPost.equals("Владелец") || userPost.equals("Администратор")) && (str_type_notification.equals("Все")))
        {
            Permissions();
        }
        else if ((userPost.equals("Модератор")) && (str_type_notification.equals("Все")))
        {
            LinearLayout_url_notification.setVisibility(View.VISIBLE);
            id_notification.setVisibility(View.VISIBLE);
        }

        if (isInternetAvailable())
        {
            progressBar.setVisibility(View.VISIBLE);

            // Инициализация Firebase Realtime Database
            databaseReference = FirebaseDatabase.getInstance("https://timetable-studcal-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Подразделения");
            switch (str_type_notification)
            {
                case "Группа":
                    databaseNotification = databaseReference.child(GroupUnion).child(GroupName).child("Объявления").child(str_id_notification);
                    break;
                case "Подразделение":
                    databaseNotification = databaseReference.child(GroupUnion).child("Объявления").child(str_id_notification);
                    break;
                case "Все":
                    databaseNotification = databaseReference.child("Объявления").child(str_id_notification);
                    break;
            }

            //проверка наличия времени
            if (args.getString("time") != null && !args.getString("time").isEmpty())
            {
                time_notification.setVisibility(View.VISIBLE);
            }
            //проверка наличия ссылки
            if (str_url_notification != null && !str_url_notification.isEmpty())
            {
                open_url_notification.setVisibility(View.VISIBLE);
            }

        }
        else
        {
            //Иконка нет интернета
            no_internet_notification.setVisibility(View.VISIBLE);

            // Запускаем таймер проверки интернета
            startInternetCheckTimer();

        }
        progressBar.setVisibility(View.GONE);

        //открытие ссылка при нажатии на кнопку
        open_url_notification.setOnClickListener(v ->
        {
            //Intent для открытия веб-сайта
            Uri uri = Uri.parse(str_url_notification);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);

            //есть ли приложение для просмотра веб-сайтов
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null)
            {
                startActivity(intent);
            }
            else
            {
                if (context != null)
                {
                    showSnackbar("Нет приложения для открытия ссылки");
                }
            }
        });

        // Обработчик для кнопки "назад"
        button_back_notification.setOnClickListener(v -> getFragmentManager().popBackStack());
        // Обработчик для кнопки "поделиться"
        button_share_notification.setOnClickListener(v -> ShareNotification());
        // Обработчик для кнопки сохранения
        button_save_notification.setOnClickListener(v -> showSave(view.getContext(), "сохранить"));
        // Обработчик для кнопки прохождения модерации
        button_verified.setOnClickListener(v -> showSave(view.getContext(), "подтверждение"));
        // Обработчик для долгой кнопки не прохождения модерации
        button_not_verified.setOnClickListener(v -> showSave(view.getContext(), "отказ"));
        // Обработчик для короткого нажатия на кнопку удаления
        button_delete_notification.setOnClickListener(v -> showSave(view.getContext(), "удалить"));

        // Обработчик для фоукса на EditText
        linear_layout_edit_notification.setOnClickListener(v ->
        {
            // Передать фокус EditText
            edit_notification.requestFocus();
            // Переместить курсор в конец текста
            edit_notification.setSelection(edit_notification.getText().length());

            // Откройте клавиатуру
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edit_notification, InputMethodManager.SHOW_IMPLICIT);
        });

        return view;
    }

    //инициализцаия интерфейса
    private void findID()
    {
        button_back_notification = view.findViewById(R.id.button_back_notification);
        button_share_notification = view.findViewById(R.id.button_share_notification);
        button_delete_notification = view.findViewById(R.id.button_delete_notification);
        button_save_notification = view.findViewById(R.id.button_save_notification);
        button_verified = view.findViewById(R.id.button_verified);
        button_not_verified = view.findViewById(R.id.button_not_verified);
        id_notification = view.findViewById(R.id.id_notification);
        url_notification = view.findViewById(R.id.url_notification);
        text_verification = view.findViewById(R.id.text_verification);
        name_notification = view.findViewById(R.id.name_notification);
        time_notification = view.findViewById(R.id.time_notification);
        type_notification = view.findViewById(R.id.type_notification);
        edit_notification = view.findViewById(R.id.edit_notification);
        open_url_notification = view.findViewById(R.id.open_url_notification);
        progressBar = view.findViewById(R.id.progress_bar_notification);
        no_internet_notification = view.findViewById(R.id.no_internet_notification);
        linear_layout_edit_notification = view.findViewById(R.id.linear_layout_edit_notification);
        LinearLayout_url_notification = view.findViewById(R.id.LinearLayout_url_notification);
        RelativeLayout_verification = view.findViewById(R.id.RelativeLayout_verification);
        verification_view = view.findViewById(R.id.verification_view);
    }

    //условия для должности
    private void Permissions()
    {
        LinearLayout_url_notification.setVisibility(View.VISIBLE);
        RelativeLayout_verification.setVisibility(View.VISIBLE);
        button_delete_notification.setVisibility(View.VISIBLE);
        button_save_notification.setVisibility(View.VISIBLE);
        verification_view.setVisibility(View.VISIBLE);
        id_notification.setVisibility(View.VISIBLE);
        name_notification.setEnabled(true);
        edit_notification.setEnabled(true);
        time_notification.setEnabled(true);
        url_notification.setEnabled(true);
    }

    //Поделиться заданием
    private void ShareNotification()
    {
        String notificationText = edit_notification.getText().toString();
        String nameText = name_notification.getText().toString();
        String timeText = time_notification.getText().toString();
        String urlText = str_url_notification;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, nameText + "\n\n" + timeText + "\n\n" + notificationText + "\n\nСсылка: " + urlText);

        // Показать диалог Поделиться
        startActivity(Intent.createChooser(shareIntent, "Поделиться заданием"));
    }

    //сохранение в FireBase
    public void SaveNotification()
    {
        //если есть инет
        if (isInternetAvailable())
        {
            progressBar.setVisibility(View.VISIBLE);

            // Получаем текст из EditText
            String name = name_notification.getText().toString();
            String time = time_notification.getText().toString();
            if (time.equals("Время не указано"))
            {
                time = "";
            }
            String url = url_notification.getText().toString();
            if (url.equals("Ссылка не указана"))
            {
                url = "";
            }
            String text = edit_notification.getText().toString();

            if (!name.isEmpty() && !text.isEmpty())
            {
                //объект для сохранения данных
                Map<String, Object> data = new HashMap<>();
                data.put("Заголовок", name);
                data.put("Время", time);
                data.put("Ссылка", url);
                data.put("Описание", text);

                // Обновление данные в базе данных
                databaseNotification.updateChildren(data).addOnSuccessListener(aVoid ->
                {
                    // Данные успешно сохранены
                    showSnackbar("Объявление изменено");
                }).addOnFailureListener(e ->
                {
                    // Ошибка при сохранении данных
                    showSnackbar("Ошибка при изменения объявления" + e.getMessage());
                }).addOnCanceledListener(() ->
                {
                    // Операция отменена
                    showSnackbar("Изменения отменены");
                });

            }
            else
            {
                if (context != null)
                {
                    showSnackbar("Поле объявления пустое. Введите объявление");
                }

            }

            progressBar.setVisibility(View.GONE);
        }
        else
        {
            if (context != null)
            {
                //сообщение об ошибке, если поле задания пусто
                showSnackbar("Ошибка изменения, нет интернета");
            }
        }
    }

    //очистка фокуса
    private void clearFocusAndHideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        name_notification.clearFocus();
        time_notification.clearFocus();
        url_notification.clearFocus();
        edit_notification.clearFocus();
    }

    //уведомления
    private void showSnackbar(String message)
    {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    // Метод для отображения диалога подтверждения
    private void showSave(Context context, String select)
    {
        clearFocusAndHideKeyboard();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null);
        builder.setView(dialogView);

        TextView title_alert_dialog = dialogView.findViewById(R.id.title_alert_dialog);
        TextView message_alert_dialog = dialogView.findViewById(R.id.message_alert_dialog);
        Button positive_button_alert_dialog = dialogView.findViewById(R.id.positive_button_alert_dialog);
        Button negative_button_alert_dialog = dialogView.findViewById(R.id.negative_button_alert_dialog);

        //вывести заголовок и текст
        switch (select)
        {
            case "подтверждение":
                title_alert_dialog.setText("Подтверждение");
                message_alert_dialog.setText("Это объявление корректно и может пройти модерацию?");
                break;
            case "отказ":
                title_alert_dialog.setText("Отказ");
                message_alert_dialog.setText("Это объявление не корректно и не может пройти модерацию?");
                break;
            case "сохранить":
                title_alert_dialog.setText("Сохранить");
                message_alert_dialog.setText("Сохранить изменения в объявлении?");
                break;
            case "удалить":
                title_alert_dialog.setText("Удалить");
                message_alert_dialog.setText("Удалить данное объявление?");
                break;
        }
        positive_button_alert_dialog.setText("Да");
        negative_button_alert_dialog.setText("Нет");

        AlertDialog dialog = builder.create();

        positive_button_alert_dialog.setOnClickListener(v ->
        {
            switch (select)
            {
                case "подтверждение":
                    Verified("1");
                    break;
                case "отказ":
                    Verified("0");
                    break;
                case "сохранить":
                    SaveNotification();
                    break;
                case "удалить":
                    DeleteNotification();
                    break;
            }

            dialog.dismiss();
        });

        negative_button_alert_dialog.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    //сохранение в FireBase
    public void Verified(String select)
    {
        //если есть инет
        if (isInternetAvailable())
        {
            progressBar.setVisibility(View.VISIBLE);

            databaseNotification.child("Проверка").setValue(select).addOnSuccessListener(aVoid ->
            {
                // сообщение об успешном сохранении
                if (select.equals("1"))
                {
                    if (context != null)
                    {
                        showSnackbar("Объявление прошло модерацию");
                    }
                }
                else
                {
                    if (context != null)
                    {
                        showSnackbar("Объявлению отказано в модерации");
                    }
                }

                progressBar.setVisibility(View.GONE);

                getActivity().getSupportFragmentManager().popBackStack();
            }).addOnFailureListener(e ->
            {
                if (context != null)
                {
                    // Обработка ошибки при сохранении
                    showSnackbar("Ошибка при модерации объявления: " + e.getMessage());

                    progressBar.setVisibility(View.GONE);
                }
            });
        }
        else
        {
            if (context != null)
            {
                //сообщение об ошибке, если поле задания пусто
                showSnackbar("Ошибка модерации, нет интернета");
            }
        }
    }

    //удаление обьявления из FireBase
    public void DeleteNotification()
    {
        //если есть инет
        if (isInternetAvailable())
        {
            progressBar.setVisibility(View.VISIBLE);

            // Очистить узел в Firebase, установив его значение в null
            databaseNotification.removeValue().addOnSuccessListener(aVoid ->
            {
                if (context != null)
                {
                    // Успешно очищено
                    showSnackbar("Объявление удалено");
                }

                // Очистите EditText после очистки задания
                edit_notification.setText("");

                progressBar.setVisibility(View.GONE);

                getFragmentManager().popBackStack();
            }).addOnFailureListener(e ->
            {
                if (context != null)
                {
                    // Ошибка при очистке
                    showSnackbar("Ошибка при удалении объявления");
                }
            });
        }
        else
        {
            if (context != null)
            {
                //сообщение об ошибке, если поле задания пусто
                showSnackbar("Ошибка удаления, нет интернета");
            }
        }
    }

    //отмена таймера при уничтожении представления
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        stopInternetCheckTimer();
    }

    // Запуск таймера проверки интернета
    private void startInternetCheckTimer()
    {
        internetCheckTimer = new Timer();
        internetCheckTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    // Проверяем доступность интернета
                    if (isInternetAvailable())
                    {
                        getActivity().runOnUiThread(() -> no_internet_notification.setVisibility(View.GONE));
                        getActivity().runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

                        // Отменяем таймер
                        stopInternetCheckTimer();
                    }
                }
                catch (Exception e)
                {
                    // Обработка ошибки при проверке интернета
                    e.printStackTrace();
                }
            }
        }, 0, 2000); // Запуск каждые 2 секунды
    }

    // Остановка таймера проверки интернета
    private void stopInternetCheckTimer()
    {
        if (internetCheckTimer != null)
        {
            internetCheckTimer.cancel();
            internetCheckTimer = null;
        }
    }

    //проверка наличия интернета
    private boolean isInternetAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            return networkInfo != null && networkInfo.isConnected();
        }

        return false;
    }
}