package com.example.timetable.fragments_staff;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.timetable.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Staff_personal extends Fragment
{
    View view;
    //передача названия институтадля парсинга
    static public String url_personal = "";
    static public String name_personal = "";

    TextView personal_staff_name, personal_staff_phone, personal_staff_fax, personal_staff_mail, personal_staff_office;
    ImageView personal_staff_photo, full_screen_image_staff_personal;
    LinearLayout LinearLayout_personal_staff_phone, LinearLayout_personal_staff_fax, LinearLayout_personal_staff_mail, LinearLayout_personal_staff_office;
    RelativeLayout RelativeLayout_full_screen_image_staff_personal;

    // Для отображения меню
    PopupWindow popupMenu;
    public Context context;
    // Кнопки меню
    TextView button_copy_personal_info, button_open_personal_info;

    private SpinKitView progressBar;

    Toolbar toolbar_personal_staff;

    RecyclerViewAdapterPersonalStaff adapter;

    LinearLayout no_internet_staff_personal;

    // Таймер для проверки интернета
    private Timer internetCheckTimer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.staff_personal, container, false);

        FindID();

        // Устанавливаем название института в Toolbar
        toolbar_personal_staff.setTitle(name_personal);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar_personal_staff);
        // Добавление кнопки "Назад"
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Обработчик нажатия на кнопку "Назад"
        toolbar_personal_staff.setNavigationOnClickListener(v ->
        {
            // Вызвать метод возврата на предыдущий фрагмент
            getActivity().onBackPressed();
        });

        new LoadWebsiteUnionStaffPersonal().execute();

        context = view.getContext();
        // Инициализация кнопок меню
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_menu_personal_staff, null);
        button_copy_personal_info = popupView.findViewById(R.id.button_copy_personal_info);
        button_open_personal_info = popupView.findViewById(R.id.button_open_personal_info);

        //Настройка PopupWindow
        popupMenu = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupMenu.setElevation(8);

        // Проверяем доступность интернета
        if (!isInternetAvailable())
        {
            no_internet_staff_personal.setVisibility(View.VISIBLE);

            // Запускаем таймер проверки интернета
            startInternetCheckTimer();
        }
        else
        {
            progressBar.setVisibility(View.VISIBLE);
            // Выполнение асинхронной задачи для загрузки данных сотрудников
            new LoadWebsiteUnionStaffPersonal().execute();
        }

        // Обработчик щелчка для номера телефона
        LinearLayout_personal_staff_phone.setOnClickListener(v -> showPopupMenu(v, "телефон"));

        // Обработчик щелчка для факса
        LinearLayout_personal_staff_fax.setOnClickListener(v -> showPopupMenu(v, "факс"));

        // Обработчик щелчка для почты
        LinearLayout_personal_staff_mail.setOnClickListener(v -> showPopupMenu(v, "почта"));

        // Обработчик щелчка для адреса
        LinearLayout_personal_staff_office.setOnClickListener(v -> showPopupMenu(v, "адрес"));

        // Обработчик щелчка для фото профиля
        personal_staff_photo.setOnClickListener(v -> RelativeLayout_full_screen_image_staff_personal.setVisibility(View.VISIBLE));

        // Обработчик щелчка для большого фото
        RelativeLayout_full_screen_image_staff_personal.setOnClickListener(v -> RelativeLayout_full_screen_image_staff_personal.setVisibility(View.GONE));

        return view;
    }

    private void FindID()
    {
        personal_staff_photo = view.findViewById(R.id.personal_staff_photo);
        personal_staff_name = view.findViewById(R.id.personal_staff_name);
        personal_staff_phone = view.findViewById(R.id.personal_staff_phone);
        personal_staff_fax = view.findViewById(R.id.personal_staff_fax);
        personal_staff_mail = view.findViewById(R.id.personal_staff_mail);
        personal_staff_office = view.findViewById(R.id.personal_staff_office);
        full_screen_image_staff_personal = view.findViewById(R.id.full_screen_image_staff_personal);
        LinearLayout_personal_staff_phone = view.findViewById(R.id.LinearLayout_personal_staff_phone);
        RelativeLayout_full_screen_image_staff_personal = view.findViewById(R.id.RelativeLayout_full_screen_image_staff_personal);
        LinearLayout_personal_staff_fax = view.findViewById(R.id.LinearLayout_personal_staff_fax);
        LinearLayout_personal_staff_mail = view.findViewById(R.id.LinearLayout_personal_staff_mail);
        LinearLayout_personal_staff_office = view.findViewById(R.id.LinearLayout_personal_staff_office);
        toolbar_personal_staff = view.findViewById(R.id.toolbar_personal_staff);
        progressBar = view.findViewById(R.id.progress_bar_staff_personal);
        no_internet_staff_personal = view.findViewById(R.id.no_internet_staff_personal);
    }

    // Внутренний класс для выполнения асинхронной задачи по загрузке данных
    private class LoadWebsiteUnionStaffPersonal extends AsyncTask<Void, Void, Map<String, String>>
    {
        // Список для хранения объектов
        Map<String, String> staffInfo = new HashMap<>();
        List<Class_staff_personal> staffPersonals = new ArrayList<>();

        @Override
        protected void onPreExecute()
        {
            // Выполняется перед началом выполнения фоновой задачи (UI-подготовка)
            super.onPreExecute();
        }

        @Override
        protected Map<String, String> doInBackground(Void... voids)
        {
            try
            {
                url_personal = url_personal.replace("//", "/");
                Document document = Jsoup.connect(url_personal).get();

                if (document != null)
                {
                    // Парсинг имени
                    String name = document.select(".right_colum h1").text();
                    staffInfo.put("name", name);

                    // Парсинг фото
                    String photoUrl = "https://www.s-vfu.ru/" + document.select(".img-fluid.w-100.u-block-hover__main--zoom-v1").attr("src");
                    staffInfo.put("photo", photoUrl);

                    // Парсинг номера
                    Element phoneElement = document.select(".col-lg-9 i.icon-phone").first();
                    String number = phoneElement != null ? phoneElement.nextSibling().toString() : "";
                    staffInfo.put("number", !number.isEmpty() ? number.trim() : "Не указан");

                    // Парсинг факса
                    Element faxElement = document.select(".col-lg-9 i.icon-doc").first();
                    String fax = faxElement != null ? faxElement.nextSibling().toString() : "";
                    staffInfo.put("fax", !fax.isEmpty() ? fax.trim() : "Не указан");

                    // Парсинг email
                    Element emailElement = document.select(".col-lg-9 i.icon-envelope-letter").first();
                    String email = "";
                    if (emailElement != null)
                    {
                        Element emailLink = emailElement.parent().select("a[rel=nofollow]").first();

                        if (emailLink != null)
                        {
                            email = emailLink.text();
                        }
                    }
                    staffInfo.put("email", !email.isEmpty() ? email : "Не указан");

                    // Парсинг адреса
                    Element addressElement = document.select(".col-lg-9 i.icon-location-pin").first();
                    String address = addressElement != null ? addressElement.parent().ownText() : "";
                    staffInfo.put("address", !address.isEmpty() ? address.trim() : "Не указан");

                    //парсинг подробной инфы
                    Elements colMd12Elements = document.select(".col-md-12");
                    for (Element colMd12Element : colMd12Elements)
                    {
                        Element titleElement = colMd12Element.select("i.icon-direction").first();
                        Element textElement = colMd12Element.select(".g-px-10.pre-wrap").first();

                        if (titleElement != null && textElement != null)
                        {
                            String title = titleElement.nextSibling().toString();
                            String text = textElement.text();

                            //проверка на наличие текста
                            if (text.equals(""))
                            {
                                text = "Данные не внесены";
                            }

                            Class_staff_personal classStaffPersonal = new Class_staff_personal(title, text);
                            staffPersonals.add(classStaffPersonal);
                        }
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return staffInfo;
        }

        @Override
        protected void onPostExecute(Map<String, String> staffInfo)
        {
            if (getActivity() != null)
            {
                // Скрытие ProgressBar после выполнения задачи
                progressBar.setVisibility(View.GONE);

                personal_staff_name.setText(staffInfo.get("name"));
                personal_staff_phone.setText(staffInfo.get("number"));
                personal_staff_fax.setText(staffInfo.get("fax"));
                personal_staff_mail.setText(staffInfo.get("email"));
                personal_staff_office.setText(staffInfo.get("address"));

                // Загрузка и отображение изображения с помощью Glide
                String photoUrl = staffInfo.get("photo");
                Glide.with(getActivity()).load(photoUrl)
                        .apply(new RequestOptions().placeholder(R.drawable.ic_people)) // Заглушка на случай задержки загрузки
                        .transition(DrawableTransitionOptions.withCrossFade()) // Плавное переключение между изображениями
                        .centerCrop() // Изображение подгоняется к размерам с сохранением пропорций
                        .into(personal_staff_photo);

                //фото профиля
                RecyclerView recyclerView_personal_info = view.findViewById(R.id.recyclerView_personal_info);
                adapter = new RecyclerViewAdapterPersonalStaff(staffPersonals);
                recyclerView_personal_info.setAdapter(adapter);
                recyclerView_personal_info.setLayoutManager(new LinearLayoutManager(getActivity()));

                //большое фото
                Glide.with(getActivity()).load(photoUrl)
                        .apply(new RequestOptions().placeholder(R.drawable.ic_people)) // Заглушка на случай задержки загрузки
                        .transition(DrawableTransitionOptions.withCrossFade()) // Плавное переключение между изображениями
                        .centerCrop() // Изображение подгоняется к размерам с сохранением пропорций
                        .into(full_screen_image_staff_personal);
            }
        }
    }

    // Метод для отображения меню всплывающего окна
    private void showPopupMenu(View anchorView, String infoType)
    {
        //вывести относительно экрана справа посередине
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        int x = location[0] + anchorView.getWidth();
        int y = location[1] + anchorView.getHeight() / 2;

        // Показать меню всплывающего окна
        popupMenu.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y);

        // Создайте анимацию для масштабирования от 0 до 1
        Animation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.0f);
        scaleAnimation.setDuration(250);

        // Создайте набор анимаций и добавьте в него обе анимации
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);

        // Установите анимацию для вашего popupMenu
        popupMenu.getContentView().startAnimation(animationSet);

        button_copy_personal_info.setOnClickListener(v ->
        {
            // Обработка щелчка на кнопке Скопировать
            copyInformation(infoType);
        });

        button_open_personal_info.setOnClickListener(v ->
        {
            // Обработка щелчка на кнопке Открыть
            openInformation(infoType);
        });
    }

    // Метод для копирования информации в буфер обмена
    private void copyInformation(String infoType)
    {
        String informationToCopy = "";

        switch (infoType)
        {
            case "телефон":
                informationToCopy = personal_staff_phone.getText().toString().trim();
                break;
            case "факс":
                informationToCopy = personal_staff_fax.getText().toString().trim();
                break;
            case "почта":
                informationToCopy = personal_staff_mail.getText().toString().trim();
                break;
            case "адрес":
                informationToCopy = personal_staff_office.getText().toString().trim();
                break;
            default:
                break;
        }

        if (!informationToCopy.isEmpty() && !informationToCopy.equals("Не указан") && !informationToCopy.equals(" ") && !informationToCopy.equals("Факс:"))
        {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(infoType, informationToCopy);
            clipboard.setPrimaryClip(clip);

            switch (infoType)
            {
                case "телефон":
                    Snackbar.make(view, "Номер телефона скопирован в буфер обмена", Snackbar.LENGTH_SHORT).show();
                    break;
                case "факс":
                    Snackbar.make(view, "Номер факса скопирован в буфер обмена", Snackbar.LENGTH_SHORT).show();
                    break;
                case "почта":
                    Snackbar.make(view, "Почта скопирована в буфер обмена", Snackbar.LENGTH_SHORT).show();
                    break;
                case "адрес":
                    Snackbar.make(view, "Адрес скопирован в буфер обмена", Snackbar.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
        else
        {
            Snackbar.make(view, "Нельзя скопировать, данные не указаны", Snackbar.LENGTH_SHORT).show();
        }

        popupMenu.dismiss();
    }

    // Метод для открытия информации
    private void openInformation(String infoType)
    {
        String informationToOpen;

        switch (infoType)
        {
            case "телефон":
                informationToOpen = personal_staff_phone.getText().toString().trim();
                openPhoneNumber(informationToOpen);
                break;
            case "факс":
                informationToOpen = personal_staff_fax.getText().toString().trim();
                openPhoneNumber(informationToOpen);
                break;
            case "почта":
                informationToOpen = personal_staff_mail.getText().toString().trim();
                openEmail(informationToOpen);
                break;
            case "адрес":
                informationToOpen = personal_staff_office.getText().toString().trim();
                openAddressInMap(informationToOpen);
                break;
            default:
                break;
        }

        popupMenu.dismiss();
    }

    // Метод для открытия номера телефона
    private void openPhoneNumber(String phoneNumber)
    {
        if (!phoneNumber.isEmpty() && !phoneNumber.equals("Не указан") && !phoneNumber.equals(" ") && !phoneNumber.equals("Факс:"))
        {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(dialIntent);
        }
        else
        {
            Snackbar.make(view, "Нельзя открыть, данные не указаны", Snackbar.LENGTH_SHORT).show();
        }
    }

    // Метод для открытия почты
    private void openEmail(String email)
    {
        if (!email.isEmpty() && !email.equals("Не указан") && !email.equals("") && !email.equals("Факс:"))
        {
            // Создание интента для отправки письма
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + email));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Тема письма");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Текст письма");

            // Проверка наличия приложения для отправки почты и запуск активности
            if (emailIntent.resolveActivity(getActivity().getPackageManager()) != null)
            {
                startActivity(emailIntent);
            }
        }
        else
        {
            Snackbar.make(view, "Нельзя открыть, данные не указаны", Snackbar.LENGTH_SHORT).show();
        }
    }

    // Метод для открытия адреса на карте
    private void openAddressInMap(String address)
    {
        if (!address.isEmpty() && !address.equals("Не указан") && !address.equals("") && !address.equals("Факс:"))
        {
            // Создание URI для открытия приложений карт с указанным местоположением
            Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));

            // Создание интента для открытия приложений карт
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Проверка доступных приложений карт и создание диалога выбора
            Intent chooserIntent = Intent.createChooser(mapIntent, "Выберите приложение для открытия карт");
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null)
            {
                startActivity(chooserIntent);
            }
        }
        else
        {
            Snackbar.make(view, "Нельзя открыть, данные не указаны", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        // Отменяем таймер при уничтожении представления
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
                // Проверяем доступность интернета
                if (isInternetAvailable())
                {
                    getActivity().runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
                    getActivity().runOnUiThread(() -> no_internet_staff_personal.setVisibility(View.GONE));

                    // Отменяем таймер
                    stopInternetCheckTimer();

                    // Запускаем LoadWebsiteUnion
                    new LoadWebsiteUnionStaffPersonal().execute();
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