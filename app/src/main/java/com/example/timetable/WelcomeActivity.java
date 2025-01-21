package com.example.timetable;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity  extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        new Handler().postDelayed(() ->
        {
            // Открыть основное окно
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));

            // Отключение анимации
            overridePendingTransition(0, 0);

            // Завершить текущее окно
            finish();
        }, 1000);
    }
}